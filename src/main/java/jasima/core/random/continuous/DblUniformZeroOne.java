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

import java.util.Random;

import jasima.core.util.Pair;

/**
 * Generates uniformly distributed doubles in the range [0,1.0). This class just
 * delegates to the underlying method {@link java.util.Random#nextDouble()}.
 * 
 * @author Torsten Hildebrandt
 */
public class DblUniformZeroOne extends DblSequence {

	private static final long serialVersionUID = -5917490656405705668L;

	public DblUniformZeroOne() {
		this(null);
	}

	public DblUniformZeroOne(Random random) {
		super();
		setRndGen(random);
	}

	@Override
	public double nextDbl() {
		return rndGen.nextDouble();
	}

	@Override
	public double getNumericalMean() {
		return 0.5;
	}

	@Override
	public Pair<Double, Double> getValueRange() {
		return new Pair<>(0.0, 1.0);
	}

	@Override
	public String toString() {
		return "DblUniformZeroOne";
	}

}
