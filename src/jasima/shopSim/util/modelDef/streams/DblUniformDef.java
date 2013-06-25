package jasima.shopSim.util.modelDef.streams;

import jasima.core.random.continuous.DblStream;
import jasima.core.random.continuous.DblUniformRange;

public class DblUniformDef extends StreamDef {

	public static final String PARAM_MAX_VALUE = "maxValue";
	public static final String PARAM_MIN_VALUE = "minValue";
	public static final String TYPE_STRING = "dblUnif";

	private double minValue = 0.0;
	private double maxValue = 1.0;

	public DblUniformDef() {
		super();
	}

	public static String getTypeString() {
		return TYPE_STRING;
	}

	@Override
	public String toString() {
		String params = Double.toString(getMinValue()) + ","
				+ Double.toString(getMaxValue());

		return String.format("%s(%s)", getTypeString(), params);
	}

	@Override
	public DblStream createStream() {
		return new DblUniformRange(getMinValue(), getMaxValue());
	}

	public double getMinValue() {
		return minValue;
	}

	public void setMinValue(double minValue) {
		firePropertyChange(PARAM_MIN_VALUE, this.minValue,
				this.minValue = minValue);
	}

	public double getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(double maxValue) {
		firePropertyChange(PARAM_MAX_VALUE, this.maxValue,
				this.maxValue = maxValue);
	}

}
