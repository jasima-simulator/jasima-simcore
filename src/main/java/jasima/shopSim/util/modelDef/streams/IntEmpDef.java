/*******************************************************************************
 * This file is part of jasima, v1.3, the Java simulator for manufacturing and 
 * logistics.
 *  
 * Copyright (c) 2015 		jasima solutions UG
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package jasima.shopSim.util.modelDef.streams;

import static jasima.core.util.i18n.I18n.defFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import jasima.core.random.continuous.DblSequence;
import jasima.core.random.discrete.IntEmpirical;
import jasima.core.util.Pair;
import jasima.core.util.Util;

public class IntEmpDef extends IntStreamDef {

	private static final long serialVersionUID = 6302098802706171687L;

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
				errors.add(defFormat("invalid number: %s", nfe.getLocalizedMessage()));
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
				errors.add(defFormat("probabilities have to sum to 1.0, current sum is %f.", Util.sum(probs)));
				return null;
			}

			IntEmpDef res = new IntEmpDef();
			res.setProbs(probs);
			res.setValues(values);
			return res;
		}

		@Override
		public DblStreamDef streamToStreamDef(DblSequence stream) {
			if (stream instanceof IntEmpirical) {
				IntEmpirical s = (IntEmpirical) stream;
				IntEmpDef def = new IntEmpDef();

				double[] probs = s.getProbabilities();
				if (probs != null)
					probs = probs.clone();
				def.setProbs(probs);

				int[] values = s.getValues();
				if (values != null)
					values = values.clone();
				def.setValues(values);

				return def;
			} else
				return null;
		}

	};

	private double[] probs = { 0.7, 0.3 };
	private int[] values = { 1, 2 };

	public IntEmpDef() {
		super();
	}

	@Override
	public String toString() {
		String params = "";

		StringBuilder sb = new StringBuilder();

		int n = probs != null ? probs.length : 0;
		int m = values != null ? values.length : 0;
		for (int i = 0; i < Math.max(n, m); i++) {
			String v = values != null && i < values.length ? Integer.toString(values[i]) : "?";
			String p = probs != null && i < probs.length ? Double.toString(probs[i]) : "?";

			sb.append('<').append(v).append(',').append(p).append(">;");
		}
		if (sb.length() > 0)
			params = sb.substring(0, sb.length() - 1);

		return defFormat("%s(%s)", FACTORY.getTypeString(), params);
	}

	@Override
	public IntEmpDef clone() {
		IntEmpDef c = (IntEmpDef) super.clone();

		if (values != null)
			c.values = values.clone();
		if (probs != null)
			c.probs = probs.clone();

		return c;
	}

	@Override
	public DblSequence createStream() {
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
