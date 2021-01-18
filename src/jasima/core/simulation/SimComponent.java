package jasima.core.simulation;

import java.util.Map;

import javax.annotation.Nullable;

import jasima.core.simulation.SimComponent.SimComponentEvent;
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
public interface SimComponent extends Notifier<SimComponent, SimComponentEvent>, ValueStore, Cloneable, SimOperations {

	interface SimComponentEvent {
	}

	/**
	 * The separator used in {@link #getHierarchicalName()}.
	 */
	public static final char NAME_SEPARATOR = '.';

	public enum SimComponentLifeCycleMessage implements SimComponentEvent {
		INIT, SIM_START, RESET_STATS, SIM_END, DONE
	}

	/**
	 * Message send when a {@link SimComponent} is requested to produce results.
	 * 
	 * @author Torsten Hildebrandt
	 */
	public static class ProduceResultsMessage implements SimComponentEvent {

		public final Map<String, Object> resultMap;

		public ProduceResultsMessage(Map<String, Object> resultMap) {
			this.resultMap = resultMap;
		}

		@Override
		public String toString() {
			return "ProduceResultsEvent";
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

	void init();

	default void simStart() {
		fire(SimComponentLifeCycleMessage.SIM_START);
	}

	default void resetStats() {
		fire(SimComponentLifeCycleMessage.RESET_STATS);
	}

	default void simEnd() {
		fire(SimComponentLifeCycleMessage.SIM_END);
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
//			if (!(this instanceof SimComponentRoot)) {
//				sb.append(NAME_SEPARATOR);
//			}
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

	@Override // inherited from SimOperations
	default <T extends SimComponent> SimOperations addComponent(T sc) {
		throw new UnsupportedOperationException();
	}

	// event notification, delegate to adapter

	/**
	 * {@code SimComponent}s can notify registered listeners of certain
	 * events/messages occurring. The default implementation of {@link SimComponent}
	 * informs listeners of lifecycle events such as INIT, DONE, etc.
	 */
	@Override
	Notifier<SimComponent, SimComponentEvent> notifierImpl();

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
