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
package jasima.core.random.continuous;

import static jasima.core.util.i18n.I18n.defFormat;
import static java.lang.Double.isNaN;

import jasima.core.util.Pair;

/**
 * Takes a version of a base stream, where minimum and maximum values are fixed.
 * This is done by returning
 * <ol>
 * <li>{@code minValue} whenever the {@code baseStream} returns a smaller value,
 * <li>{@code maxValue}, if {@code baseStream} returns a larger value
 * <li>otherwise the {@code baseStream}'s value is returned as is.
 * </ol>
 * This method is sometimes called cut-off truncation. Technically this creates
 * a new distribution with a (cumulative) distribution function with a value of
 * 0 for all values smaller than {@code minValue}, then following the
 * distribution function of {@code baseStream} for the interval [
 * {@code minValue} , {@code maxValue}], and having the value 1 for all values
 * larger than {@code maxValue}.
 * <p>
 * See also <a href=
 * "https://sites.google.com/site/100mostfrequenterrors/list/error003">Wolfgang
 * Bziuk: Generating random variates from truncated distributions</a> (last
 * accessed 2020-10-26).
 * 
 * @author Torsten Hildebrandt
 */
public class DblTruncatedSimple extends DblSequence {

	// TODO: implement scaled truncation in a separate class

	private static final long serialVersionUID = -3224445720493038341L;

	private DblSequence baseStream;
	private double minValue;
	private double maxValue;

	public DblTruncatedSimple() {
		this(null, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	public DblTruncatedSimple(DblSequence baseStream, double min, double max) {
		super();
		setBaseStream(baseStream);
		setMinValue(min);
		setMaxValue(max);
	}

	@Override
	public void init() {
		if (!isValidRange(getMinValue(), getMaxValue()))
			throw new IllegalArgumentException();

		super.init();

		if (baseStream.getRndGen() == null) {
			baseStream.setRndGen(getRndGen());
		}
		baseStream.init();
	}

	@Override
	public double nextDbl() {
		return valueInRange(baseStream.nextDbl(), getMinValue(), getMaxValue());
	}

	@Override
	public double getNumericalMean() {
		// TODO implement me, requires access to baseStream's distribution
		// function
		throw new UnsupportedOperationException();
	}

	@Override
	public Pair<Double, Double> getValueRange() {
		return new Pair<Double, Double>(minValue, maxValue);
	}

	@Override
	public DblSequence clone() {
		DblTruncatedSimple c = (DblTruncatedSimple) super.clone();

		if (baseStream != null)
			c.baseStream = baseStream.clone();

		return c;
	}

	@Override
	public String toString() {
		return defFormat("DblTruncatedSimple(baseStream=%s;minValue=%f;maxValue=%f)", getBaseStream(), getMinValue(),
				getMaxValue());
	}

	// ******************* getters / setters below **********************

	public DblSequence getBaseStream() {
		return baseStream;
	}

	/**
	 * Sets the stream to be truncated.
	 * 
	 * @param baseStream The stream to truncate.
	 */
	public void setBaseStream(DblSequence baseStream) {
		this.baseStream = baseStream;
	}

	public double getMinValue() {
		return minValue;
	}

	/**
	 * Sets the minimum value.
	 * 
	 * @param minValue The lower bound of allowed values.
	 */
	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}

	public double getMaxValue() {
		return maxValue;
	}

	/**
	 * Sets the maximum allowed value.
	 * 
	 * @param maxValue The upper bound of allowed values.
	 */
	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

	// static methods

	/**
	 * Static method to generate a truncated value in the range
	 * minValue<=baseValue<=maxValue. Assign NaN to minValue or maxValue if you
	 * don't want to have a lower / upper bound.
	 * 
	 * @param baseValue The value to check for falling in the desired interval.
	 * @param minValue
	 * @param maxValue
	 * @return the truncated value.
	 */
	public static double valueInRange(double baseValue, double minValue, double maxValue) {
		if (!isValidRange(minValue, maxValue)) {
			throw new IllegalArgumentException();
		}

		if (baseValue < minValue)
			baseValue = minValue;
		if (baseValue > maxValue)
			baseValue = maxValue;
		return baseValue;
	}

	private static boolean isValidRange(double min, double max) {
		return (min <= max) ? true : (isNaN(min) || isNaN(max));
	}

}
