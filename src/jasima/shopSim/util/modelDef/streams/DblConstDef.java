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

import java.util.Arrays;
import java.util.List;

import jasima.core.random.continuous.DblConst;
import jasima.core.random.continuous.DblSequence;
import jasima.core.util.Util;
import jasima.core.util.i18n.I18n;

public class DblConstDef extends DblStreamDef {

	private static final long serialVersionUID = -2859329292507063575L;

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
				errors.add(I18n.defFormat("invalid number: %s", nfe.getLocalizedMessage()));
				return null;
			}

			DblConstDef res = new DblConstDef();
			res.setValues(ll);
			return res;
		}

		@Override
		public DblStreamDef streamToStreamDef(DblSequence stream) {
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
		String s = Arrays.toString(getValues()).replace("[", "").replace("]", "");
		return I18n.defFormat("%s(%s)", FACTORY.getTypeString(), s);
	}

	@Override
	public DblConstDef clone() {
		DblConstDef c = (DblConstDef) super.clone();

		if (values != null)
			c.values = values.clone();

		return c;
	}

	@Override
	public DblSequence createStream() {
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