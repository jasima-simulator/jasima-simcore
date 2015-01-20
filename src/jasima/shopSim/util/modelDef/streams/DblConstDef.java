package jasima.shopSim.util.modelDef.streams;

import jasima.core.random.continuous.DblConst;
import jasima.core.random.continuous.DblStream;
import jasima.core.util.Util;

import java.util.Arrays;
import java.util.List;

public class DblConstDef extends DblStreamDef {

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

		@Override
		public DblStreamDef streamToStreamDef(DblStream stream) {
			if (stream instanceof DblConst) {
				DblConst s = (DblConst) stream;
				DblConstDef def = new DblConstDef();

				double[] values = s.getValues();
				if (values != null)
					values = values.clone();
				def.setValues(values);

				return def;
			} else
				return null;
		}
	};

	public DblConstDef() {
		super();
	}

	private double[] values = { 1.0, 2.0, 3.0 };

	@Override
	public String toString() {
		String s = Arrays.toString(getValues()).replace("[", "")
				.replace("]", "");
		return String.format("%s(%s)", FACTORY.getTypeString(), s);
	}

	@Override
	public DblConstDef clone() throws CloneNotSupportedException {
		DblConstDef c = (DblConstDef) super.clone();

		if (values != null)
			c.values = values.clone();

		return c;
	}

	@Override
	public DblStream createStream() {
		return new DblConst(getValues() != null ? getValues().clone() : null);
	}

	public double[] getValues() {
		return values;
	}

	public void setValues(double[] values) {
		firePropertyChange(PARAM_VALUES, this.values, this.values = values);
	}

	static {
		registerStreamFactory(DblConstDef.FACTORY);
	}

}