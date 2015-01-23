/*******************************************************************************
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This file is part of jasima, v1.2.
 *
 * jasima is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jasima is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jasima.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package jasima.shopSim.util.modelDef.streams;

import jasima.core.random.continuous.DblStream;
import jasima.core.random.discrete.IntConst;
import jasima.core.random.discrete.IntStream;
import jasima.core.util.Util;

import java.util.Arrays;
import java.util.List;

public class IntConstDef extends IntStreamDef {

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

		@Override
		public DblStreamDef streamToStreamDef(DblStream stream) {
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
		String s = Arrays.toString(getValues()).replace("[", "")
				.replace("]", "");
		return String.format("%s(%s)", FACTORY.getTypeString(), s);
	}

	@Override
	public IntStream createStream() {
		return new IntConst(getValues() != null ? getValues().clone() : null);
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