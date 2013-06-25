package jasima.shopSim.util.modelDef.streams;

import jasima.core.random.continuous.DblConst;
import jasima.core.random.continuous.DblStream;

import java.util.Arrays;

public class DblConstDef extends StreamDef {

	public static final String PARAM_VALUES = "values";
	public static final String TYPE_STRING = "const";

	private double[] values;

	public static String getTypeString() {
		return TYPE_STRING;
	}

	@Override
	public String toString() {
		String s = Arrays.toString(getValues()).replace("[", "")
				.replace("]", "");
		return String.format("%s(%s)", getTypeString(), s);
	}

	@Override
	public DblStream createStream() {
		return new DblConst(getValues().clone());
	}

	public double[] getValues() {
		return values;
	}

	public void setValues(double[] values) {
		firePropertyChange(PARAM_VALUES, this.values, this.values = values);
	}

}