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

import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;

/**
 * Implements a number stream with values following a
 * <a href="http://en.wikipedia.org/wiki/Beta_distribution">Beta
 * distribution</a>. A beta distribution is characterized by the two positive
 * shape parameters {@code alpha} and {@code beta}.
 * 
 * @author Torsten Hildebrandt
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Beta_distribution">Beta
 *      distribution (Wikipedia)</a>
 * @see <a href="http://mathworld.wolfram.com/BetaDistribution.html">Beta
 *      distribution (MathWorld)</a>
 */
public class DblBeta extends DblDistribution {

	private static final long serialVersionUID = -5717640931580338541L;

	private BetaDistribution dist;

	public DblBeta() {
		this(2.0, 5.0);
	}

	public DblBeta(double alpha, double beta) {
		super();
		setDistribution(new BetaDistribution(alpha, beta));
	}

	@Override
	protected void setDistribution(RealDistribution distribution) {
		dist = (BetaDistribution) distribution;
		super.setDistribution(distribution);
	}

	@Override
	public String toString() {
		return defFormat("DblBeta(alpha=%f;beta=%f)", getAlpha(), getBeta());
	}

	public double getAlpha() {
		return dist.getAlpha();
	}

	/**
	 * Sets the parameter value for the distribution's shape parameter
	 * {@code alpha}.
	 * 
	 * @param alpha The alpha value to use.
	 * @throws NotStrictlyPositiveException If {@code alpha} was {@code <=0}.
	 */
	public void setAlpha(double alpha) throws NotStrictlyPositiveException {
		setDistribution(new BetaDistribution(alpha, getBeta()));
	}

	public double getBeta() {
		return dist.getBeta();
	}

	/**
	 * Sets the shape parameter {@code beta} of the distribution.
	 * 
	 * @param beta The {@code beta} parameter value to use.
	 * @throws NotStrictlyPositiveException If {@code beta} was {@code <=0}.
	 */
	public void setBeta(double beta) throws NotStrictlyPositiveException {
		setDistribution(new BetaDistribution(getAlpha(), beta));
	}

}
