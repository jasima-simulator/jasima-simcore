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
import jasima.core.random.continuous.DblTriangular;
import jasima.core.util.Util;

import java.util.List;
import java.util.Locale;

public class DblTriangularDef extends DblStreamDef {

	private static final long serialVersionUID = 2748975328234554477L;

	public static final String PARAM_MAX_VALUE = "maxValue";
	public static final String PARAM_MODE_VALUE = "modeValue";
	public static final String PARAM_MIN_VALUE = "minValue";
	public static final String TYPE_STRING = "dblTri";

	public static final StreamDefFact FACTORY = new StreamDefFact() {

		@Override
		public String getTypeString() {
			return TYPE_STRING;
		}

		@Override
		public DblTriangularDef stringToStreamDef(String params,
				List<String> errors) {
			double[] ll;
			try {
				ll = Util.parseDblList(params);
			} catch (NumberFormatException nfe) {
				errors.add(String.format(Util.DEF_LOCALE, "invalid number: %s",
						nfe.getLocalizedMessage()));
				return null;
			}
			if (ll.length != 3) {
				errors.add(String
						.format(Util.DEF_LOCALE,
								"invalid number of parameters (3 required, min, mode, and max value): '%s'",
								params));
				return null;
			}

			DblTriangularDef res = new DblTriangularDef();
			res.setMinValue(ll[0]);
			res.setModeValue(ll[1]);
			res.setMaxValue(ll[2]);
			return res;
		}

		@Override
		public DblStreamDef streamToStreamDef(DblStream stream) {
			if (stream instanceof DblTriangular) {
				DblTriangular s = (DblTriangular) stream;
				DblTriangularDef def = new DblTriangularDef();

				def.setMinValue(s.getMin());
				def.setModeValue(s.getMode());
				def.setMaxValue(s.getMax());

				return def;
			} else
				return null;
		}

	};

	private double minValue = 0.0;
	private double modeValue = 5.0;
	private double maxValue = 10.0;

	public DblTriangularDef() {
		super();
	}

	@Override
	public String toString() {
		return String.format(Locale.US, "%s(%s,%s,%s)",
				FACTORY.getTypeString(), getMinValue(), getModeValue(),
				getMaxValue());
	}

	@Override
	public DblStream createStream() {
		return new DblTriangular(getMinValue(), getModeValue(), getMaxValue());
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

	public double getModeValue() {
		return modeValue;
	}

	public void setModeValue(double modeValue) {
		firePropertyChange(PARAM_MODE_VALUE, this.modeValue,
				this.modeValue = modeValue);
	}

	static {
		registerStreamFactory(DblTriangularDef.FACTORY);
	}

}
