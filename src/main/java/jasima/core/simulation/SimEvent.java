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
package jasima.core.simulation;

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