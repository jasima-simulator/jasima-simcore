/*******************************************************************************
 * This file is part of jasima, v1.3, the Java simulator for manufacturing and 
 * logistics.
 *  
 * Copyright (c) 2015 		jasima solutions UG
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
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
