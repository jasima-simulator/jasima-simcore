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

import jasima.core.random.continuous.DblStream;

/**
 * Base class for arrival processes.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>, 2012-08-07
 * @version "$Id$"
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
