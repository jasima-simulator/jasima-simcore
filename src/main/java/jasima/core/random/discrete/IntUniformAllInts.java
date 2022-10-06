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
package jasima.core.random.discrete;

import java.util.Random;

import jasima.core.util.Pair;

/**
 * Generates uniformly distributed integers for the complete set of integer
 * values. This class just delegates to the underlying method
 * {@link java.util.Random#nextInt()}.
 * 
 * @author Torsten Hildebrandt
 */
public class IntUniformAllInts extends IntSequence {

	private static final long serialVersionUID = -2235431750818257710L;

	public IntUniformAllInts() {
		this(null);
	}

	public IntUniformAllInts(Random random) {
		super();
		setRndGen(random);
	}

	@Override
	public int nextInt() {
		return rndGen.nextInt();
	}

	@Override
	public double getNumericalMean() {
		return (((long) Integer.MAX_VALUE) + Integer.MIN_VALUE) / 2.0;
	}

	@Override
	public String toString() {
		return "IntUniformAllInts";
	}

	@Override
	public Pair<Double, Double> getValueRange() {
		return new Pair<>((double) Integer.MIN_VALUE, (double) Integer.MAX_VALUE);
	}

}