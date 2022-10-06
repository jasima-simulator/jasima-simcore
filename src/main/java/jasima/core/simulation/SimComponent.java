/*
This file is part of jasima, the Java simulator for manufacturing and logistics.
 
Copyright 2010-2022 jasima contributors (see license.txt)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package jasima.core.simulation;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import jasima.core.simulation.SimComponent.SimComponentEvent;
import jasima.core.simulation.Simulation.ProduceResultsMessage;
import jasima.core.simulation.Simulation.SimLifecycleEvent;
import jasima.core.simulation.Simulation.StdSimLifecycleEvents;
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
public interface SimComponent
		extends Notifier<SimComponent, SimComponentEvent>, SimLifecycleListener, ValueStore, Cloneable, SimOperations {

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
	public static class ProduceResultsEvent implements SimComponentEvent {

		public final Map<String, Object> resultMap;

		public ProduceResultsEvent(Map<String, Object> resultMap) {
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
	SimComponent getParent();

	/**
	 * Sets the container this component is contained in.
	 */
	void setParent(@Nullable SimComponent p);

	/**
	 * Gets the name of this component (must not be changed once set).
	 */
	String getName();

	SimComponent setName(String name);

	default boolean isValidName(String name) {
		return name != null && name.length() > 0 && name.indexOf(NAME_SEPARATOR) < 0;
	}

	boolean isInitialized();

	void setInitialized(boolean initStatus);

	// default implementation of simulation lifecycle messages/events

	@Override
	default void init() {
	}

	@Override
	default void simStart() {
	}

	@Override
	default void resetStats() {
	}

	@Override
	default void simEnd() {
	}

	@Override
	default void done() {
	}

	@Override
	default void produceResults(Map<String, Object> res) {
	}

	@Override
	default void handleOther(Simulation sim, SimLifecycleEvent event) {
	}

	@Override
	default void inform(Simulation sim, SimLifecycleEvent event) {
		// give component a chance to handle event itself
		if (event == StdSimLifecycleEvents.INIT) {
			if (isInitialized())
				throw new IllegalStateException("Component already initialized: " + toString());
			fire(SimComponentLifeCycleMessage.INIT);
			init();
			setInitialized(true);
		} else if (event == StdSimLifecycleEvents.SIM_START) {
			fire(SimComponentLifeCycleMessage.SIM_START);
			simStart();
		} else if (event == StdSimLifecycleEvents.RESET_STATS) {
			fire(SimComponentLifeCycleMessage.RESET_STATS);
			resetStats();
		} else if (event == StdSimLifecycleEvents.SIM_END) {
			fire(SimComponentLifeCycleMessage.SIM_END);
			simEnd();
		} else if (event == StdSimLifecycleEvents.DONE) {
			fire(SimComponentLifeCycleMessage.DONE);
			done();
		} else if (event instanceof ProduceResultsMessage) {
			ProduceResultsMessage pe = (ProduceResultsMessage) event;
			fire(new ProduceResultsEvent(pe.resultMap));
			produceResults(pe.resultMap);
		} else {
			handleOther(sim, event);
		}

		// propagate to all children
		for (SimComponent c : getChildren()) {
			c.inform(sim, event);
		}
	}

	/**
	 * Returns a base name for a SimComponent consisting of the hierarchical
	 * representation of the parent ({@link #getParent()}), if it exists, and the
	 * (simple) name of the component's class.
	 */
	default String getHierarchicalName() {
		StringBuilder sb = new StringBuilder();
		SimComponent p = getParent();
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
	default void addComponent(SimComponent... scs) {
		throw new UnsupportedOperationException("Can only add components to a container.");
	}

	/**
	 * Returns a list of all child components of this container.
	 */
	default List<SimComponent> getChildren() {
		return Collections.emptyList();
	}

	/**
	 * Adds one or more child nodes to the container.
	 * 
	 * @return the container itself (to allow chaining calls)
	 */
	default SimComponentContainer addChild(SimComponent... scs) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Removes a child from this container.
	 * 
	 * @param sc the child to remove
	 * @return true, if the child was removed; false otherwise
	 */
	default boolean removeChild(SimComponent sc) {
		return false;
	}

	/**
	 * Returns {@code true}, if this container contains the node given as a
	 * parameter.
	 */
	default boolean containsChild(SimComponent sc) {
		return sc.getParent() == this;
	}

	/**
	 * Removes all child nodes from this container.
	 */
	default void removeChildren() {
	}

	/**
	 * Returns the number of children currently contained in this container.
	 */
	default int numChildren() {
		return 0;
	}

	/**
	 * Returns the child identified by {@code index}.
	 * 
	 * @param index the child's index (0-based; range: [0, numChildren-1])
	 * @return The child.
	 */
	default SimComponent getChild(int index) {
		return getChildren().get(index);
	}

	/**
	 * Returns the child identified by {@code name}.
	 * 
	 * @param name The child's name.
	 * @return The child; {@code null} if not found.
	 */
	default @Nullable SimComponent getChildByName(String name) {
		return null;
	}

	/**
	 * Add a result to the simulation's result Map. If the given name starts with a
	 * dot, then the full component name (see {@link #getHierarchicalName()}) will
	 * be added as a name prefix.
	 */
	@Override
	default void addResult(String name, Object value) {
		String fullName;
		if (name.startsWith("."))
			fullName = getHierarchicalName() + name;
		else
			fullName = name;

		SimOperations.super.addResult(fullName, value);
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
