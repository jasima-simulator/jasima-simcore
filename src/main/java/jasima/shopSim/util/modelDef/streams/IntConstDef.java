/*
This file is part of jasima, the Java simulator for manufacturing and logistics.
 
Copyright 2010-2022 jasima contributors (see license.txt)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package jasima.shopSim.util.modelDef.streams;

import static jasima.core.util.i18n.I18n.defFormat;

import java.util.Arrays;
import java.util.List;

import jasima.core.random.continuous.DblSequence;
import jasima.core.random.discrete.IntConst;
import jasima.core.random.discrete.IntSequence;
import jasima.core.util.Util;

public class IntConstDef extends IntStreamDef {

	private static final long serialVersionUID = -4870221883421631767L;

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
				errors.add(defFormat("invalid number: %s", nfe.getLocalizedMessage()));
				return null;
			}

			IntConstDef res = new IntConstDef();
			res.setValues(ll);
			return res;
		}

		@Override
		public DblStreamDef streamToStreamDef(DblSequence stream) {
			if (stream instanceof IntConst) {
				IntConst s = (IntConst) stream;
				IntConstDef def = new IntConstDef();

				int[] values = s.getValues();
				if (values != null)
					values = values.clone();
				def.setValues(values);

				return def;
			} else
				return null;
		}
	};

	public IntConstDef() {
		super();
	}

	private int[] values = { 1, 2, 3 };

	@Override
	public String toString() {
		String s = Arrays.toString(getValues()).replace("[", "").replace("]", "");
		return defFormat("%s(%s)", FACTORY.getTypeString(), s);
	}

	@Override
	public IntSequence createStream() {
		return new IntConst(getValues() != null ? getValues().clone() : null);
	}

	@Override
	public IntConstDef clone() {
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