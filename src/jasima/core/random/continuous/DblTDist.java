package jasima.core.random.continuous;

import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;

/**
 * Implements a stream of numbers following a <a
 * href="http://en.wikipedia.org/wiki/Student%27s_t-distribution">Student's
 * t-distribution</a>. The distribution has a single shape parameter:
 * {@code degreesOfFreedom}.
 * 
 * @author Torsten Hildebrandt
 * @version "$Id$"
 * 
 * @see <a
 *      href="http://en.wikipedia.org/wiki/Student%27s_t-distribution">Student's
 *      t-distribution (Wikipedia)</a>
 * @see <a
 *      href="http://mathworld.wolfram.com/Studentst-Distribution.html">Student
 *      's t-Distribution (MathWorld)</a>
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

	public double getDegreesOfFreedom() {
		return dist.getDegreesOfFreedom();
	}

	/**
	 * Sets the degrees of freedom for this distribution.
	 * 
	 * @param degreesOfFreedom
	 *            The degrees of freedom to use.
	 * @throws NotStrictlyPositiveException
	 *             If the parameter value was {@code <=0.0}.
	 */
	public void setDegreesOfFreedom(double degreesOfFreedom)
			throws NotStrictlyPositiveException {
		setDistribution(new TDistribution(degreesOfFreedom));
	}

	@Override
	protected void setDistribution(RealDistribution distribution) {
		dist = (TDistribution) distribution;
		super.setDistribution(distribution);
	}

}
