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
package jasima.core.statistics;

import static jasima.core.util.i18n.I18n.defFormat;

/**
 * A {@link SummaryStat} which weights values according to the time. As result
 * statistics the values returned by {@link #mean()}, {@link #min()}, and
 * {@link #max()} have defined values.
 * 
 * @author Torsten Hildebrandt
 */
public class TimeWeightedSummaryStat extends SummaryStat {

	private static final long serialVersionUID = -2192851354158356984L;

	private double lastTime;

	/**
	 * Constructs a new instance with {@code lastTime} and {@code lastValue}
	 * initialized with 0.
	 */
	public TimeWeightedSummaryStat() {
		super(); // calls clear() so lastTime is initialized properly
	}

	/**
	 * Copy constructor.
	 * 
	 * @param s The object to copy.
	 */
	public TimeWeightedSummaryStat(TimeWeightedSummaryStat s) {
		super(s);
		this.lastTime = s.lastTime;
	}

	@Override
	public void clear() {
		super.clear();
		lastTime = Double.NaN;
	}

	/**
	 * Adds a new {@code value}, weighted by the difference between {@code time} and
	 * {@link #lastTime()}. {@code time} is then saved for the next invocation of
	 * this method.
	 * 
	 * @param value Value of some state variable from the point in time given as the
	 *              second parameter.
	 * @param time  The point in time from which the current value is {@code value}
	 * @return {@code this} to allow easy chaining of calls.
	 */
	@Override
	public TimeWeightedSummaryStat value(double value, double time) {
		if (numObs() == 0) {
			super.value(value, 0.0);
		} else {
			if (time < lastTime())
				throw new IllegalArgumentException(
						defFormat("negative time span (lastTime=%f, time=%f).", lastTime(), time));
			super.value(lastValue, time - lastTime());
		}
		lastTime = time;
		lastValue = value;
		return this;
	}

	/**
	 * Don't use this method as it doesn't make sense for a
	 * {@code TimeWeightedSummaryStat}. Raises an
	 * {@link UnsupportedOperationException} when called.
	 * 
	 * @param v ignored
	 * @return TimeWeightedSummaryStat ignored
	 */
	@Override
	public TimeWeightedSummaryStat value(double v) {
		throw new UnsupportedOperationException("Use method TimeWeightedSummaryStat.value(double,double) instead.");
	}

	@Override
	public double min() {
		return Math.min(super.min(), lastValue);
	}

	@Override
	public double max() {
		return Math.max(super.max(), lastValue);
	}

	@Override
	public double variance() {
		return Double.NaN;
	}

	@Override
	public double variancePopulation() {
		return Double.NaN;
	}

	/**
	 * Returns the current value of the attribute {@code lastTime}.
	 * 
	 * @return The last point in time for which {@link #value(double, double)} was
	 *         called.
	 * @see #lastValue()
	 */
	public double lastTime() {
		return lastTime;
	}

	@Override
	public TimeWeightedSummaryStat clone() {
		return (TimeWeightedSummaryStat) super.clone();
	}

}
