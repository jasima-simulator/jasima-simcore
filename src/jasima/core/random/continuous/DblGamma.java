package jasima.core.random.continuous;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;

/**
 * Implements a stream of real numbers where each value follows a <a
 * href="http://en.wikipedia.org/wiki/Gamma_distribution">Gamma
 * distribution</a>. The distribution is characterized by a shape and a scale
 * parameter. It is a generalization of the Exponential and Erlang
 * distributions.
 * 
 * @author Torsten Hildebrandt
 * @version "$Id$"
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
		setDistribution(new GammaDistribution(shape, dist.getScale()));
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
	public void setScale(double scale) throws NotStrictlyPositiveException {
		setDistribution(new GammaDistribution(dist.getShape(), scale));
	}

	@Override
	protected void setDistribution(RealDistribution distribution) {
		dist = (GammaDistribution) distribution;
		super.setDistribution(distribution);
	}

}
