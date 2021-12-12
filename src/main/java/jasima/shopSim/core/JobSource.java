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

import jasima.core.simulation.SimEvent;
import jasima.core.simulation.SimComponentBase;

/**
 * A job source is an abstract base class for classes producing {@link Job}s.
 * 
 * @author Torsten Hildebrandt
 */
public abstract class JobSource extends SimComponentBase {

	// bigger than WorkStation.DEPART_PRIO but smaller than
	// WorkStation.SELECT_PRIO
	public static final int ARRIVE_PRIO = SimEvent.EVENT_PRIO_HIGH;

	private Shop shop;

	public boolean stopArrivals;
	public int jobsStarted;
	public int index; // index in shop.sources

	public JobSource() {
		super();
	}

	@Override
	public void init() {
		super.init();

		stopArrivals = false;
		jobsStarted = 0;

		SimEvent arriveEvent = new SimEvent(0.0d, ARRIVE_PRIO) {

			private Job nextJob; // next job to be released

			@Override
			public void handle() {
				if (stopArrivals)
					return;

				// create new job
				Job job = createNextJob();

				if (job != null) {
					if (job.getRelDate() < simTime())
						throw new IllegalStateException("arrival time is in the past: " + job);

					// schedule next arrival reusing this Event object
					this.setTime(job.getRelDate());
					getSim().schedule(this);
				}

				// release "nextJob"
				if (nextJob != null) {
					getShop().startJob(nextJob);
				}

				nextJob = job;
			}

		};

		// schedule first arrival
		arriveEvent.setTime(simTime());
		getSim().schedule(arriveEvent);
	}

	public abstract Job createNextJob();

	/**
	 * Factory method used in {@link #createNextJob()} to create a new job
	 * instance.
	 * 
	 * @return The new {@link Job} instance.
	 */
	protected Job newJobInstance() {
		return new Job(getShop());
	}

	public Shop getShop() {
		return shop;
	}

	public void setShop(Shop shop) {
		this.shop = shop;
	}

	@Override
	public JobSource clone() {
		JobSource s = (JobSource) super.clone();
		return s;
	}

}
