/*******************************************************************************
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This file is part of jasima, v1.2.
 *
 * jasima is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jasima is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jasima.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
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
