package jasima.core.simulation;

import java.util.Map;

import jasima.core.simulation.Simulation.ProduceResultsEvent;
import jasima.core.simulation.Simulation.SimComponentLifeCycleEvent;
import jasima.core.util.SilentCloneable;
import jasima.core.util.observer.Notifier;
import jasima.core.util.observer.NotifierAdapter;
import jasima.core.util.observer.Subscriber;

public interface SimComponent extends Notifier<SimComponent, Object>, SilentCloneable<SimComponent> {

	Simulation getSim();

	void setSim(Simulation sim);

	default double simTime() {
		return getSim().simTime();
	}

	SimComponentContainer<?> getParent();

	void setParent(SimComponentContainer<?> p);

	default void init() {
		fire(SimComponentLifeCycleEvent.INIT);
	}

	default void beforeRun() {
		fire(SimComponentLifeCycleEvent.BEFORE_RUN);
	}

	default void afterRun() {
		fire(SimComponentLifeCycleEvent.AFTER_RUN);
	}

	default void done() {
		fire(SimComponentLifeCycleEvent.DONE);
	}

	default void produceResults(Map<String, Object> res) {
		fire(new ProduceResultsEvent(res));
	}

	// event notification

	NotifierAdapter<SimComponent, Object> adapter();

	@Override
	default int numListener() {
		return adapter().numListener();
	}

	@Override
	default void addListener(Subscriber<SimComponent, Object> l) {
		adapter().addListener(l);
	}

	@Override
	default boolean removeListener(Subscriber<SimComponent, Object> l) {
		return adapter().removeListener(l);
	}

	@Override
	default Subscriber<SimComponent, Object> getListener(int idx) {
		return adapter().getListener(idx);
	}

	@Override
	default void fire(Object msg) {
		adapter().fire(msg);
	}
	
	// cloning

	@Override
	SimComponent clone() throws CloneNotSupportedException;

}
