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

	private double lastTime = 0.0d;

	private final double initialTime;
	private final double initialValue;

	/**
	 * Constructs a new instance with {@code lastTime} and {@code lastValue}
	 * initialized with 0.
	 */
	public TimeWeightedSummaryStat() {
		this(0.0d, 0.0d);
	}

	/**
	 * Constructs a new instance initializing {@code lastTime} with the given
	 * parameter {@code initialTime}.
	 * 
	 * @param initialValue Initial value of the underlying state variable at time
	 *                     {@code initialTime}.
	 * @param initialTime  The first point in time to consider.
	 */
	public TimeWeightedSummaryStat(double initialValue, double initialTime) {
		super();
		this.initialTime = initialTime;
		this.initialValue = initialValue;
		clear();
	}

	/**
	 * Copy constructor.
	 * 
	 * @param s The object to copy.
	 */
	public TimeWeightedSummaryStat(TimeWeightedSummaryStat s) {
		super(s);
		this.initialTime = s.initialTime;
		this.initialValue = s.initialValue;
	}

	@Override
	public void clear() {
		super.clear();

		lastTime = initialTime;
		lastValue = initialValue;
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
		if (time < lastTime())
			throw new IllegalArgumentException(
					defFormat("negative time span (lastTime=%f, time=%f).", lastTime(), time));
		super.value(lastValue, time - lastTime());

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

}
