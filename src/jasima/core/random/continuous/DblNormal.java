package jasima.core.random.continuous;

import jasima.core.util.Pair;

import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;

/**
 * Creates a number stream with values following a <a
 * href="http://en.wikipedia.org/wiki/Normal_distribution">Normal
 * distribution</a>.
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 */
public class DblNormal extends DblStream {

	private static final long serialVersionUID = 8266321644360710699L;

	private double mean = 0.0;
	private double stdev = 1.0;

	public DblNormal() {
		super();
	}

	@Override
	public double nextDbl() {
		return getMean() + rndGen.nextGaussian() * getStdev();
	}

	@Override
	public double getNumericalMean() {
		return mean;
	}

	public double getMean() {
		return mean;
	}

	/**
	 * Sets the mean value of this normally distributed number stream. Default
	 * value is {@code 0.0}.
	 * 
	 * @param mean
	 *            The mean value.
	 */
	public void setMean(double mean) {
		this.mean = mean;
	}

	public double getStdev() {
		return stdev;
	}

	/**
	 * Sets the standard deviation of this normally distributed number stream.
	 * This defaults to {@code 1.0}.
	 * 
	 * @param stdev
	 *            The standard deviation.
	 * @throws NotPositiveException
	 *             Raised if stdev was negative.
	 */
	public void setStdev(double stdev) throws NotPositiveException {
		if (stdev >= 0.0) {
			this.stdev = stdev;
		} else {
			throw new NotStrictlyPositiveException(stdev);
		}
	}

	@Override
	public Pair<Double, Double> getValueRange() {
		return new Pair<>(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

}
