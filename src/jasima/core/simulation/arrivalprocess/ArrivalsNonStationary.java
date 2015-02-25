/*******************************************************************************
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This file is part of jasima, v1.2.
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

import jasima.core.random.continuous.DblStream;

import java.util.Random;

import org.apache.commons.math3.analysis.UnivariateFunction;

/**
 * <p>
 * This class can be used to create a non-stationary arrival process, i.e., if
 * (inter-)arrivals do not follow a single distribution. To do so, a DblStream
 * with mean of 1 (e.g., an exponential distribution with rate 1) and the
 * inverse cumulative expectation function have to be provided.
 * </p>
 * <p>
 * For details see, e.g., Law: "Simulation Modelling and Analysis", how to
 * create non-stationary (Poisson) processes.
 * </p>
 * 
 * @author Torsten Hildebrandt, 2012-08-07
 * @version 
 *          "$Id$"
 */
public class ArrivalsNonStationary extends ArrivalProcess {

	private static final long serialVersionUID = -4530103049458748815L;

	private DblStream unitMeanDblStream;
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
	}

	@Override
	public DblStream clone() throws CloneNotSupportedException {
		ArrivalsNonStationary c = (ArrivalsNonStationary) super.clone();
		if (unitMeanDblStream != null)
			c.unitMeanDblStream = unitMeanDblStream.clone();

		return c;
	}

	public DblStream getUnitMeanDblStream() {
		return unitMeanDblStream;
	}

	/**
	 * Sets the DblStream to use. This stream has to produce values with a mean
	 * of 1.
	 * 
	 * @param unitMeanDblStream
	 */
	public void setUnitMeanDblStream(DblStream unitMeanDblStream) {
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
	public void setInverseCumulativeExpectation(
			UnivariateFunction inverseCumulativeExpectation) {
		this.inverseCumulativeExpectation = inverseCumulativeExpectation;
	}

	@Override
	public void setRndGen(Random rndGen) {
		super.setRndGen(rndGen);

		if (unitMeanDblStream != null)
			unitMeanDblStream.setRndGen(rndGen);
	}

}
