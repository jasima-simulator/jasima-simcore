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
package jasima.core.random.continuous;

import static jasima.core.util.i18n.I18n.defFormat;

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
public class DblTruncatedSimple extends DblStream {

	// TODO: implement scaled truncation in a separate class

	private static final long serialVersionUID = -3224445720493038341L;

	private DblStream baseStream;
	private double minValue;
	private double maxValue;

	public DblTruncatedSimple() {
		this(null, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	public DblTruncatedSimple(DblStream baseStream, double min, double max) {
		super();
		setBaseStream(baseStream);
		setMinValue(min);
		setMaxValue(max);
	}

	@Override
	public void init() {
		if (minValue > maxValue)
			throw new IllegalArgumentException();

		super.init();

		if (baseStream.getRndGen() == null) {
			baseStream.setRndGen(getRndGen());
		}
		baseStream.init();
	}

	@Override
	public double nextDbl() {
		double baseValue = baseStream.nextDbl();
		if (baseValue < minValue)
			baseValue = minValue;
		if (baseValue > maxValue)
			baseValue = maxValue;
		return baseValue;
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
	public DblStream clone() {
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

	public DblStream getBaseStream() {
		return baseStream;
	}

	/**
	 * Sets the stream to be truncated.
	 * 
	 * @param baseStream The stream to truncate.
	 */
	public void setBaseStream(DblStream baseStream) {
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

}
