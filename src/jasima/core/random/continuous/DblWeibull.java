package jasima.core.random.continuous;

import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.distribution.WeibullDistribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;

/**
 * This class implements a number stream of real values following a <a
 * href="http://en.wikipedia.org/wiki/Weibull_distribution">Weibull
 * distribution</a>. A Weilbull distribution is characterized by a shape
 * parameter and a scale parameter.
 * 
 * @author Torsten Hildebrandt
 * @version "$Id$"
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Weibull_distribution">Weibull
 *      distribution (Wikipedia)</a>
 * @see <a href="http://mathworld.wolfram.com/WeibullDistribution.html">Weibull
 *      distribution (MathWorld)</a>
 */
public class DblWeibull extends DblDistribution {

	private static final long serialVersionUID = 2252634635785170819L;

	private WeibullDistribution dist;

	public DblWeibull() {
		this(1.0, 1.5);
	}

	public DblWeibull(double shape, double scale) {
		super();
		setDistribution(new WeibullDistribution(shape, scale));
	}

	@Override
	protected void setDistribution(RealDistribution distribution) {
		dist = (WeibullDistribution) distribution;
		super.setDistribution(distribution);
	}

	public double getShape() {
		return dist.getShape();
	}

	/**
	 * Sets the shape parameter for this distribution.
	 * 
	 * @param shape
	 *            The shape parameter to use.
	 * @throws NotStrictlyPositiveException
	 *             If the parameter value was {@code <=0.0}.
	 */
	public void setShape(double shape) throws NotStrictlyPositiveException {
		setDistribution(new WeibullDistribution(shape, dist.getScale()));
	}

	public double getScale() {
		return dist.getScale();
	}

	/**
	 * Sets the scale parameter for this distribution.
	 * 
	 * @param scale
	 *            The scale parameter to use.
	 * @throws NotStrictlyPositiveException
	 *             If the parameter value was {@code <=0.0}.
	 */
	public void setScale(double scale) {
		setDistribution(new WeibullDistribution(dist.getShape(), scale));
	}

}
