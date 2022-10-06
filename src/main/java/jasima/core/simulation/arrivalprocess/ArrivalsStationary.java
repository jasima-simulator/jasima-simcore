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
