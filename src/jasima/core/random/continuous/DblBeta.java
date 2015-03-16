package jasima.core.random.continuous;

import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;

/**
 * Implements a number stream with values following a <a
 * href="http://en.wikipedia.org/wiki/Beta_distribution">Beta distribution</a>.
 * A beta distribution is characterized by the two positive shape parameters
 * {@code alpha} and {@code beta}.
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
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

	public double getAlpha() {
		return dist.getAlpha();
	}

	/**
	 * Sets the parameter value for the distribution's shape parameter
	 * {@code alpha}.
	 * 
	 * @param alpha
	 *            The alpha value to use.
	 * @throws NotStrictlyPositiveException
	 *             If {@code alpha} was {@code <=0}.
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
	 * @param beta
	 *            The {@code beta} parameter value to use.
	 * @throws NotStrictlyPositiveException
	 *             If {@code beta} was {@code <=0}.
	 */
	public void setBeta(double beta) throws NotStrictlyPositiveException {
		setDistribution(new BetaDistribution(getAlpha(), beta));
	}

	@Override
	protected void setDistribution(RealDistribution distribution) {
		dist = (BetaDistribution) distribution;
		super.setDistribution(distribution);
	}

}
