package jasima.core.simulation;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import jasima.core.util.StringUtil;

/**
 * Interface to be implemented by all components containing sub-components. This
 * interface is usually not implemented directly, but by deriving from
 * {@link SimComponentContainerBase}.
 */
public interface SimComponentContainer extends SimComponent {

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
		if (hierarchicalName.length() == 0) {
			return this;
		}

		// next segment matching a child?
		int childNameEnd = hierarchicalName.indexOf(NAME_SEPARATOR);
		String childName = (childNameEnd >= 0) ? hierarchicalName.substring(0, childNameEnd) : hierarchicalName;

		SimComponent comp = getChildByName(childName);
		return comp != null ? comp.getByHierarchicalName(hierarchicalName) : null;
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
