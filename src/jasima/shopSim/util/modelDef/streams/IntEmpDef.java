package jasima.shopSim.util.modelDef.streams;

import jasima.core.random.continuous.DblStream;
import jasima.core.random.discrete.IntEmpirical;
import jasima.core.util.Pair;
import jasima.core.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class IntEmpDef extends StreamDef {

	public static final String PARAM_PROBS = "probs";
	public static final String PARAM_VALUES = "values";
	public static final String TYPE_STRING = "intEmp";

	public static final StreamDefFact FACTORY = new StreamDefFact() {

		@Override
		public String getTypeString() {
			return TYPE_STRING;
		}

		@Override
		public IntEmpDef stringToStreamDef(String params, List<String> errors) {
			ArrayList<Pair<Integer, Double>> l = new ArrayList<Pair<Integer, Double>>();
			StringTokenizer st = new StringTokenizer(params, "<");
			try {
				while (st.hasMoreTokens()) {
					String v = st.nextToken().replace(">", "").trim();
					String[] vv = v.split(",");
					int v1 = Integer.parseInt(vv[0]);
					double p1 = Double.parseDouble(vv[1]);
					l.add(new Pair<Integer, Double>(v1, p1));
				}
			} catch (NumberFormatException nfe) {
				errors.add(String.format("invalid number: %s",
						nfe.getLocalizedMessage()));
				return null;
			}

			double[] probs = new double[l.size()];
			int[] values = new int[l.size()];
			for (int i = 0; i < l.size(); i++) {
				Pair<Integer, Double> p = l.get(i);
				values[i] = p.a;
				probs[i] = p.b;
			}
			if (Math.abs(Util.sum(probs) - 1.0) > 1e-6) {
				errors.add(String.format(
						"probabilities have to sum to 1.0, current sum is %f.",
						Util.sum(probs)));
				return null;
			}

			IntEmpDef res = new IntEmpDef();
			res.setProbs(probs);
			res.setValues(values);
			return res;
		}

	};

	private double[] probs = {};
	private int[] values = {};

	public IntEmpDef() {
		super();
	}

	@Override
	public String toString() {
		String params = "";

		StringBuilder sb = new StringBuilder();
		if (probs != null) {
			for (int i = 0; i < probs.length; i++) {
				sb.append('<').append(values[i]).append(',').append(probs[i])
						.append(">,");
			}
			if (sb.length() > 0)
				params = sb.substring(0, sb.length() - 1);
		}

		return String.format("%s(%s)", FACTORY.getTypeString(), params);
	}

	@Override
	public IntEmpDef clone() throws CloneNotSupportedException {
		IntEmpDef c = (IntEmpDef) super.clone();

		if (values != null)
			c.values = values.clone();
		if (probs != null)
			c.probs = probs.clone();

		return c;
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

	static {
		registerStreamFactory(IntEmpDef.FACTORY);
	}

}
