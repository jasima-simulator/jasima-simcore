package jasima.shopSim.util.modelDef.streams;

import org.apache.commons.math3.distribution.ExponentialDistribution;

import jasima.core.random.continuous.DblDistribution;
import jasima.core.random.continuous.DblStream;

public class DblExponentialDef extends StreamDef {

	public static final String PARAM_MEAN = "mean";
	public static final String TYPE_STRING = "dblExp";

	private double mean = 1.0;

	public DblExponentialDef() {
		super();
	}

	public static String getTypeString() {
		return TYPE_STRING;
	}

	@Override
	public String toString() {
		String params = Double.toString(getMean());
		return String.format("%s(%s)", getTypeString(), params);
	}

	@Override
	public DblStream createStream() {
		return new DblDistribution(new ExponentialDistribution(getMean()));
	}

	public double getMean() {
		return mean;
	}

	public void setMean(double mean) {
		firePropertyChange(PARAM_MEAN, this.mean, this.mean = mean);
	}

}
