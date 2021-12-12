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
