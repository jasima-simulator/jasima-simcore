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

import jasima.shopSim.util.modelDef.streams.DblStreamDef;

public class DynamicSourceDef extends SourceDef {

	private static final long serialVersionUID = 3137811924039749794L;

	private int route;
	private DblStreamDef iats;
	private DblStreamDef dueDates;
	private DblStreamDef weights;
	private int numJobs;

	public int getRoute() {
		return route;
	}

	public void setRoute(int route) {
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
