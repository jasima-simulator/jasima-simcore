package jasima.core.random.continuous;

/**
 * Returns a random real number following a triangular distribution as defined
 * by the three parameters min, mode, and max.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version "$Id: $"
 */
public class DblTriangular extends DblStream {

	private static final long serialVersionUID = -8098960209631757070L;

	// parameters with some arbitrary default values
	private double min = 0.0;
	private double mode = 5.0;
	private double max = 10.0;

	// constants derived from parameters
	private double critValue, range, minDist, maxDist;

	public DblTriangular(double min, double mode, double max) {
		super();
		setMin(min);
		setMode(mode);
		setMax(max);
	}

	private void calcInternalValues() {
		range = (max - min);
		minDist = (mode - min);
		maxDist = (max - mode);
		critValue = minDist / range;
	}

	@Override
	public double nextDbl() {
		double rnd = rndGen.nextDouble();

		// inverse of CDF
		double v;
		if (rnd <= critValue) {
			v = getMin() + Math.sqrt(range * minDist * rnd);
		} else {
			v = getMax() - Math.sqrt(range * maxDist * (1.0 - rnd));
		}

		return v;
	}

	@Override
	public String toString() {
		return "DblTriangular(min=" + min + ";mode=" + mode + ";max=" + max
				+ ")";
	}

	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
		calcInternalValues();
	}

	public double getMode() {
		return mode;
	}

	public void setMode(double mode) {
		this.mode = mode;
		calcInternalValues();
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
		calcInternalValues();
	}

}
