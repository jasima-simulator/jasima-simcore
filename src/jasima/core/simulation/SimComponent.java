package jasima.core.simulation;

import java.util.Map;

import jasima.core.util.SilentCloneable;
import jasima.core.util.ValueStore;
import jasima.core.util.observer.Notifier;

/**
 * This interface provides basic functionality for simulation components.
 * 
 * @author Torsten Hildebrandt
 * @see Simulation
 */
public interface SimComponent extends Notifier<SimComponent, Object>, ValueStore, SilentCloneable<SimComponent> {

	/**
	 * Base class for messages send by a {@link SimComponent} to registered
	 * listeners.
	 * 
	 * @author Torsten Hildebrandt
	 * @see SimComponent#addListener(jasima.core.util.observer.NotifierListener)
	 */
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

	/**
	 * Message send when a {@link SimComponent} is requested to produce results.
	 * 
	 * @author Torsten Hildebrandt
	 */
	public static class ProduceResultsMessage extends SimComponentLifeCycleMessage {

		public final Map<String, Object> resultMap;

		public ProduceResultsMessage(Map<String, Object> resultMap) {
			super("ProduceResultsEvent");
			this.resultMap = resultMap;
		}

	}

	/**
	 * Returns the simulation this component is associated with.
	 */
	Simulation getSim();

	/**
	 * Sets the simulation this component is part of.
	 */
	void setSim(Simulation sim);

	/**
	 * Returns the current simulation time.
	 * 
	 * @see Simulation#simTime()
	 */
	default double simTime() {
		return getSim().simTime();
	}

	/**
	 * Returns the container this component is contained in.
	 */
	SimComponentContainer<?> getParent();

	/**
	 * Sets the container this component is contained in.
	 */
	void setParent(SimComponentContainer<?> p);

	// default implementations of lifecycle messages/events

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
		fire(new ProduceResultsMessage(res));
	}

	// event tracing

	/**
	 * Produces a trace message.
	 * 
	 * @see Simulation#trace(Object...)
	 */
	default void trace(Object... params) {
		getSim().trace(params);
	}

	/**
	 * Returns true is trace messages should be produced.
	 * 
	 * @see Simulation#isTraceEnabled()
	 */
	default boolean isTraceEnabled() {
		return getSim().isTraceEnabled();
	}

	// event notification, delegate to adapter

	/**
	 * {@code SimComponent}s can notify registered listeners of certain
	 * events/messages occurring. The default implementation of
	 * {@link SimComponent} informs listeners of lifecycle events such as INIT,
	 * DONE, etc.
	 */
	@Override
	Notifier<SimComponent, Object> notifierImpl();

	// ValueStore, delegate implementation

	/**
	 * {@code SimComponent}s provide a {@link ValueStore} to attach arbitrary
	 * key/value-pairs with them. This can be used as a simple extension
	 * mechanism without having to use inheritance.
	 */
	@Override
	ValueStore valueStoreImpl();

	// cloning

	/**
	 * Public clone method. Implementing classes should implement a suitable
	 * functionality or throw a {@link CloneNotSupportedException}.
	 */
	@Override
	SimComponent clone() throws CloneNotSupportedException;

}
