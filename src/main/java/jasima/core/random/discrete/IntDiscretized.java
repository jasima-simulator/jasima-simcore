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

import jasima.core.random.continuous.DblSequence;
import jasima.core.util.Pair;

/**
 * Turns an arbitrary {@link DblSequence} into an {@link IntSequence} by returning
 * its values rounded to the closest integer.
 * 
 * @author Torsten Hildebrandt
 */
public class IntDiscretized extends IntSequence {

	private static final long serialVersionUID = -5846263470745133816L;

	private DblSequence baseStream;

	public IntDiscretized() {
		this(null);
	}

	public IntDiscretized(DblSequence baseStream) {
		super();

		setBaseStream(baseStream);
	}

	@Override
	public void init() {
		super.init();

		if (baseStream != null)
			baseStream.init();
	}

	@Override
	public int nextInt() {
		return roundToClosestInt(baseStream.nextDbl());
	}

	@Override
	public double getNumericalMean() {
		return roundToClosestInt(baseStream.nextDbl());
	}

	@Override
	public Pair<Double, Double> getValueRange() {
		Pair<Double, Double> r = baseStream.getValueRange();
		return new Pair<Double, Double>((double) roundToClosestInt(r.a), (double) roundToClosestInt(r.b));
	}

	@Override
	public void setRndGen(Random rndGen) {
		super.setRndGen(rndGen);

		if (baseStream != null)
			baseStream.setRndGen(rndGen);
	}

	private static int roundToClosestInt(double v) {
		return (int) Math.round(v);
	}

	public IntDiscretized clone() {
		IntDiscretized c = (IntDiscretized) super.clone();

		if (baseStream != null) {
			c.baseStream = baseStream.clone();
		}

		return c;
	}

	@Override
	public String toString() {
		return "IntDiscretized(baseStream=" + String.valueOf(getBaseStream()) + ")";
	}

	// getter/setter below

	public DblSequence getBaseStream() {
		return baseStream;
	}

	public void setBaseStream(DblSequence baseStream) {
		this.baseStream = baseStream;

		if (getRndGen() != null) {
			baseStream.setRndGen(getRndGen());
		}
	}

}
