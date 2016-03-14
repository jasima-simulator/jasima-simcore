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
package jasima.shopSim.util;

import jasima.core.util.observer.NotifierService;
import jasima.core.util.observer.Subscriber;
import jasima.shopSim.core.Job;
import jasima.shopSim.core.JobShop;
import jasima.shopSim.core.JobShop.JobShopEvent;

/**
 * This class can be used as a base class for classes collecting results based
 * on job releases/job completions.
 * 
 * @author Torsten Hildebrandt
 * 
 * @see BasicJobStatCollector
 * @see ExtendedJobStatCollector
 */
public abstract class ShopListenerBase extends SimLifeCycleListenerBase implements Subscriber, Cloneable {

	private double initialPeriod = 0;
	private int ignoreFirst = 0;

	public ShopListenerBase() {
		super();
	}

	@Override
	public void register(NotifierService ns) {
		super.register(ns);
		ns.addSubscription(JobShopEvent.class, this);
	}

	/**
	 * Update method to be notified of shop events.
	 */
	@Override
	public void inform(Object o, Object e) {
		if (e == JobShop.JOB_RELEASED) {
			JobShop shop = (JobShop) o;
			jobReleased(shop, shop.lastJobReleased);
		} else if (e == JobShop.JOB_FINISHED) {
			JobShop shop = (JobShop) o;
			jobFinished(shop, shop.lastJobFinished);
		} else {
			super.inform(o, e);
		}
	}

	protected void jobReleased(JobShop shop, Job j) {
	}

	protected void jobFinished(JobShop shop, Job j) {
	}

	protected boolean shouldCollect(Job j) {
		return (j.getShop().simTime() >= getInitialPeriod() && j.getJobNum() >= getIgnoreFirst());
	}

	@Override
	public ShopListenerBase clone() throws CloneNotSupportedException {
		return (ShopListenerBase) super.clone();
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

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}
