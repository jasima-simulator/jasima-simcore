package jasima.core.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class SimComponentContainerBase<SUB extends SimComponent> extends SimComponentBase
		implements SimComponentContainer<SUB> {

	private ArrayList<SUB> components;
	private HashMap<String, SUB> componentsByName;

	public SimComponentContainerBase() {
		super();
		components = new ArrayList<>();
		componentsByName = new HashMap<>();
	}

	@Override
	public void setSim(Simulation s) {
		super.setSim(s);

		components.forEach(c -> c.setSim(s));
	}

	@Override
	public List<SUB> getComponents() {
		return Collections.unmodifiableList(components);
	}

	@Override
	public Iterator<SUB> iterator() {
		return components.iterator();
	}

	@Override
	public SUB getComponent(int index) {
		return components.get(index);
	}

	@Override
	public SUB getComponentByName(String name) {
		return componentsByName.get(name);
	}

	@Override
	public SimComponentContainerBase<SUB> addComponent(SUB sc) {
		// name has to be unique
		if (getComponentByName(sc.getName()) != null) {
			throw new IllegalArgumentException(String.format("Container '%s' already contains a component '%s'.",
					getHierarchicalName(), sc.getName()));
		}

		components.add(sc);
		sc.setParent(this);

		componentsByName.put(sc.getName(), sc);

		return this;
	}

	@Override
	public boolean removeComponent(SUB sc) {
		boolean b = components.remove(sc);
		if (b) {
			sc.setParent(null);
		}
		return b;
	}

	@Override
	public void removeAll() {
		components.forEach(c -> c.setParent(null));
		components.clear();
	}

	@Override
	public int numComponents() {
		return components.size();
	}

	@SuppressWarnings("unchecked")
	@Override
	public SimComponentContainerBase<SUB> clone() throws CloneNotSupportedException {
		SimComponentContainerBase<SUB> clone = (SimComponentContainerBase<SUB>) super.clone();

		clone.components = new ArrayList<>();
		for (int i = 0; i < numComponents(); i++) {
			SUB c = getComponent(i);
			clone.addComponent((SUB) c.clone());
		}

		return clone;
	}

}
