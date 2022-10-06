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
package jasima.core.simulation.util;

import java.time.Duration;

import jasima.core.simulation.SimEvent;
import jasima.core.simulation.Simulation;

public class TimeSynchronizer {

	public static final int NUM_SYNC_EVENTS_PER_REAL_SECOND = 25;
	public static final long REAL_TIME_BETWEEN_SYNC_MILLIS = Math.round(1000.0 / NUM_SYNC_EVENTS_PER_REAL_SECOND);

	private final Simulation sim;
	private double realTimeFactor;

	private long eventCounter;
	private long framesDropped;
	private double simTimeBetweenSync;

	private long tStartReal;
	private double tStartSim;

	private SimEvent syncEvent;

	public TimeSynchronizer(Simulation sim, double realTimeFactor) {
		super();

		this.sim = sim;
		this.setRealTimeFactor(realTimeFactor);

		init();

		syncEvent = new SimEvent(tStartSim, SimEvent.EVENT_PRIO_HIGHEST, "sync_event") {
			@Override
			public void handle() {
				// sync with real time
				syncTime();

				// schedule next sync event
				eventCounter++;

				double tNew = tStartSim + eventCounter * simTimeBetweenSync;
				setTime(Math.max(tNew, sim.simTime()));
				sim.schedule(this);
			}

			@Override
			public boolean isAppEvent() {
				return false;
			}
		};
		sim.schedule(syncEvent);

		sim.valueStorePut("TimeSynchronizer", this);
	}

	private void init() {
		eventCounter = framesDropped = 0;
		tStartReal = System.currentTimeMillis();
		tStartSim = sim.simTime();
		simTimeBetweenSync = sim.toSimTime(Duration.ofMillis(REAL_TIME_BETWEEN_SYNC_MILLIS)) * realTimeFactor;
	}

	protected void syncTime() {
		double realTimeDue = tStartReal + eventCounter * REAL_TIME_BETWEEN_SYNC_MILLIS;
		double timeDiff = realTimeDue - System.currentTimeMillis();
//		System.out.println(System.currentTimeMillis()+"\t"+sim.simTime()+"\t"+realTimeDue+"\t"+timeDiff+"\t"+framesDropped);
		if (timeDiff > 0) {
			try {
				Thread.sleep(Math.round(timeDiff));
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		} else if (timeDiff < 0) {
			framesDropped++;
		}
	}

	public double getRealTimeFactor() {
		return realTimeFactor;
	}

	public void setRealTimeFactor(double realTimeFactor) {
		this.realTimeFactor = realTimeFactor;
		init();
	}

}
