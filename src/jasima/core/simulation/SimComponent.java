package jasima.core.simulation;

import java.util.Map;

import jasima.core.util.SilentCloneable;
import jasima.core.util.observer.Notifier;
import jasima.core.util.observer.NotifierAdapter;
import jasima.core.util.observer.NotifierListener;

public interface SimComponent extends Notifier<SimComponent, Object>, SilentCloneable<SimComponent> {

	public static class SimComponentLifeCycleMessage {

		private final String name;

		public SimComponentLifeCycleMessage(String s) {
			name = s;
		}

		@Override
		public String toString() {
			return name;
		}

		public static final SimComponentLifeCycleMessage INIT = new SimComponentLifeCycleMessage("INIT");
		public static final SimComponentLifeCycleMessage BEFORE_RUN = new SimComponentLifeCycleMessage("BEFORE_RUN");
		public static final SimComponentLifeCycleMessage AFTER_RUN = new SimComponentLifeCycleMessage("AFTER_RUN");
		public static final SimComponentLifeCycleMessage DONE = new SimComponentLifeCycleMessage("DONE");
	}

	public static class ProduceResultsEvent extends SimComponentLifeCycleMessage {

		public final Map<String, Object> resultMap;

		public ProduceResultsEvent(Map<String, Object> resultMap) {
			super("ProduceResultsEvent");
			this.resultMap = resultMap;
		}

	}

	Simulation getSim();

	void setSim(Simulation sim);

	default double simTime() {
		return getSim().simTime();
	}

	SimComponentContainer<?> getParent();

	void setParent(SimComponentContainer<?> p);

	default void init() {
		fire(SimComponentLifeCycleMessage.INIT);
	}

	default void beforeRun() {
		fire(SimComponentLifeCycleMessage.BEFORE_RUN);
	}

	default void afterRun() {
		fire(SimComponentLifeCycleMessage.AFTER_RUN);
	}

	default void done() {
		fire(SimComponentLifeCycleMessage.DONE);
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
	default void addListener(NotifierListener<SimComponent, Object> l) {
		adapter().addListener(l);
	}

	@Override
	default boolean removeListener(NotifierListener<SimComponent, Object> l) {
		return adapter().removeListener(l);
	}

	@Override
	default NotifierListener<SimComponent, Object> getListener(int idx) {
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
