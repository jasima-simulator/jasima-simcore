package jasima.core.random.discrete;

import org.apache.commons.math3.distribution.HypergeometricDistribution;
import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;

/**
 * This class implements a discrete number stream following a <a
 * href="http://en.wikipedia.org/wiki/Hypergeometric_distribution"
 * >Hypergeometric distribution</a>. It has a three parameters: the population
 * size, the number of (possible) successes in the population, and the number of
 * trials/samples.
 * 
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 * 
 * @see <a
 *      href="http://en.wikipedia.org/wiki/Hypergeometric_distribution">Hypergeometric
 *      distribution (Wikipedia)</a>
 * @see <a
 *      href="http://mathworld.wolfram.com/HypergeometricDistribution.html">Hypergeometric
 *      distribution (MathWorld)</a>
 */
public class IntHypergeometric extends IntDistribution {

	private static final long serialVersionUID = 7866375360611450226L;

	private HypergeometricDistribution dist;

	public IntHypergeometric() {
		this(20, 10, 5);
	}

	public IntHypergeometric(int populationSize, int numberOfSuccesses,
			int sampleSize) {
		super();
		setDistribution(new HypergeometricDistribution(populationSize,
				numberOfSuccesses, sampleSize));
	}

	public int getPopulationSize() {
		return dist.getPopulationSize();
	}

	/**
	 * The total population size.
	 * 
	 * @param populationSize
	 *            The value for the population size.
	 * @throws NotStrictlyPositiveException
	 *             If the supplied value was {@code <=0}.
	 */
	public void setPopulationSize(int populationSize)
			throws NotStrictlyPositiveException {
		setDistribution(new HypergeometricDistribution(populationSize,
				dist.getNumberOfSuccesses(), dist.getSampleSize()));
	}

	public int getNumberOfSuccesses() {
		return dist.getNumberOfSuccesses();
	}

	/**
	 * The number of possible successes in the population.
	 * 
	 * @param numberOfSuccesses
	 *            The number of successes.
	 * @throws NumberIsTooLargeException
	 *             If the supplied value is larger than {@code populationSize}.
	 */
	public void setNumberOfSuccesses(int numberOfSuccesses)
			throws NumberIsTooLargeException {
		setDistribution(new HypergeometricDistribution(
				dist.getPopulationSize(), numberOfSuccesses,
				dist.getSampleSize()));
	}

	public int getSampleSize() {
		return dist.getSampleSize();
	}

	/**
	 * The number of samples taken.
	 * 
	 * @param sampleSize
	 *            The number of trials conducted.
	 * @throws NumberIsTooLargeException
	 *             If the supplied value is larger than {@code populationSize}.
	 */
	public void setSampleSize(int sampleSize) throws NumberIsTooLargeException {
		setDistribution(new HypergeometricDistribution(
				dist.getPopulationSize(), dist.getNumberOfSuccesses(),
				sampleSize));
	}

	@Override
	protected void setDistribution(IntegerDistribution distribution) {
		dist = (HypergeometricDistribution) distribution;
		super.setDistribution(distribution);
	}

}
