package jasima.core.simulation;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface SimComponentContainer extends SimComponent, Iterable<SimComponent> {

	List<SimComponent> getComponents();

	SimComponentContainer addComponent(SimComponent sc);

	boolean removeComponent(SimComponent sc);

	void removeAll();

	SimComponent getComponent(int index);

	int numComponents();

	SimComponent getComponentByName(String name);

	default SimComponent getComponentByHierarchicalName(String hierarchicalName) {
		int dotPos = hierarchicalName.indexOf('.');
		if (dotPos < 0) {
			// leaf level?
			return getComponentByName(hierarchicalName);
		} else {
			// get component for current level and then recurse
			String currName = hierarchicalName.substring(0, dotPos);

			SimComponent comp = getComponentByName(currName);

			if (comp != null) {
				SimComponentContainer asContainer = (SimComponentContainer) comp;
				return asContainer.getComponentByHierarchicalName(hierarchicalName.substring(dotPos + 1));
			} else {
				return null;
			}
		}
	}

	@Override
	default void init() {
		SimComponent.super.init();

		getComponents().forEach(c -> c.init());
	}

	@Override
	default void beforeRun() {
		SimComponent.super.beforeRun();

		getComponents().forEach(c -> c.beforeRun());
	}

	@Override
	default void resetStats() {
		SimComponent.super.resetStats();

		getComponents().forEach(c -> c.resetStats());
	}

	@Override
	default void afterRun() {
		SimComponent.super.afterRun();

		getComponents().forEach(c -> c.afterRun());
	}

	@Override
	default void done() {
		SimComponent.super.done();

		getComponents().forEach(c -> c.done());
	}

	@Override
	default void produceResults(Map<String, Object> res) {
		SimComponent.super.produceResults(res);

		getComponents().forEach(c -> c.produceResults(res));
	}

	default <T extends SimComponent> void componentSetHelper(T newValue, Supplier<T> getter, Consumer<T> setter) {
		T oldValue = getter.get();
		if (oldValue != null) {
			removeComponent(oldValue);
		}

		setter.accept(newValue);
		addComponent(newValue);
	}

}
