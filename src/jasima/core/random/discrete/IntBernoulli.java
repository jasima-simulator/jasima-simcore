package jasima.core.random.discrete;

import jasima.core.util.Pair;

import org.apache.commons.math3.exception.OutOfRangeException;

/**
 * This class implements a discrete number stream following a <a
 * href="http://en.wikipedia.org/wiki/Bernoulli_distribution">Bernoulli
 * distribution</a>. This means, it contains the value {@code 0} with a
 * probability of {@code 1-probabilityOfSuccess} and the value {@code 1} with a
 * probability of {@code probabilityOfSuccess}.
 * 
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Bernoulli_distribution">Bernoulli
 *      distribution (Wikipedia)</a>
 * @see <a
 *      href="http://mathworld.wolfram.com/BernoulliDistribution.html">Bernoulli
 *      distribution (MathWorld)</a>
 */
public class IntBernoulli extends IntStream {
	private static final long serialVersionUID = 1515307949822420310L;

	private double probabilityOfSuccess;

	public IntBernoulli() {
		this(0.5);
	}

	public IntBernoulli(double probabilityOfSuccess) {
		super();
		setProbabilityOfSuccess(probabilityOfSuccess);
	}

	@Override
	public int nextInt() {
		return rndGen.nextDouble() < probabilityOfSuccess ? 1 : 0;
	}

	@Override
	public double getNumericalMean() {
		return probabilityOfSuccess;
	}

	public double getProbabilityOfSuccess() {
		return probabilityOfSuccess;
	}

	/**
	 * Sets the probability of success.
	 * 
	 * @param probOfSuccess
	 *            The success probability.
	 * @throws OutOfRangeException
	 *             If the supplied probability is not in the interval
	 *             {@code [0,1]}.
	 */
	public void setProbabilityOfSuccess(double probOfSuccess)
			throws OutOfRangeException {
		if (probOfSuccess < 0 || probOfSuccess > 1) {
			throw new OutOfRangeException(probOfSuccess, 0, 1);
		}
		probabilityOfSuccess = probOfSuccess;
	}

	@Override
	public Pair<Double, Double> getValueRange() {
		return new Pair<>(0.0, 1.0);
	}

}
