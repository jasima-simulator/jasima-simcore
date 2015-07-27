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

import jasima.core.util.Pair;
import jasima.core.util.Util;

import java.util.Arrays;

/**
 * Creates a number stream that is the sum of a given set of base streams.
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 */
public class DblSumStream extends DblStream {

	private static final long serialVersionUID = -1978211297841470708L;

	private DblStream[] subStreams;

	public DblSumStream() {
		this((DblStream[]) null);
	}

	public DblSumStream(DblStream... subStreams) {
		super();
		this.subStreams = subStreams;
	}

	@Override
	public void init() {
		super.init();

		for (DblStream s : subStreams) {
			if (s.getRndGen() == null)
				s.setRndGen(getRndGen());
			s.init();
		}
	}

	@Override
	public double nextDbl() {
		double sum = subStreams[0].nextDbl();
		for (int i = 1, n = subStreams.length; i < n; i++) {
			sum += subStreams[i].nextDbl();
		}
		return sum;
	}

	@Override
	public double getNumericalMean() {
		if (subStreams == null || subStreams.length == 0)
			return Double.NaN;

		double sum = subStreams[0].getNumericalMean();
		for (int i = 1, n = subStreams.length; i < n; i++) {
			sum += subStreams[i].getNumericalMean();
		}
		return sum;
	}

	@Override
	public Pair<Double, Double> getValueRange() {
		if (subStreams == null || subStreams.length == 0)
			return new Pair<>(Double.NaN, Double.NaN);

		Pair<Double, Double> r0 = subStreams[0].getValueRange();
		double min = r0.a;
		double max = r0.b;

		for (int i = 1, n = subStreams.length; i < n; i++) {
			Pair<Double, Double> range = subStreams[i].getValueRange();

			min += range.a;
			max += range.b;
		}

		return new Pair<>(min, max);
	}

	@Override
	public String toString() {
		return String.format(Util.DEF_LOCALE, "DblSumStream(subStreams=%s)",
				Arrays.deepToString(getSubStreams()));
	}

	// ************* getter / setter below ****************

	public DblStream[] getSubStreams() {
		return subStreams;
	}

	/**
	 * Sets the sub-streams to compute the values of this number stream.
	 * 
	 * @param subStreams
	 *            The sub-streams to use.
	 */
	public void setSubStreams(DblStream... subStreams) {
		this.subStreams = subStreams;
	}

}
