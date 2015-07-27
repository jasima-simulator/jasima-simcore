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

import jasima.core.random.continuous.DblStream;
import jasima.core.util.Pair;

/**
 * Base class for arrival processes, i.e., calculating absolute arrival times.
 * 
 * @author Torsten Hildebrandt, 2012-08-07
 * @version 
 *          "$Id$"
 */
public abstract class ArrivalProcess extends DblStream {

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
