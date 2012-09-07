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
package jasima.core.simulation.arrivalprocess;

import jasima.core.random.continuous.DblDistribution;
import jasima.core.random.continuous.DblStream;

/**
 * This class can be used to create a stationary arrival process which can be
 * described by a certain sequence of inter-arrival times (using, e.g.,
 * {@link DblDistribution}).
 * 
 * @see DblStream
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>, 2012-08-07
 * @version $Id$
 */
public class ArrivalsStationary extends ArrivalProcess {

	private DblStream interArrivalTimes = null;

	public ArrivalsStationary() {
		this(null);
	}

	public ArrivalsStationary(DblStream interArrivalTimes) {
		super();
		setInterArrivalTimes(interArrivalTimes);
	}

	@Override
	public double nextDbl() {
		if (isFirst && isArrivalAtTimeZero()) {
			// state = state; // do nothing
		} else {
			state = state + interArrivalTimes.nextDbl();
		}
		isFirst = false;
		return state;
	}

	@Override
	public void init() {
		super.init();

		interArrivalTimes.setRndGen(rndGen);
	}

	@Override
	public DblStream clone() throws CloneNotSupportedException {
		ArrivalsStationary c = (ArrivalsStationary) super.clone();
		if (interArrivalTimes != null)
			c.interArrivalTimes = interArrivalTimes.clone();
		return c;
	}

	public DblStream getInterArrivalTimes() {
		return interArrivalTimes;
	}

	public void setInterArrivalTimes(DblStream interArrivalTimes) {
		this.interArrivalTimes = interArrivalTimes;
	}

}
