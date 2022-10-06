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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import jasima.core.simulation.Simulation.SimExecState;

/**
 * Simple implementation of a {@link SimComponentContainer}.
 * 
 * @author Torsten Hildebrandt
 */
public class SimComponentContainerBase extends SimComponentBase implements SimComponentContainer {

	private List<SimComponent> components;
	private transient Map<String, SimComponent> componentsByName;

	public SimComponentContainerBase() {
		this(null);
	}

	public SimComponentContainerBase(String name, SimComponent... children) {
		super(name);

		components = new ArrayList<>();
		componentsByName = null; // lazy initialization in getComponentByName()

		if (children != null && children.length > 0) {
			Stream.of(children).forEach(this::addChild);
		}
	}

	@Override
	public List<SimComponent> getChildren() {
		return Collections.unmodifiableList(components);
	}

	@Override
	public SimComponent getChild(int index) {
		return components.get(index);
	}

	@Override
	public @Nullable SimComponent getChildByName(String name) {
		if (componentsByName == null) {
			componentsByName = new HashMap<>();
			components.forEach(c -> componentsByName.put(c.getName(), c));
		}

		return name == null ? null : componentsByName.get(name);
	}

	@Override
	public SimComponentContainerBase addChild(SimComponent... scs) {
		for (SimComponent sc : scs) {
			// name has to be unique
			if (getChildByName(sc.getName()) != null) {
				throw new IllegalArgumentException(String.format("Container '%s' already contains a component '%s'.",
						getHierarchicalName(), sc.getName()));
			}

			// map was initialized by calling getChildByName
			componentsByName.put(sc.getName(), sc);

			components.add(sc);
			sc.setParent(this);
			sc.setSim(sim); // no getSim() here because it throws an exception on null
		}

		if (sim != null && sim.state() != SimExecState.INITIAL && isInitialized()) {
			sim.activateComponents(scs);
		}

		return this;
	}

	@Override // inherited from SimOperations
	public void addComponent(SimComponent... scs) {
		addChild(scs);
	}

	@Override
	public boolean removeChild(SimComponent sc) {
		boolean b = components.remove(sc);
		if (b) {
			sc.setParent(null);
		}
		return b;
	}

	@Override
	public void removeChildren() {
		components.forEach(c -> c.setParent(null));
		components.clear();
	}

	@Override
	public int numChildren() {
		return components.size();
	}

	@Override
	public SimComponentContainerBase clone() {
		SimComponentContainerBase clone = (SimComponentContainerBase) super.clone();

		clone.componentsByName = null;

		clone.components = new ArrayList<>();
		for (int i = 0; i < numChildren(); i++) {
			SimComponent c = getChild(i);
			clone.addChild(c.clone());
		}

		return clone;
	}

}
