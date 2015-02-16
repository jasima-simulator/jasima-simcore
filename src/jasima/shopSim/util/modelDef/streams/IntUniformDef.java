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
import jasima.core.random.discrete.IntUniformRange;
import jasima.core.util.Util;

import java.util.List;

public class IntUniformDef extends IntStreamDef {

	private static final long serialVersionUID = 741813233923786283L;

	public static final String PARAM_MAX_VALUE = "maxValue";
	public static final String PARAM_MIN_VALUE = "minValue";
	public static final String TYPE_STRING = "intUnif";

	public static final StreamDefFact FACTORY = new StreamDefFact() {

		@Override
		public String getTypeString() {
			return TYPE_STRING;
		}

		@Override
		public IntUniformDef stringToStreamDef(String params,
				List<String> errors) {
			int[] ll;
			try {
				ll = Util.parseIntList(params);
			} catch (NumberFormatException nfe) {
				errors.add(String.format(Util.DEF_LOCALE, "invalid number: %s",
						nfe.getLocalizedMessage()));
				return null;
			}
			if (ll.length != 2) {
				errors.add(String
						.format(Util.DEF_LOCALE,
								"invalid number of parameters (2 required, min and max value): '%s'",
								params));
				return null;
			}

			IntUniformDef res = new IntUniformDef();
			res.setMinValue(ll[0]);
			res.setMaxValue(ll[1]);
			return res;
		}

		@Override
		public DblStreamDef streamToStreamDef(DblStream stream) {
			if (stream instanceof IntUniformRange) {
				IntUniformRange s = (IntUniformRange) stream;
				IntUniformDef def = new IntUniformDef();

				def.setMinValue(s.getMin());
				def.setMaxValue(s.getMax());

				return def;
			} else
				return null;
		}

	};

	private int minValue = 0;
	private int maxValue = 10;

	public IntUniformDef() {
		super();
	}

	@Override
	public String toString() {
		return String.format(Util.DEF_LOCALE, "%s(%d,%d)",
				FACTORY.getTypeString(), getMinValue(), getMaxValue());
	}

	@Override
	public DblStream createStream() {
		return new IntUniformRange(getMinValue(), getMaxValue());
	}

	public int getMinValue() {
		return minValue;
	}

	public void setMinValue(int minValue) {
		firePropertyChange(PARAM_MIN_VALUE, this.minValue,
				this.minValue = minValue);
	}

	public int getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(int maxValue) {
		firePropertyChange(PARAM_MAX_VALUE, this.maxValue,
				this.maxValue = maxValue);
	}

	static {
		registerStreamFactory(IntUniformDef.FACTORY);
	}

}
