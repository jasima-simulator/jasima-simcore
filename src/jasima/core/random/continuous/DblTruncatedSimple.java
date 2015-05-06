package jasima.core.random.continuous;

import jasima.core.util.Pair;
import jasima.core.util.Util;

/**
 * Takes a version of a base stream, where minimum and maximum values are fixed.
 * This is done by returning
 * <ol>
 * <li>{@code minValue} whenever the {@code baseStream} returns a smaller value,
 * <li>{@code maxValue}, if {@code baseStream} returns a larger value
 * <li>otherwise the {@code baseStream}'s value is returned as is.
 * </ol>
 * This method is sometimes called cut-off truncation. Technically this creates
 * a new distribution with a (cumulative) distribution function with a value of
 * 0 for all values smaller than {@code minValue}, then following the
 * distribution function of {@code baseStream} for the interval [{@code minValue}
 * , {@code maxValue}], and having the value 1 for all values larger than
 * {@code maxValue}.
 * 
 * @author Torsten Hildebrandt
 * @since 1.3
 * @see <a
 *      href="http://www.comnets.uni-bremen.de/itg/itgfg521/per_eval/p004.html"
 *      >Wolfgang Bziuk: Generating random variates from truncated
 *      distributions</a> (last accessed 2015-05-06)
 */
public class DblTruncatedSimple extends DblStream {

	// TODO: implement scaled truncation in a separate class

	private static final long serialVersionUID = -3224445720493038341L;

	private DblStream baseStream;
	private double minValue;
	private double maxValue;

	public DblTruncatedSimple() {
		this(null, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	public DblTruncatedSimple(DblStream baseStream, double min, double max) {
		super();
		setBaseStream(baseStream);
		setMinValue(min);
		setMaxValue(max);
	}

	@Override
	public void init() {
		if (minValue > maxValue)
			throw new IllegalArgumentException();
		
		super.init();

		if (baseStream.getRndGen() == null) {
			baseStream.setRndGen(getRndGen());
		}
		baseStream.init();
	}

	@Override
	public double nextDbl() {
		double baseValue = baseStream.nextDbl();
		if (baseValue < minValue)
			baseValue = minValue;
		if (baseValue > maxValue)
			baseValue = maxValue;
		return baseValue;
	}

	@Override
	public double getNumericalMean() {
		// TODO implement me, requires access to baseStream's distribution
		// function
		throw new UnsupportedOperationException();
	}

	@Override
	public Pair<Double, Double> getValueRange() {
		return new Pair<Double, Double>(minValue, maxValue);
	}

	@Override
	public DblStream clone() throws CloneNotSupportedException {
		DblTruncatedSimple c = (DblTruncatedSimple) super.clone();

		if (baseStream != null)
			c.baseStream = baseStream.clone();

		return c;
	}

	@Override
	public String toString() {
		return String.format(Util.DEF_LOCALE, "Truncated(%s,min=%f,max=%f)",
				baseStream, minValue, maxValue);
	}

	// ******************* getters / setters below **********************

	public DblStream getBaseStream() {
		return baseStream;
	}

	/**
	 * Sets the stream to be truncated.
	 * 
	 * @param baseStream
	 *            The stream to truncate.
	 */
	public void setBaseStream(DblStream baseStream) {
		this.baseStream = baseStream;
	}

	public double getMinValue() {
		return minValue;
	}

	/**
	 * Sets the minimum value.
	 * 
	 * @param minValue
	 *            The lower bound of allowed values.
	 */
	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}

	public double getMaxValue() {
		return maxValue;
	}

	/**
	 * Sets the maximum allowed value.
	 * 
	 * @param maxValue
	 *            The upper bound of allowed values.
	 */
	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

}
