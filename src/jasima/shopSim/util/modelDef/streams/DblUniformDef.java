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
import jasima.core.random.continuous.DblUniformRange;
import jasima.core.util.Util;

import java.util.List;

public class DblUniformDef extends DblStreamDef {

	private static final long serialVersionUID = -586246159696640227L;

	public static final String PARAM_MAX_VALUE = "maxValue";
	public static final String PARAM_MIN_VALUE = "minValue";
	public static final String TYPE_STRING = "dblUnif";

	public static final StreamDefFact FACTORY = new StreamDefFact() {

		@Override
		public String getTypeString() {
			return TYPE_STRING;
		}

		@Override
		public DblUniformDef stringToStreamDef(String params,
				List<String> errors) {
			double[] ll;
			try {
				ll = Util.parseDblList(params);
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
			DblUniformDef res = new DblUniformDef();
			res.setMinValue(ll[0]);
			res.setMaxValue(ll[1]);
			return res;
		}

		@Override
		public DblStreamDef streamToStreamDef(DblStream stream) {
			if (stream instanceof DblUniformRange) {
				DblUniformRange s = (DblUniformRange) stream;
				DblUniformDef def = new DblUniformDef();

				def.setMinValue(s.min());
				def.setMaxValue(s.max());

				return def;
			} else
				return null;
		}

	};

	private double minValue = 0.0;
	private double maxValue = 1.0;

	public DblUniformDef() {
		super();
	}

	@Override
	public String toString() {
		return String.format(Util.DEF_LOCALE, "%s(%s,%s)",
				FACTORY.getTypeString(), getMinValue(), getMaxValue());
	}

	@Override
	public DblStream createStream() {
		return new DblUniformRange(getMinValue(), getMaxValue());
	}

	public double getMinValue() {
		return minValue;
	}

	public void setMinValue(double minValue) {
		firePropertyChange(PARAM_MIN_VALUE, this.minValue,
				this.minValue = minValue);
	}

	public double getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(double maxValue) {
		firePropertyChange(PARAM_MAX_VALUE, this.maxValue,
				this.maxValue = maxValue);
	}

	static {
		registerStreamFactory(DblUniformDef.FACTORY);
	}

}
