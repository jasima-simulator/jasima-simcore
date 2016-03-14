package jasima.core.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimComponentContainerBase<T extends SimComponent> extends SimComponentBase
		implements SimComponentContainer<T> {

	private ArrayList<T> components;

	public SimComponentContainerBase() {
		super();
		components = new ArrayList<>();
	}

	@Override
	public void setSim(Simulation s) {
		super.setSim(s);

		components.forEach(c -> c.setSim(s));
	}

	@Override
	public List<T> getComponents() {
		return Collections.unmodifiableList(components);
	}

	@Override
	public T getComponent(int index) {
		return components.get(index);
	}

	@Override
	public void addComponent(T sc) {
		components.add(sc);
		sc.setParent(this);
	}

	@Override
	public boolean removeComponent(T sc) {
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

}
