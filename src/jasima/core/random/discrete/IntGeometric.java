package jasima.core.random.discrete;

import org.apache.commons.math3.distribution.GeometricDistribution;
import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.exception.OutOfRangeException;

/**
 * This class implements a discrete number stream following a <a
 * href="http://en.wikipedia.org/wiki/Geometric_distribution">Geometric
 * distribution</a>. It has a single parameter, the probability of success in a
 * single trial.
 * 
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Geometric_distribution">Geometric
 *      distribution (Wikipedia)</a>
 * @see <a
 *      href="http://mathworld.wolfram.com/GeometricDistribution.html">Geometric
 *      distribution (MathWorld)</a>
 */
public class IntGeometric extends IntDistribution {

	private static final long serialVersionUID = 3026113171560620917L;

	private GeometricDistribution dist;

	public IntGeometric() {
		this(0.5);
	}

	public IntGeometric(double probOfSuccess) {
		super();
		setDistribution(new GeometricDistribution(probOfSuccess));
	}

	public double getProbabilityOfSuccess() {
		return dist.getProbabilityOfSuccess();
	}

	/**
	 * Sets the probability of success in a single trial.
	 * 
	 * @param probOfSuccess
	 *            The success probability.
	 * @throws OutOfRangeException
	 *             If the supplied probability if not in the interval
	 *             {@code (0,1]}.
	 */
	public void setProbabilityOfSuccess(double probOfSuccess)
			throws OutOfRangeException {
		setDistribution(new GeometricDistribution(probOfSuccess));
	}

	@Override
	protected void setDistribution(IntegerDistribution distribution) {
		dist = (GeometricDistribution) distribution;
		super.setDistribution(distribution);
	}

}
