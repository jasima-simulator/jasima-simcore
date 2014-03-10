package jasima.shopSim.util.modelDef.streams;

import jasima.core.random.continuous.DblConst;
import jasima.core.random.continuous.DblStream;
import jasima.core.util.Util;

import java.util.Arrays;
import java.util.List;

public class DblConstDef extends StreamDef {

	public static final String PARAM_VALUES = "values";
	public static final String TYPE_STRING = "const";

	public static final StreamDefFact FACTORY = new StreamDefFact() {
		@Override
		public String getTypeString() {
			return TYPE_STRING;
		}

		@Override
		public DblConstDef stringToStreamDef(String params, List<String> errors) {
			double[] ll;
			try {
				ll = Util.parseDblList(params);
			} catch (NumberFormatException nfe) {
				errors.add(String.format("invalid number: %s",
						nfe.getLocalizedMessage()));
				return null;
			}

			DblConstDef res = new DblConstDef();
			res.setValues(ll);
			return res;
		}
	};

	
	
	private double[] values;

	@Override
	public String toString() {
		String s = Arrays.toString(getValues()).replace("[", "")
				.replace("]", "");
		return String.format("%s(%s)", FACTORY.getTypeString(), s);
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