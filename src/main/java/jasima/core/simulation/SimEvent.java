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
package jasima.core.simulation;

import jasima.core.simulation.util.SimOperations.SimEventType;

/**
 * Abstract base class for all simulation events. Events are sequenced by an
 * {@link EventQueue} according to their time, priority and event number (to
 * enforce FIFO order of concurrent events with the same priority).
 * 
 * @author Torsten Hildebrandt
 */
public abstract class SimEvent implements Comparable<SimEvent>, Runnable {

	private static final int PRIO_INCREMENT = Integer.MAX_VALUE / 4;

	// some constants for event priorities. You shouldn't use MAX and MIN in
	// custom code
	public static final int EVENT_PRIO_NORMAL = 0;
	public static final int EVENT_PRIO_LOW = 1 * PRIO_INCREMENT;
	public static final int EVENT_PRIO_LOWER = 2 * PRIO_INCREMENT;
	public static final int EVENT_PRIO_LOWEST = 3 * PRIO_INCREMENT;
	public static final int EVENT_PRIO_HIGH = -1 * PRIO_INCREMENT;
	public static final int EVENT_PRIO_HIGHER = -2 * PRIO_INCREMENT;
	public static final int EVENT_PRIO_HIGHEST = -3 * PRIO_INCREMENT;
	public static final int EVENT_PRIO_MAX = Integer.MIN_VALUE;
	public static final int EVENT_PRIO_MIN = Integer.MAX_VALUE;

	private double time;
	private int prio;
	private String description;
	int eventNum;

	public SimEvent(double time, int prio) {
		this(time, prio, null);
	}

	public SimEvent(double time, int prio, String description) {
		super();
		this.time = time;
		this.prio = prio;
		this.description = description;
	}

	@Override
	public final void run() {
		handle();
	}

	/**
	 * Override this method to implement the actual functionality.
	 */
	public abstract void handle();

	public void setTime(double time) {
		this.time = time;
	}

	public double getTime() {
		return time;
	}

	public int getPrio() {
		return prio;
	}

	public void setPrio(int newPrio) {
		this.prio = newPrio;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String newDescription) {
		this.description = newDescription;
	}

	/**
	 * An application event is the usual event type in a simulation. A simulation is
	 * terminated if the event queue is empty or does not contain any application
	 * events (i.e. isAppEvent() of all events in the queue returns false).
	 */
	public boolean isAppEvent() {
		return true;
	}

	public SimEventType eventType() {
		return isAppEvent() ? SimEventType.APP_EVENT : SimEventType.UTILITY_EVENT;
	}

	@Override
	public final int compareTo(SimEvent o) {
		if (time < o.time)
			return -1;
		else if (time > o.time)
			return +1;
		else {
			// time is the same, prio is secondary criterion
			if (prio < o.prio)
				return -1;
			else if (prio > o.prio)
				return +1;
			else {
				// eventNum is ternary criterion to enforce FIFO processing
				// should both time and prio should be the same.
				return (eventNum < o.eventNum) ? -1 : ((eventNum > o.eventNum) ? +1 : 0);
			}
		}
	}

	@Override
	public String toString() {
		String descr = getDescription();
		return descr != null ? descr : super.toString();
	}
}