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
