package jasima.core.simulation;

public class SimComponentBase implements SimComponent {

	private Simulation sim;
	private SimComponentContainer<?> parent;

	public SimComponentBase() {
		super();
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

}
