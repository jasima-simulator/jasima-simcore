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
