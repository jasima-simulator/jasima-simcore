package jasima.core.simulation;

import jasima.core.util.TypeUtil;
import jasima.core.util.observer.NotifierAdapter;

public class SimComponentBase implements SimComponent {

	private Simulation sim;
	private SimComponentContainer<?> parent;
	private NotifierAdapter<SimComponent, Object> adapter;

	public SimComponentBase() {
		super();
		adapter = new NotifierAdapter<>(this);
	}

	@Override
	public Simulation getSim() {
		if (sim == null) {
			// find first uplevel component with simulation set
			SimComponent p = getParent();
			while (p != null) {
				if (p.getSim() != null) {
					setSim(p.getSim());
					break; // while
				}
				p = p.getParent();
			}
		}

		return sim;
	}

	@Override
	public void setSim(Simulation s) {
		sim = s;
	}

	@Override
	public SimComponentContainer<?> getParent() {
		return parent;
	}

	@Override
	public void setParent(SimComponentContainer<?> parent) {
		this.parent = parent;
	}

	@Override
	public NotifierAdapter<SimComponent, Object> adapter() {
		return adapter;
	}

	@Override
	public SimComponentBase clone() throws CloneNotSupportedException {
		SimComponentBase c = (SimComponentBase) super.clone();

		c.adapter = new NotifierAdapter<>(c);
		for (int i = 0; i < numListener(); i++) {
			c.addListener(TypeUtil.cloneIfPossible(getListener(i)));
		}

		return c;
	}

}
