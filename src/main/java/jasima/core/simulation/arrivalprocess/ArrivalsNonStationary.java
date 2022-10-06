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

import org.apache.commons.math3.analysis.UnivariateFunction;

import jasima.core.random.continuous.DblSequence;

/**
 * This class can be used to create a non-stationary arrival process, i.e., if
 * (inter-)arrivals do not follow a single distribution. To do so, a DblStream
 * with mean of 1 (e.g., an exponential distribution with rate 1) and the
 * inverse cumulative expectation function have to be provided.
 * <p>
 * For details see, e.g., Law: "Simulation Modelling and Analysis", how to
 * create non-stationary (Poisson) processes.
 * 
 * @author Torsten Hildebrandt, 2012-08-07
 */
public class ArrivalsNonStationary extends ArrivalProcess {

	private static final long serialVersionUID = -4530103049458748815L;

	private DblSequence unitMeanDblStream;
	private UnivariateFunction inverseCumulativeExpectation;

	public ArrivalsNonStationary() {
		super();
	}

	@Override
	public double nextDbl() {
		if (isFirst && isArrivalAtTimeZero()) {
			// state = state; // do nothing
		} else {
			state = state + unitMeanDblStream.nextDbl();
		}
		isFirst = false;

		return inverseCumulativeExpectation.value(state);
	}

	@Override
	public void init() {
		super.init();

		unitMeanDblStream.setRndGen(rndGen);
		unitMeanDblStream.init();
	}

	@Override
	public DblSequence clone() {
		ArrivalsNonStationary c = (ArrivalsNonStationary) super.clone();
		if (unitMeanDblStream != null)
			c.unitMeanDblStream = unitMeanDblStream.clone();

		return c;
	}

	public DblSequence getUnitMeanDblStream() {
		return unitMeanDblStream;
	}

	/**
	 * Sets the DblStream to use. This stream has to produce values with a mean of
	 * 1.
	 * 
	 * @param unitMeanDblStream
	 */
	public void setUnitMeanDblStream(DblSequence unitMeanDblStream) {
		this.unitMeanDblStream = unitMeanDblStream;
	}

	public UnivariateFunction getInverseCumulativeExpectation() {
		return inverseCumulativeExpectation;
	}

	/**
	 * Sets the inverse cumulative expectation function. This function has to be
	 * strictly monotonically increasing.
	 * 
	 * @param inverseCumulativeExpectation
	 */
	public void setInverseCumulativeExpectation(UnivariateFunction inverseCumulativeExpectation) {
		this.inverseCumulativeExpectation = inverseCumulativeExpectation;
	}

	@Override
	public void setRndGen(Random rndGen) {
		super.setRndGen(rndGen);

		if (unitMeanDblStream != null)
			unitMeanDblStream.setRndGen(rndGen);
	}

}
