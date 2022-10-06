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
