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
package jasima.shopSim.util.modelDef;

public class JobDef extends PropertySupport {

	private static final long serialVersionUID = 1889942721418427369L;

	private String name;
	private int route;
	private double releaseDate;
	private double dueDate;
	private double weight = 1.0;

	public JobDef() {
		super();
	}

	public JobDef(int rd, double rel, double due, double w, String n) {
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

	public int getRoute() {
		return route;
	}

	public void setRoute(int route) {
		firePropertyChange("route", this.route, this.route = route);
	}

	public double getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(double releaseDate) {
		firePropertyChange("releaseDate", this.releaseDate, this.releaseDate = releaseDate);
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
