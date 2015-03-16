package jasima.core.random.discrete;

import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.exception.NotPositiveException;

/**
 * This class implements a discrete number stream following a <a
 * href="http://en.wikipedia.org/wiki/Poisson_distribution">Poisson
 * distribution</a>. It is parameterized by its mean.
 * 
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Poisson_distribution">Poisson
 *      distribution (Wikipedia)</a>
 * @see <a href="http://mathworld.wolfram.com/PoissonDistribution.html">Poisson
 *      distribution (MathWorld)</a>
 * */
public class IntPoisson extends IntDistribution {

	private static final long serialVersionUID = -3018032705267062035L;

	private PoissonDistribution dist;

	public IntPoisson() {
		this(1.0);
	}

	public IntPoisson(double mean) {
		super();
		setDistribution(new PoissonDistribution(mean));
	}

	public double getMean() {
		return dist.getMean();
	}

	/**
	 * The mean of the Poisson distribution.
	 * 
	 * @param mean
	 *            The value to use.
	 * @throws NotPositiveException
	 *             If {@code mean} was {@code <=0}.
	 */
	public void setMean(double mean) throws NotPositiveException {
		setDistribution(new PoissonDistribution(mean));
	}

	@Override
	protected void setDistribution(IntegerDistribution distribution) {
		dist = (PoissonDistribution) distribution;
		super.setDistribution(distribution);
	}

}
