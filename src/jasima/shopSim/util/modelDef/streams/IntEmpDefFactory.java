package jasima.shopSim.util.modelDef.streams;

import jasima.core.util.Pair;
import jasima.core.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class IntEmpDefFactory extends StreamDefFact {

	public IntEmpDefFactory() {
		super();
	}

	@Override
	public String getTypeString() {
		return IntEmpDef.getTypeString();
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

}
