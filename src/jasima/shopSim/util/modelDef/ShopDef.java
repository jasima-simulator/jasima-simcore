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

import jasima.shopSim.core.JobShop;
import jasima.shopSim.util.ShopConfigurator;

/**
 * Simple data model object to hold the parameters necessary to configure a
 * {@link JobShop}.
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 */
public class ShopDef extends PropertySupport {

	private static final long serialVersionUID = -8471081132120093539L;

	private String name = null;
	private double simulationLength = 0.0d;
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
		firePropertyChange("simulationLength", this.simulationLength,
				this.simulationLength = simulationLength);
	}

	public int getMaxJobsInSystem() {
		return maxJobsInSystem;
	}

	public void setMaxJobsInSystem(int maxJobsInSystem) {
		firePropertyChange("maxJobsInSystem", this.maxJobsInSystem,
				this.maxJobsInSystem = maxJobsInSystem);
	}

	public int getStopAfterNumJobs() {
		return stopAfterNumJobs;
	}

	public void setStopAfterNumJobs(int stopAfterNumJobs) {
		firePropertyChange("stopAfterNumJobs", this.stopAfterNumJobs,
				this.stopAfterNumJobs = stopAfterNumJobs);
	}

	public boolean isEnableLookAhead() {
		return enableLookAhead;
	}

	public void setEnableLookAhead(boolean enableLookAhead) {
		firePropertyChange("enableLookAhead", this.enableLookAhead,
				this.enableLookAhead = enableLookAhead);
	}

	public WorkstationDef[] getWorkstations() {
		return workstations;
	}

	public void setWorkstations(WorkstationDef[] workstations) {
		firePropertyChange("workstations", this.workstations,
				this.workstations = workstations);
	}

	public SourceDef[] getJobSources() {
		return jobSources;
	}

	public void setJobSources(SourceDef[] js) {
		if (js != null)
			for (SourceDef m : js) {
				m.setShop(this);
			}
		if (jobSources != null)
			for (SourceDef m : jobSources) {
				m.setShop(null);
			}
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
	 * {@link JobShop} using this {@link ShopDef}.
	 */
	public ShopConfigurator getShopConfigurator() {
		ShopConfigurator conf = new ShopConfigurator();
		conf.setShopDef(this);
		return conf;
	}

}
