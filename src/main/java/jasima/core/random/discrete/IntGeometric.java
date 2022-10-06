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

import org.apache.commons.math3.distribution.GeometricDistribution;
import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.exception.OutOfRangeException;

/**
 * This class implements a discrete number stream following a
 * <a href="http://en.wikipedia.org/wiki/Geometric_distribution">Geometric
 * distribution</a>. It has a single parameter, the probability of success in a
 * single trial.
 * 
 * 
 * @author Torsten Hildebrandt
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Geometric_distribution">Geometric
 *      distribution (Wikipedia)</a>
 * @see <a href="http://mathworld.wolfram.com/GeometricDistribution.html">
 *      Geometric distribution (MathWorld)</a>
 */
public class IntGeometric extends IntDistribution {

	private static final long serialVersionUID = 3026113171560620917L;

	private GeometricDistribution dist;

	public IntGeometric() {
		this(0.5);
	}

	public IntGeometric(double probOfSuccess) {
		super();
		setDistribution(new GeometricDistribution(probOfSuccess));
	}

	@Override
	protected void setDistribution(IntegerDistribution distribution) {
		dist = (GeometricDistribution) distribution;
		super.setDistribution(distribution);
	}

	@Override
	public String toString() {
		return defFormat("IntGeometric(probabilityOfSuccess=%f)", getProbabilityOfSuccess());
	}

	public double getProbabilityOfSuccess() {
		return dist.getProbabilityOfSuccess();
	}

	/**
	 * Sets the probability of success in a single trial.
	 * 
	 * @param probOfSuccess The success probability.
	 * @throws OutOfRangeException If the supplied probability if not in the
	 *                             interval {@code (0,1]}.
	 */
	public void setProbabilityOfSuccess(double probOfSuccess) throws OutOfRangeException {
		setDistribution(new GeometricDistribution(probOfSuccess));
	}

}
