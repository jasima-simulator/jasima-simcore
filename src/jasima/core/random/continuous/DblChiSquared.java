package jasima.core.random.continuous;

import jasima.core.util.Util;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;

/**
 * Implements a stream of numbers following a <a
 * href="http://en.wikipedia.org/wiki/Chi-squared_distribution">Chi-squared
 * distribution</a>. The distribution has a single shape (integer) parameter:
 * {@code degreesOfFreedom}. This distribution is a special case of a
 * {@link DblGamma Gamma distribution}.
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 * 
 * @see <a
 *      href="http://en.wikipedia.org/wiki/Chi-squared_distribution">Chi-squared
 *      distribution (Wikipedia)</a>
 * @see <a
 *      href="http://mathworld.wolfram.com/Chi-SquaredDistribution.html">Chi-squared
 *      distribution (MathWorld)</a>
 */
public class DblChiSquared extends DblDistribution {

	private static final long serialVersionUID = -5702591884949743185L;

	private ChiSquaredDistribution dist;

	public DblChiSquared() {
		this(2);
	}

	public DblChiSquared(int degreesOfFreedom) {
		super();
		setDistribution(new ChiSquaredDistribution(degreesOfFreedom));
	}

	@Override
	public String toString() {
		return String.format(Util.DEF_LOCALE,
				"DblChiSquared(degreesOfFreedom=%d)", getDegreesOfFreedom());
	}

	@Override
	protected void setDistribution(RealDistribution distribution) {
		dist = (ChiSquaredDistribution) distribution;
		super.setDistribution(distribution);
	}

	public int getDegreesOfFreedom() {
		return (int) dist.getDegreesOfFreedom();
	}

	/**
	 * Sets the degrees of freedom for this distribution.
	 * 
	 * @param degreesOfFreedom
	 *            The degrees of freedom to use.
	 * @throws NotStrictlyPositiveException
	 *             If the parameter value was {@code <=0.0}.
	 */
	public void setDegreesOfFreedom(int degreesOfFreedom)
			throws NotStrictlyPositiveException {
		setDistribution(new ChiSquaredDistribution(degreesOfFreedom));
	}

}
