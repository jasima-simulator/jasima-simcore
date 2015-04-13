package jasima.core.random.continuous;

import jasima.core.util.Pair;

/**
 * Creates a number stream that is the sum of a given set of base streams.
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 */
public class DblSumStream extends DblStream {

	private static final long serialVersionUID = -1978211297841470708L;

	private DblStream[] subStreams;

	public DblSumStream() {
		this((DblStream[]) null);
	}

	public DblSumStream(DblStream... subStreams) {
		super();
		this.subStreams = subStreams;
	}

	@Override
	public void init() {
		super.init();
		for (DblStream s : subStreams) {
			s.setRndGen(getRndGen());
		}
	}

	@Override
	public double nextDbl() {
		double sum = subStreams[0].nextDbl();
		for (int i = 1, n = subStreams.length; i < n; i++) {
			sum += subStreams[i].nextDbl();
		}
		return sum;
	}

	@Override
	public double getNumericalMean() {
		if (subStreams == null || subStreams.length == 0)
			return Double.NaN;

		double sum = subStreams[0].getNumericalMean();
		for (int i = 1, n = subStreams.length; i < n; i++) {
			sum += subStreams[i].getNumericalMean();
		}
		return sum;
	}

	@Override
	public Pair<Double, Double> getValueRange() {
		if (subStreams == null || subStreams.length == 0)
			return new Pair<>(Double.NaN, Double.NaN);

		Pair<Double, Double> r0 = subStreams[0].getValueRange();
		double min = r0.a;
		double max = r0.b;

		for (int i = 1, n = subStreams.length; i < n; i++) {
			Pair<Double, Double> range = subStreams[i].getValueRange();

			min += range.a;
			max += range.b;
		}

		return new Pair<>(min, max);
	}

	// ************* getter / setter below ****************

	public DblStream[] getSubStreams() {
		return subStreams;
	}

	/**
	 * Sets the sub-streams to compute the values of this number stream.
	 * 
	 * @param subStreams
	 *            The sub-streams to use.
	 */
	public void setSubStreams(DblStream... subStreams) {
		this.subStreams = subStreams;
	}

}
