package jasima.shopSim.util.modelDef.streams;

import jasima.core.random.discrete.IntConst;
import jasima.core.random.discrete.IntStream;
import jasima.core.util.Util;

import java.util.Arrays;
import java.util.List;

public class IntConstDef extends StreamDef {

	public static final String PARAM_VALUES = "values";
	public static final String TYPE_STRING = "intConst";

	public static final StreamDefFact FACTORY = new StreamDefFact() {
		@Override
		public String getTypeString() {
			return TYPE_STRING;
		}

		@Override
		public IntConstDef stringToStreamDef(String params, List<String> errors) {
			int[] ll;
			try {
				ll = Util.parseIntList(params);
			} catch (NumberFormatException nfe) {
				errors.add(String.format("invalid number: %s",
						nfe.getLocalizedMessage()));
				return null;
			}

			IntConstDef res = new IntConstDef();
			res.setValues(ll);
			return res;
		}
	};

	public IntConstDef() {
		super();
	}

	private int[] values;

	@Override
	public String toString() {
		String s = Arrays.toString(getValues()).replace("[", "")
				.replace("]", "");
		return String.format("%s(%s)", FACTORY.getTypeString(), s);
	}

	@Override
	public IntStream createStream() {
		return new IntConst(getValues().clone());
	}

	@Override
	public IntConstDef clone() throws CloneNotSupportedException {
		IntConstDef c = (IntConstDef) super.clone();

		if (values != null)
			c.values = values.clone();

		return c;
	}

	public int[] getValues() {
		return values;
	}

	public void setValues(int[] values) {
		firePropertyChange(PARAM_VALUES, this.values, this.values = values);
	}

	static {
		registerStreamFactory(IntConstDef.FACTORY);
	}

}