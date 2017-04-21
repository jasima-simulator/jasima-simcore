package jasima.core.simulation;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface SimComponentContainer<SUB extends SimComponent> extends SimComponent, Iterable<SUB> {

	List<SUB> getComponents();

	SimComponentContainer<SUB> addComponent(SUB sc);

	boolean removeComponent(SUB sc);

	void removeAll();

	SUB getComponent(int index);

	int numComponents();

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
	

	default <T extends SUB> void componentSetHelper(T newValue, Supplier<T> getter, Consumer<T> setter) {
		T oldValue = getter.get();
		if (oldValue != null) {
			removeComponent(oldValue);
		}

		setter.accept(newValue);
		addComponent(newValue);
	}

}
