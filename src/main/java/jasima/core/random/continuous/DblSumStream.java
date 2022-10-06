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

import java.util.Arrays;

import jasima.core.util.Pair;

/**
 * Creates a number stream that is the sum of a given set of base streams.
 * 
 * @author Torsten Hildebrandt
 */
public class DblSumStream extends DblSequence {

	private static final long serialVersionUID = -1978211297841470708L;

	private DblSequence[] subStreams;

	public DblSumStream() {
		this((DblSequence[]) null);
	}

	public DblSumStream(DblSequence... subStreams) {
		super();
		this.subStreams = subStreams;
	}

	@Override
	public void init() {
		super.init();

		for (DblSequence s : subStreams) {
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
		return defFormat("DblSumStream(subStreams=%s)", Arrays.deepToString(getSubStreams()));
	}

	// ************* getter / setter below ****************

	public DblSequence[] getSubStreams() {
		return subStreams;
	}

	/**
	 * Sets the sub-streams to compute the values of this number stream.
	 * 
	 * @param subStreams The sub-streams to use.
	 */
	public void setSubStreams(DblSequence... subStreams) {
		this.subStreams = subStreams;
	}

}
