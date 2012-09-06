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
package jasima.shopSim.core;

import jasima.core.simulation.Event;

public abstract class JobSource {

	// bigger than WorkStation.DEPART_PRIO but smaller than
	// WorkStation.SELECT_PRIO
	public static final int ARRIVE_PRIO = Event.EVENT_PRIO_HIGH;

	private JobShop shop;

	public boolean stopArrivals;
	public int jobsStarted;
	public int index; // index in shop.sources

	public JobSource() {
		super();
	}

	public void init() {
		stopArrivals = false;
		jobsStarted = 0;

		Event arriveEvent = new Event(0.0d, ARRIVE_PRIO) {

			private Job nextJob; // next job to be released

			@Override
			public void handle() {
				if (stopArrivals)
					return;

				// create new job
				Job job = createNextJob();

				if (job != null) {
					job.setSource(JobSource.this);

					if (job.getRelDate() < getShop().simTime())
						throw new IllegalStateException(
								"arrival time is in the past: " + job);

					// schedule next arrival reusing this Event object
					this.setTime(job.getRelDate());
					getShop().schedule(this);
				}

				// release "nextJob"
				if (nextJob != null) {
					nextJob.setSourceNum(jobsStarted++);
					getShop().startJob(nextJob);
				}

				nextJob = job;
			}

		};
		// schedule first arrival
		arriveEvent.setTime(getShop().simTime());
		getShop().schedule(arriveEvent);
	}

	public abstract Job createNextJob();

	/**
	 * Factory method used in {@link #createNextJob()} to create a new job
	 * instance.
	 */
	protected Job newJobInstance() {
		return new Job(getShop());
	}

	public JobShop getShop() {
		return shop;
	}

	public void setShop(JobShop shop) {
		this.shop = shop;
	}
}
