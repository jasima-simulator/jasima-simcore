package jasima.core.random.continuous;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;

/**
 * A class implementing a number stream with values following the <a
 * href="http://en.wikipedia.org/wiki/Exponential_distribution">Exponential
 * distribution</a>.
 * 
 * @author Torsten Hildebrandt
 * @version "$Id$"
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
	 * @param mean
	 *            The exponential distribution's mean. This value has to be
	 *            {@code >0}.
	 * @throws NotStrictlyPositiveException
	 *             If the supplied mean value was not positive.
	 */
	public void setMean(double mean) throws NotStrictlyPositiveException {
		dist = new ExponentialDistribution(mean);
		setDistribution(dist);
	}

	@Override
	public DblStream clone() throws CloneNotSupportedException {
		// default cloning behaviour is ok as 'dist' is immutable
		return super.clone();
	}

}
