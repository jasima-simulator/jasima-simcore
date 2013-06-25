package jasima.shopSim.util.modelDef;

public class JobDef extends PropertySupport {
	private String name;
	private RouteDef route;
	private double releaseDate;
	private double dueDate;
	private double weight = 1.0;

	public JobDef() {
		super();
	}

	public JobDef(RouteDef rd, double rel, double due, double w, String n) {
		this();
		route = rd;
		releaseDate = rel;
		dueDate = due;
		weight = w;
		name = n;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		firePropertyChange("name", this.name, this.name = name);
	}

	public RouteDef getRoute() {
		return route;
	}

	public void setRoute(RouteDef route) {
		firePropertyChange("route", this.route, this.route = route);
	}

	public double getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(double releaseDate) {
		firePropertyChange("releaseDate", this.releaseDate,
				this.releaseDate = releaseDate);
	}

	public double getDueDate() {
		return dueDate;
	}

	public void setDueDate(double dueDate) {
		firePropertyChange("dueDate", this.dueDate, this.dueDate = dueDate);
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		firePropertyChange("weight", this.weight, this.weight = weight);
	}

}
