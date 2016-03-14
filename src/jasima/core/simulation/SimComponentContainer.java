package jasima.core.simulation;

import java.util.List;
import java.util.Map;

public interface SimComponentContainer<T extends SimComponent> extends SimComponent {

	List<T> getComponents();

	void addComponent(T sc);

	boolean removeComponent(T sc);

	void removeAll();
	
	T getComponent(int index);

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

}
