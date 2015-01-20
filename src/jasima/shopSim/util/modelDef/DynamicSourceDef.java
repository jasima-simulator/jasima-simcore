package jasima.shopSim.util.modelDef;

import jasima.shopSim.util.modelDef.streams.DblStreamDef;

public class DynamicSourceDef extends SourceDef {
	private RouteDef route;
	private DblStreamDef iats;
	private DblStreamDef dueDates;
	private DblStreamDef weights;
	private int numJobs;

	public RouteDef getRoute() {
		return route;
	}

	public void setRoute(RouteDef route) {
		firePropertyChange("route", this.route, this.route = route);
	}

	public DblStreamDef getIats() {
		return iats;
	}

	public void setIats(DblStreamDef iats) {
		firePropertyChange("iats", this.iats, this.iats = iats);
	}

	public DblStreamDef getDueDates() {
		return dueDates;
	}

	public void setDueDates(DblStreamDef dueDates) {
		firePropertyChange("dueDates", this.dueDates, this.dueDates = dueDates);
	}

	public DblStreamDef getWeights() {
		return weights;
	}

	public void setWeights(DblStreamDef weights) {
		firePropertyChange("weights", this.weights, this.weights = weights);
	}

	public int getNumJobs() {
		return numJobs;
	}

	public void setNumJobs(int numJobs) {
		firePropertyChange("numJobs", this.numJobs, this.numJobs = numJobs);
	}

}
