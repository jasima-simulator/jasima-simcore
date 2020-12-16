package jasima.core.simulation;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import jasima.core.util.StringUtil;

/**
 * Interface to be implemented by all components containing sub-components. This
 * interface is usually not implemented directly, but by deriving from
 * {@link SimComponentContainerBase}.
 */
public interface SimComponentContainer extends SimComponent, Iterable<SimComponent> {

	List<SimComponent> getComponents();

	SimComponentContainer addComponent(SimComponent sc);

	boolean removeComponent(SimComponent sc);

	void removeAll();

	SimComponent getComponent(int index);

	int numComponents();

	SimComponent getComponentByName(String name);

	@Override
	default SimComponent getComponentByHierarchicalName(String hierarchicalName) {
		// first part of hierarchicalName matching our name?
		String thisName = hierarchicalName;

		int dotPos;
		dotPos = hierarchicalName.indexOf(NAME_SEPARATOR);
		if (dotPos >= 0) {
			thisName = hierarchicalName.substring(0, dotPos);
			hierarchicalName = hierarchicalName.substring(dotPos + 1);
		}

		if (!StringUtil.equals(thisName, getName())) {
			return null;
		}

		// find child if required
		String childName = hierarchicalName;
		dotPos = hierarchicalName.indexOf(NAME_SEPARATOR);
		if (dotPos >= 0) {
			childName = hierarchicalName.substring(0, dotPos);
		}
		SimComponent comp = getComponentByName(childName);

		return comp.getComponentByHierarchicalName(hierarchicalName);
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
