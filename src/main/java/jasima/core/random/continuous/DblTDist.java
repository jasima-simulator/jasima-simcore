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

import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;

/**
 * Implements a stream of numbers following a
 * <a href="http://en.wikipedia.org/wiki/Student%27s_t-distribution">Student's
 * t-distribution</a>. The distribution has a single shape parameter:
 * {@code degreesOfFreedom}.
 * 
 * @author Torsten Hildebrandt
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Student%27s_t-distribution">
 *      Student's t-distribution (Wikipedia)</a>
 * @see <a href="http://mathworld.wolfram.com/Studentst-Distribution.html">
 *      Student 's t-Distribution (MathWorld)</a>
 */
public class DblTDist extends DblDistribution {

	private static final long serialVersionUID = -3355042798681194054L;

	private TDistribution dist;

	public DblTDist() {
		this(1.0);
	}

	public DblTDist(double degreesOfFreedom) {
		super();
		setDistribution(new TDistribution(degreesOfFreedom));
	}

	@Override
	protected void setDistribution(RealDistribution distribution) {
		dist = (TDistribution) distribution;
		super.setDistribution(distribution);
	}

	@Override
	public String toString() {
		return defFormat("DblTDist(degreesOfFreedom=%f)", getDegreesOfFreedom());
	}

	public double getDegreesOfFreedom() {
		return dist.getDegreesOfFreedom();
	}

	/**
	 * Sets the degrees of freedom for this distribution.
	 * 
	 * @param degreesOfFreedom The degrees of freedom to use.
	 * @throws NotStrictlyPositiveException If the parameter value was
	 *                                      {@code <=0.0}.
	 */
	public void setDegreesOfFreedom(double degreesOfFreedom) throws NotStrictlyPositiveException {
		setDistribution(new TDistribution(degreesOfFreedom));
	}

}
