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

import jasima.core.util.TypeUtil;
import jasima.shopSim.core.Shop;
import jasima.shopSim.util.ShopConfigurator;

/**
 * Simple data model object to hold the parameters necessary to configure a
 * {@link Shop}.
 * 
 * @author Torsten Hildebrandt
 */
public class ShopDef extends PropertySupport implements Cloneable {

	private static final long serialVersionUID = -8471081132120093539L;

	private String name = null;
	private double simulationLength = Double.NaN;
	private int maxJobsInSystem = 0;
	private int stopAfterNumJobs = 0;
	private boolean enableLookAhead = false;
	private WorkstationDef[] workstations = {};
	private SourceDef[] jobSources = {};
	private RouteDef[] routes = {};

	public double getSimulationLength() {
		return simulationLength;
	}

	public void setSimulationLength(double simulationLength) {
		firePropertyChange("simulationLength", this.simulationLength, this.simulationLength = simulationLength);
	}

	public int getMaxJobsInSystem() {
		return maxJobsInSystem;
	}

	public void setMaxJobsInSystem(int maxJobsInSystem) {
		firePropertyChange("maxJobsInSystem", this.maxJobsInSystem, this.maxJobsInSystem = maxJobsInSystem);
	}

	public int getStopAfterNumJobs() {
		return stopAfterNumJobs;
	}

	public void setStopAfterNumJobs(int stopAfterNumJobs) {
		firePropertyChange("stopAfterNumJobs", this.stopAfterNumJobs, this.stopAfterNumJobs = stopAfterNumJobs);
	}

	public boolean isEnableLookAhead() {
		return enableLookAhead;
	}

	public void setEnableLookAhead(boolean enableLookAhead) {
		firePropertyChange("enableLookAhead", this.enableLookAhead, this.enableLookAhead = enableLookAhead);
	}

	public WorkstationDef[] getWorkstations() {
		return workstations;
	}

	public void setWorkstations(WorkstationDef[] workstations) {
		firePropertyChange("workstations", this.workstations, this.workstations = workstations);
	}

	public SourceDef[] getJobSources() {
		return jobSources;
	}

	public void setJobSources(SourceDef[] js) {
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

	/**
	 * Returns a {@link ShopConfigurator} that knows how to configure a
	 * {@link Shop} using this {@link ShopDef}.
	 */
	public ShopConfigurator getShopConfigurator() {
		ShopConfigurator conf = new ShopConfigurator();
		conf.setShopDef(this);
		return conf;
	}

	@Override
	public ShopDef clone() {
		ShopDef c = (ShopDef) super.clone();

		if (workstations != null) {
			c.workstations = TypeUtil.deepCloneArrayIfPossible(workstations);
		}

		if (jobSources != null) {
			c.jobSources = TypeUtil.deepCloneArrayIfPossible(jobSources);
		}

		if (routes != null) {
			c.routes = TypeUtil.deepCloneArrayIfPossible(routes);
		}

		return c;
	}

}
