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
package jasima.shopSim.core;

import jasima.core.simulation.SimComponent;
import jasima.core.simulation.SimComponentLifeCycleListener;
import jasima.shopSim.util.BasicJobStatCollector;
import jasima.shopSim.util.ExtendedJobStatCollector;

/**
 * This class can be used as a base class for classes collecting results based
 * on job releases/job completions.
 * 
 * @author Torsten Hildebrandt
 * 
 * @see BasicJobStatCollector
 * @see ExtendedJobStatCollector
 */
public interface ShopListener extends SimComponentLifeCycleListener {

	/**
	 * Inform method to be notified of shop events.
	 */
	@Override
	default void inform(SimComponent c, Object msg) {
		if (msg == JobShopEvent.JOB_RELEASED) {
			JobShop shop = (JobShop) c;
			jobReleased(shop, shop.lastJobReleased);
		} else if (msg == JobShopEvent.JOB_FINISHED) {
			JobShop shop = (JobShop) c;
			jobFinished(shop, shop.lastJobFinished);
		} else {
			SimComponentLifeCycleListener.super.inform(c, msg);
		}
	}

	default void jobReleased(JobShop shop, Job j) {
	}

	default void jobFinished(JobShop shop, Job j) {
	}

	// protected boolean shouldCollect(Job j) {
	// return (j.getShop().simTime() >= getInitialPeriod() && j.getJobNum() >=
	// getIgnoreFirst());
	// }

	// // boring getters and setters below
	//
	// public double getInitialPeriod() {
	// return initialPeriod;
	// }
	//
	// public void setInitialPeriod(double initialPeriod) {
	// this.initialPeriod = initialPeriod;
	// }
	//
	// public int getIgnoreFirst() {
	// return ignoreFirst;
	// }
	//
	// public void setIgnoreFirst(int ignoreFirst) {
	// this.ignoreFirst = ignoreFirst;
	// }

	// @Override
	// public String toString() {
	// return getClass().getSimpleName();
	// }

}
