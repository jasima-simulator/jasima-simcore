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
	 * Adds a child node to the container.
	 * 
	 * @return the container itself (to allow chaining calls)
	 */
	SimComponentContainer addChild(SimComponent sc);

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
		// first part of hierarchicalName matching our name?
		String thisName = hierarchicalName;

		int dotPos;
		dotPos = hierarchicalName.indexOf(NAME_SEPARATOR);
		if (dotPos >= 0) {
			thisName = hierarchicalName.substring(0, dotPos);
			hierarchicalName = hierarchicalName.substring(dotPos + 1);
		}

		if (!StringUtil.equals(thisName, getName())) {
			return null; // no match
		}

		// find child if required
		String childName = hierarchicalName;
		dotPos = hierarchicalName.indexOf(NAME_SEPARATOR);
		if (dotPos >= 0) {
			childName = hierarchicalName.substring(0, dotPos);
		}
		SimComponent comp = getChildByName(childName);

		return comp.getByHierarchicalName(hierarchicalName);
	}

	// code below is forwarding lifecycle events to children

	@Override
	default void init() {
		SimComponent.super.init();

		getChildren().forEach(c -> c.init());
	}

	@Override
	default void beforeRun() {
		SimComponent.super.beforeRun();

		getChildren().forEach(c -> c.beforeRun());
	}

	@Override
	default void resetStats() {
		SimComponent.super.resetStats();

		getChildren().forEach(c -> c.resetStats());
	}

	@Override
	default void afterRun() {
		SimComponent.super.afterRun();

		getChildren().forEach(c -> c.afterRun());
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

}
