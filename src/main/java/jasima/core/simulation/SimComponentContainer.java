package jasima.core.simulation;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import jasima.core.util.StringUtil;

/**
 * Interface to be implemented by all components containing sub-components. This
 * interface is usually not implemented directly, but by deriving from
 * {@link SimComponentContainerBase}.
 */
public interface SimComponentContainer extends SimComponent, Iterable<SimComponent> {

	/**
	 * Returns a list of all child components of this container.
	 */
	List<SimComponent> getChildren();

	/**
	 * Adds one or more child nodes to the container.
	 * 
	 * @return the container itself (to allow chaining calls)
	 */
	SimComponentContainer addChild(SimComponent... scs);

	/**
	 * Removes a child from this container.
	 * 
	 * @param sc the child to remove
	 * @return true, if the child was removed; false otherwise
	 */
	boolean removeChild(SimComponent sc);

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
	void removeChildren();

	/**
	 * Returns the number of children currently contained in this container.
	 */
	int numChildren();

	/**
	 * Returns the child identified by {@code index}.
	 * 
	 * @param index the child's index (0-based; range: [0, numChildren-1])
	 * @return The child.
	 */
	SimComponent getChild(int index);

	/**
	 * Returns the child identified by {@code name}.
	 * 
	 * @param name The child's name.
	 * @return The child; {@code null} if not found.
	 */
	@Nullable
	SimComponent getChildByName(String name);

	/**
	 * Tries to find a component using the relative names given in the parameter.
	 * The first name segment has to match the container's name, next name-segment a
	 * child, next name-segment a child's child etc.
	 * <p>
	 * Names segments are separated using '.', i.e., a valid hierarchical name could
	 * for instance be "network1.machines.welding1".
	 * 
	 * @see Simulation#getComponentByHierarchicalName(String)
	 */
	@Override
	default @Nullable SimComponent getByHierarchicalName(String hierarchicalName) {
		// first segment of hierarchicalName matching our name?
		String thisName;
		int endThisName = hierarchicalName.indexOf(NAME_SEPARATOR);
		if (endThisName >= 0) {
			thisName = hierarchicalName.substring(0, endThisName);
			hierarchicalName = hierarchicalName.substring(endThisName + 1);
		} else {
			thisName = hierarchicalName;
			hierarchicalName = "";
		}
		if (!StringUtil.equals(thisName, getName())) {
			return null;
		}
		if (hierarchicalName.length()==0) {
			return this;
		}

		// next segment matching a child?
		int childNameEnd = hierarchicalName.indexOf(NAME_SEPARATOR);
		String childName = (childNameEnd >= 0) ? hierarchicalName.substring(0, childNameEnd) : hierarchicalName;

		SimComponent comp = getChildByName(childName);
		return comp != null ? comp.getByHierarchicalName(hierarchicalName) : null;
	}

	// code below is forwarding lifecycle events to children

	@Override
	default void simStart() {
		SimComponent.super.simStart();

		getChildren().forEach(c -> c.simStart());
	}

	@Override
	default void resetStats() {
		SimComponent.super.resetStats();

		getChildren().forEach(c -> c.resetStats());
	}

	@Override
	default void simEnd() {
		SimComponent.super.simEnd();

		getChildren().forEach(c -> c.simEnd());
	}

	@Override
	default void done() {
		SimComponent.super.done();

		getChildren().forEach(c -> c.done());
	}

	@Override
	default void produceResults(Map<String, Object> res) {
		SimComponent.super.produceResults(res);

		getChildren().forEach(c -> c.produceResults(res));
	}

	default <T extends SimComponent> void componentSetHelper(T newValue, Supplier<T> getter, Consumer<T> setter) {
		T oldValue = getter.get();
		if (oldValue != null) {
			removeChild(oldValue);
		}

		setter.accept(newValue);
		addChild(newValue);
	}

	// cloning

	/**
	 * {@inheritDoc}
	 */
	SimComponentContainer clone();

}
