/*******************************************************************************
 * Copyright 2011, 2012 Torsten Hildebrandt and BIBA - Bremer Institut für Produktion und Logistik GmbH
 *
 * This file is part of jasima, v1.0.
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
package jasima.shopSim.util;

import jasima.core.simulation.Simulation;
import jasima.core.simulation.Simulation.SimPrintEvent;
import jasima.core.simulation.Simulation.SimEvent;
import jasima.core.util.observer.NotifierListener;
import jasima.shopSim.core.Job;
import jasima.shopSim.core.JobShop;

import java.io.Serializable;
import java.util.Map;

/**
 * This class can be used as a base class for classes collecting results based
 * on job releases/job completions.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>, 2012-08-21
 * @see BasicJobStatCollector
 */
public abstract class JobShopListenerBase implements
		NotifierListener<Simulation, SimEvent>, Serializable, Cloneable {

	private static final long serialVersionUID = 8342338485821153287L;

	private double initialPeriod = 0;
	private int ignoreFirst = 0;

	/**
	 * Update method to be notified of shop events.
	 * 
	 * @param sim
	 * @param event
	 */
	@Override
	public final void update(Simulation sim, SimEvent event) {
		if (event == JobShop.JOB_RELEASED) {
			JobShop shop = (JobShop) sim;
			jobReleased(shop, shop.lastJobReleased);
		} else if (event == JobShop.JOB_FINISHED) {
			JobShop shop = (JobShop) sim;
			jobFinished(shop, shop.lastJobFinished);
		} else if (event == Simulation.COLLECT_RESULTS) {
			produceResults(sim, sim.resultMap);
		} else if (event == Simulation.SHOP_SIM_START) {
			shopSimStart(sim);
		} else if (event == Simulation.SHOP_SIM_END) {
			shopSimEnd(sim);
		} else if (event == Simulation.SHOP_INIT) {
			init(sim);
		} else if (event == Simulation.SHOP_DONE) {
			done(sim);
		} else if (event instanceof SimPrintEvent) {
			print(sim, (SimPrintEvent) event);
		} else {
			handleOther(sim, event);
		}
	}

	protected void handleOther(Simulation sim, SimEvent event) {
	}

	protected void print(Simulation sim, SimPrintEvent event) {
	}

	protected void done(Simulation sim) {
	}

	protected void init(Simulation sim) {
	}

	protected void shopSimEnd(Simulation sim) {
	}

	protected void shopSimStart(Simulation sim) {
	}

	protected void produceResults(Simulation sim, Map<String, Object> resultMap) {
	}

	protected void jobReleased(JobShop shop, Job j) {
	}

	protected void jobFinished(JobShop shop, Job j) {
	}

	protected boolean shouldCollect(Job j) {
		return (j.getShop().simTime() >= getInitialPeriod() && j.getJobNum() >= getIgnoreFirst());
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	// boring getters and setters below

	public double getInitialPeriod() {
		return initialPeriod;
	}

	public void setInitialPeriod(double initialPeriod) {
		this.initialPeriod = initialPeriod;
	}

	public int getIgnoreFirst() {
		return ignoreFirst;
	}

	public void setIgnoreFirst(int ignoreFirst) {
		this.ignoreFirst = ignoreFirst;
	}

}
