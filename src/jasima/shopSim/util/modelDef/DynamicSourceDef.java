package jasima.shopSim.util.modelDef;

import jasima.shopSim.util.modelDef.streams.StreamDef;

public class DynamicSourceDef extends SourceDef {
	private RouteDef route;
	private StreamDef iats;
	private StreamDef dueDates;
	private StreamDef weights;
	private int numJobs;

	public RouteDef getRoute() {
		return route;
	}

	public void setRoute(RouteDef route) {
		firePropertyChange("route", this.route, this.route = route);
	}

	public StreamDef getIats() {
		return iats;
	}

	public void setIats(StreamDef iats) {
		firePropertyChange("iats", this.iats, this.iats = iats);
	}

	public StreamDef getDueDates() {
		return dueDates;
	}

	public void setDueDates(StreamDef dueDates) {
		firePropertyChange("dueDates", this.dueDates, this.dueDates = dueDates);
	}

	public StreamDef getWeights() {
		return weights;
	}

	public void setWeights(StreamDef weights) {
		firePropertyChange("weights", this.weights, this.weights = weights);
	}

	public int getNumJobs() {
		return numJobs;
	}

	public void setNumJobs(int numJobs) {
		firePropertyChange("numJobs", this.numJobs, this.numJobs = numJobs);
	}

}
