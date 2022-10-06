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

import jasima.core.random.continuous.DblSequence;
import jasima.core.util.Pair;

/**
 * Base class for arrival processes, i.e., calculating absolute arrival times.
 * 
 * @author Torsten Hildebrandt, 2012-08-07
 */
public abstract class ArrivalProcess extends DblSequence {

	private static final long serialVersionUID = 99456277227340206L;

	protected double state;
	protected boolean isFirst;

	private double initialState = 0.0;
	private boolean arrivalAtTimeZero = false;

	@Override
	public void init() {
		state = getInitialState();
		isFirst = true;
	}

	public abstract double nextDbl();

	/**
	 * A mean value for an arrival process is usually undefined, therefore this
	 * method throws an {@link UnsupportedOperationException}.
	 */
	@Override
	public double getNumericalMean() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Pair<Double, Double> getValueRange() {
		return new Pair<>(getInitialState(), Double.POSITIVE_INFINITY);
	}

	public double getInitialState() {
		return initialState;
	}

	public void setInitialState(double initialState) {
		this.initialState = initialState;
	}

	public boolean isArrivalAtTimeZero() {
		return arrivalAtTimeZero;
	}

	public void setArrivalAtTimeZero(boolean arrivalAtTimeZero) {
		this.arrivalAtTimeZero = arrivalAtTimeZero;
	}

}
