package jasima.shopSim.util.modelDef.streams;

import jasima.core.random.continuous.DblStream;
import jasima.core.random.discrete.IntUniformRange;

public class IntUniformDef extends StreamDef {

	public static final String PARAM_MAX_VALUE = "maxValue";
	public static final String PARAM_MIN_VALUE = "minValue";
	public static final String TYPE_STRING = "intUnif";

	private int minValue = 0;
	private int maxValue = 10;

	public IntUniformDef() {
		super();
	}

	public static String getTypeString() {
		return TYPE_STRING;
	}

	@Override
	public String toString() {
		return String.format("%s(%d,%d)", getTypeString(), getMinValue(),
				getMaxValue());
	}

	@Override
	public DblStream createStream() {
		return new IntUniformRange(getMinValue(), getMaxValue());
	}

	public int getMinValue() {
		return minValue;
	}

	public void setMinValue(int minValue) {
		firePropertyChange(PARAM_MIN_VALUE, this.minValue,
				this.minValue = minValue);
	}

	public int getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(int maxValue) {
		firePropertyChange(PARAM_MAX_VALUE, this.maxValue,
				this.maxValue = maxValue);
	}

}
