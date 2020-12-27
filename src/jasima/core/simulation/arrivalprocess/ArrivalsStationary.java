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
package jasima.core.simulation.arrivalprocess;

import java.util.Random;

import jasima.core.random.continuous.DblDistribution;
import jasima.core.random.continuous.DblSequence;

/**
 * This class can be used to create a stationary arrival process which can be
 * described by a certain sequence of inter-arrival times (using, e.g.,
 * {@link DblDistribution}).
 * 
 * @see DblSequence
 * 
 * @author Torsten Hildebrandt, 2012-08-07
 */
public class ArrivalsStationary extends ArrivalProcess {

	private static final long serialVersionUID = -7877781395872395477L;

	private DblSequence interArrivalTimes = null;

	public ArrivalsStationary() {
		this(null);
	}

	public ArrivalsStationary(DblSequence interArrivalTimes) {
		super();
		setInterArrivalTimes(interArrivalTimes);
	}

	@Override
	public void init() {
		super.init();

		if (interArrivalTimes != null)
			interArrivalTimes.init();
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
	public void setRndGen(Random rndGen) {
		super.setRndGen(rndGen);
		if (interArrivalTimes != null)
			interArrivalTimes.setRndGen(rndGen);
	}

	@Override
	public DblSequence clone() {
		ArrivalsStationary c = (ArrivalsStationary) super.clone();
		if (interArrivalTimes != null)
			c.interArrivalTimes = interArrivalTimes.clone();
		return c;
	}

	public DblSequence getInterArrivalTimes() {
		return interArrivalTimes;
	}

	public void setInterArrivalTimes(DblSequence interArrivalTimes) {
		this.interArrivalTimes = interArrivalTimes;
	}

}
