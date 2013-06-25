package jasima.shopSim.util.modelDef;

public class ShopDef extends PropertySupport {

	private String name = null;
	private double simulationLength = 0.0d;
	private int maxJobsInSystem = 0;
	private int maxJobsFinished = 0;
	private boolean enableLookAhead = false;
	private WorkstationDef[] workstations = {};
	private SourceDef[] jobSources = {};
	private RouteDef[] routes = {};

	public double getSimulationLength() {
		return simulationLength;
	}

	public void setSimulationLength(double simulationLength) {
		firePropertyChange("simulationLength", this.simulationLength,
				this.simulationLength = simulationLength);
	}

	public int getMaxJobsInSystem() {
		return maxJobsInSystem;
	}

	public void setMaxJobsInSystem(int maxJobsInSystem) {
		firePropertyChange("maxJobsInSystem", this.maxJobsInSystem,
				this.maxJobsInSystem = maxJobsInSystem);
	}

	public int getMaxJobsFinished() {
		return maxJobsFinished;
	}

	public void setMaxJobsFinished(int maxJobsFinished) {
		firePropertyChange("maxJobsFinished", this.maxJobsFinished,
				this.maxJobsFinished = maxJobsFinished);
	}

	public boolean isEnableLookAhead() {
		return enableLookAhead;
	}

	public void setEnableLookAhead(boolean enableLookAhead) {
		firePropertyChange("enableLookAhead", this.enableLookAhead,
				this.enableLookAhead = enableLookAhead);
	}

	public WorkstationDef[] getWorkstations() {
		return workstations;
	}

	public void setWorkstations(WorkstationDef[] workstations) {
		firePropertyChange("workstations", this.workstations,
				this.workstations = workstations);
	}

	public SourceDef[] getJobSources() {
		return jobSources;
	}

	public void setJobSources(SourceDef[] js) {
		if (js != null)
			for (SourceDef m : js) {
				m.setShop(this);
			}
		if (jobSources != null)
			for (SourceDef m : jobSources) {
				m.setShop(null);
			}
		firePropertyChange("jobSources", this.jobSources, this.jobSources = js);
	}

	public RouteDef[] getRoutes() {
		return routes;
	}

	public void setRoutes(RouteDef[] rs) {
		if (rs != null)
			for (RouteDef m : rs) {
				m.setShop(this);
			}
		if (routes != null)
			for (RouteDef m : routes) {
				m.setShop(null);
			}
		firePropertyChange("routes", this.routes, this.routes = rs);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		firePropertyChange("name", this.name, this.name = name);
	}

}
