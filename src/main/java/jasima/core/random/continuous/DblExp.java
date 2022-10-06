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

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;

/**
 * A class implementing a number stream with values following the
 * <a href="http://en.wikipedia.org/wiki/Exponential_distribution">Exponential
 * distribution</a>.
 * 
 * @author Torsten Hildebrandt
 */
public final class DblExp extends DblDistribution {

	private static final long serialVersionUID = 7949202789555424738L;

	public static final double DEFAULT_MEAN = 1.0;

	private ExponentialDistribution dist;

	public DblExp() {
		this(DEFAULT_MEAN);
	}

	public DblExp(double mean) {
		super();
		setMean(mean);
	}

	public double getMean() {
		return dist.getMean();
	}

	/**
	 * Sets the mean of the exponential distribution.
	 * 
	 * @param mean The exponential distribution's mean. This value has to be
	 *             {@code >0}.
	 * @throws NotStrictlyPositiveException If the supplied mean value was not
	 *                                      positive.
	 */
	public void setMean(double mean) throws NotStrictlyPositiveException {
		dist = new ExponentialDistribution(mean);
		setDistribution(dist);
	}

	@Override
	public String toString() {
		return defFormat("DblExp(mean=%f)", getMean());
	}

	@Override
	public DblSequence clone() {
		// default cloning behaviour is ok as 'dist' is immutable
		return super.clone();
	}

}
