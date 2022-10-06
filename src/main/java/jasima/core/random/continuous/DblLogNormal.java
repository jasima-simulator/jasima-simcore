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

import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;

/**
 * Implements a number stream with values following a
 * <a href="http://en.wikipedia.org/wiki/Log-normal_distribution">log-normal
 * distribution</a>.
 * 
 * @author Torsten Hildebrandt
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Log-normal_distribution">Log-
 *      normal distribution (Wikipedia)</a>
 * @see <a href="http://mathworld.wolfram.com/LogNormalDistribution.html">Log-
 *      normal distribution (MathWorld)</a>
 */
public class DblLogNormal extends DblDistribution {

	private static final long serialVersionUID = 4616829343071759004L;

	private LogNormalDistribution dist;

	public DblLogNormal() {
		this(0.0, 1.0);
	}

	public DblLogNormal(double scale, double shape) {
		super();
		setDistribution(new LogNormalDistribution(scale, shape));
	}

	@Override
	protected void setDistribution(RealDistribution distribution) {
		dist = (LogNormalDistribution) distribution;
		super.setDistribution(distribution);
	}

	@Override
	public String toString() {
		return defFormat("DblLogNormal(shape=%f;scale=%f)", getShape(), getScale());
	}

	public double getScale() {
		return dist.getScale();
	}

	/**
	 * Sets the scale parameter to use.
	 * 
	 * @param scale The scale value to use.
	 */
	public void setScale(double scale) {
		setDistribution(new LogNormalDistribution(scale, getShape()));
	}

	public double getShape() {
		return dist.getShape();
	}

	/**
	 * Sets the shape parameter of the distribution.
	 * 
	 * @param shape The shape parameter value to use.
	 * @throws NotStrictlyPositiveException If shape was {@code <=0}.
	 */
	public void setShape(double shape) throws NotStrictlyPositiveException {
		setDistribution(new LogNormalDistribution(getScale(), shape));
	}

}
