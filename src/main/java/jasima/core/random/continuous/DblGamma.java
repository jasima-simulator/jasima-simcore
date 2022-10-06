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

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;

/**
 * Implements a stream of real numbers where each value follows a
 * <a href="http://en.wikipedia.org/wiki/Gamma_distribution">Gamma
 * distribution</a>. The distribution is characterized by a shape and a scale
 * parameter. It is a generalization of the Exponential and Erlang
 * distributions.
 * 
 * @author Torsten Hildebrandt
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Gamma_distribution">Gamma
 *      distribution (Wikipedia)</a>
 * @see <a href="http://mathworld.wolfram.com/GammaDistribution.html">Gamma
 *      distribution (MathWorld)</a>
 */
public class DblGamma extends DblDistribution {

	private static final long serialVersionUID = 8196438918640991723L;

	private GammaDistribution dist;

	public DblGamma() {
		this(2.0, 2.0);
	}

	public DblGamma(double shape, double scale) {
		super();
		setDistribution(new GammaDistribution(shape, scale));
	}

	@Override
	protected void setDistribution(RealDistribution distribution) {
		dist = (GammaDistribution) distribution;
		super.setDistribution(distribution);
	}

	@Override
	public String toString() {
		return defFormat("DblGamma(shape=%f;scale=%f)", getShape(), getScale());
	}

	public double getShape() {
		return dist.getShape();
	}

	/**
	 * Sets the shape parameter for this distribution.
	 * 
	 * @param shape The shape parameter to use.
	 * @throws NotStrictlyPositiveException If the parameter value was
	 *                                      {@code <=0.0}.
	 */
	public void setShape(double shape) throws NotStrictlyPositiveException {
		setDistribution(new GammaDistribution(shape, dist.getScale()));
	}

	public double getScale() {
		return dist.getScale();
	}

	/**
	 * Sets the scale parameter for this distribution.
	 * 
	 * @param scale The scale parameter to use.
	 * @throws NotStrictlyPositiveException If the parameter value was
	 *                                      {@code <=0.0}.
	 */
	public void setScale(double scale) throws NotStrictlyPositiveException {
		setDistribution(new GammaDistribution(dist.getShape(), scale));
	}

}
