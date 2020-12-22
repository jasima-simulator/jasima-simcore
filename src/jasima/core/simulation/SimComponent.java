package jasima.core.simulation;

import java.util.Map;

import javax.annotation.Nullable;

import jasima.core.simulation.util.SimComponentRoot;
import jasima.core.simulation.util.SimOperations;
import jasima.core.util.StringUtil;
import jasima.core.util.ValueStore;
import jasima.core.util.observer.Notifier;

/**
 * This interface provides basic functionality for simulation components.
 * 
 * @author Torsten Hildebrandt
 * @see Simulation
 */
public interface SimComponent extends Notifier<SimComponent, Object>, ValueStore, Cloneable, SimOperations {

	/**
	 * The separator used in {@link #getHierarchicalName()}.
	 */
	public static final char NAME_SEPARATOR = '.';

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
		public static final SimComponentLifeCycleMessage RESET_STATS = new SimComponentLifeCycleMessage("RESET_STATS");
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
	 * Returns the container this component is contained in.
	 */
	@Nullable
	SimComponentContainer getParent();

	/**
	 * Sets the container this component is contained in.
	 */
	void setParent(@Nullable SimComponentContainer p);

	/**
	 * Gets the name of this component (must not be changed once set).
	 */
	String getName();

	void setName(String name);

	default boolean isValidName(String name) {
		return name != null && name.length() > 0 && name.indexOf(NAME_SEPARATOR) < 0;
	}

	// default implementations of lifecycle messages/events

	default void init() {
		fire(SimComponentLifeCycleMessage.INIT);
	}

	default void beforeRun() {
		fire(SimComponentLifeCycleMessage.BEFORE_RUN);
	}

	default void resetStats() {
		fire(SimComponentLifeCycleMessage.RESET_STATS);
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

	/**
	 * Returns a base name for a SimComponent consisting of the hierarchical
	 * representation of the parent ({@link #getParent()}), if it exists, and the
	 * (simple) name of the component's class.
	 */
	default String getHierarchicalName() {
		StringBuilder sb = new StringBuilder();
		SimComponentContainer p = getParent();
		if (p != null) {
			sb.append(p.getHierarchicalName()).append(NAME_SEPARATOR);
		} else {
			if (!(this instanceof SimComponentRoot)) {
				sb.append(NAME_SEPARATOR);
			}
		}
		sb.append(this.toString());

		return sb.toString();
	}

	/**
	 * Returns the current component if this component's name matched the parameter.
	 * 
	 * @param hierarchicalName The name to check.
	 * @return this, if name matches the parameter; null otherwise.
	 * 
	 * @see SimComponentContainer#getByHierarchicalName(String)
	 */
	default SimComponent getByHierarchicalName(String hierarchicalName) {
		return StringUtil.equals(hierarchicalName, getName()) ? this : null;
	}

	// event notification, delegate to adapter

	/**
	 * {@code SimComponent}s can notify registered listeners of certain
	 * events/messages occurring. The default implementation of {@link SimComponent}
	 * informs listeners of lifecycle events such as INIT, DONE, etc.
	 */
	@Override
	Notifier<SimComponent, Object> notifierImpl();

	// ValueStore, delegate implementation

	/**
	 * {@code SimComponent}s provide a {@link ValueStore} to attach arbitrary
	 * key/value-pairs with them. This can be used as a simple extension mechanism
	 * without having to use inheritance.
	 */
	@Override
	ValueStore valueStoreImpl();

	// cloning

	/**
	 * Public clone method. Implementing classes should implement a suitable
	 * functionality or throw a {@link CloneNotSupportedException} wrapped in a
	 * {@link RuntimeException}.
	 */
	SimComponent clone();

}
