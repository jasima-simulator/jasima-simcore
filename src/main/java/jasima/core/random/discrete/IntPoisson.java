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

import static jasima.core.util.i18n.I18n.defFormat;

import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.exception.NotPositiveException;

/**
 * This class implements a discrete number stream following a
 * <a href="http://en.wikipedia.org/wiki/Poisson_distribution">Poisson
 * distribution</a>. It is parameterized by its mean.
 * 
 * 
 * @author Torsten Hildebrandt
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Poisson_distribution">Poisson
 *      distribution (Wikipedia)</a>
 * @see <a href="http://mathworld.wolfram.com/PoissonDistribution.html">Poisson
 *      distribution (MathWorld)</a>
 */
public class IntPoisson extends IntDistribution {

	private static final long serialVersionUID = -3018032705267062035L;

	private PoissonDistribution dist;

	public IntPoisson() {
		this(1.0);
	}

	public IntPoisson(double mean) {
		super();
		setDistribution(new PoissonDistribution(mean));
	}

	@Override
	protected void setDistribution(IntegerDistribution distribution) {
		dist = (PoissonDistribution) distribution;
		super.setDistribution(distribution);
	}

	@Override
	public String toString() {
		return defFormat("IntPoisson(mean=%f)", getMean());
	}

	// ************* getter / setter below ****************

	public double getMean() {
		return dist.getMean();
	}

	/**
	 * The mean of the Poisson distribution.
	 * 
	 * @param mean The value to use.
	 * @throws NotPositiveException If {@code mean} was {@code <=0}.
	 */
	public void setMean(double mean) throws NotPositiveException {
		setDistribution(new PoissonDistribution(mean));
	}

}
