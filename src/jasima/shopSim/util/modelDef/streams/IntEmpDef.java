package jasima.shopSim.util.modelDef.streams;

import jasima.core.random.continuous.DblStream;
import jasima.core.random.discrete.IntEmpirical;

public class IntEmpDef extends StreamDef {

	public static final String PARAM_PROBS = "probs";
	public static final String PARAM_VALUES = "values";
	public static final String TYPE_STRING = "intEmp";

	private double[] probs = null;
	private int[] values = null;

	public IntEmpDef() {
		super();
	}

	public static String getTypeString() {
		return TYPE_STRING;
	}

	@Override
	public String toString() {
		String params = "";

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < probs.length; i++) {
			sb.append('<').append(values[i]).append(',').append(probs[i])
					.append(">,");
		}
		if (sb.length() > 0)
			params = sb.substring(0, sb.length() - 1);

		return String.format("%s(%s)", getTypeString(), params);
	}

	@Override
	public DblStream createStream() {
		return new IntEmpirical(getProbs().clone(), getValues().clone());
	}

	public double[] getProbs() {
		return probs;
	}

	public void setProbs(double[] probs) {
		firePropertyChange(PARAM_PROBS, this.probs, this.probs = probs);
	}

	public int[] getValues() {
		return values;
	}

	public void setValues(int[] values) {
		firePropertyChange(PARAM_VALUES, this.values, this.values = values);
	}

}
