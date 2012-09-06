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
package jasima.core.simulation;

public abstract class Event implements Comparable<Event> {

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
	private final int prio;
	int eventNum;

	public Event(double time, int prio) {
		super();
		this.time = time;
		this.prio = prio;
	}

	public abstract void handle();

	public void setTime(double time) {
		this.time = time;
	}

	public double getTime() {
		return time;
	}

	@Override
	public final int compareTo(Event o) {
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
				if (eventNum < o.eventNum)
					return -1;
				else if (eventNum > o.eventNum)
					return +1;
				else
					return 0;
			}
		}
	}

	public int getPrio() {
		return prio;
	}

	/**
	 * An application event is the usual event type in a simulation. A
	 * simulation is terminated if the event queue is empty or does not contain
	 * any application events (i.e. isAppEvent() of all events in the queue
	 * returns false).
	 */
	public boolean isAppEvent() {
		return true;
	}
}