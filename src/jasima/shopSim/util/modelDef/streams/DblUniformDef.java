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

import java.util.List;

import jasima.core.random.continuous.DblSequence;
import jasima.core.random.continuous.DblUniformRange;
import jasima.core.util.Util;
import jasima.core.util.i18n.I18n;

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
		public DblUniformDef stringToStreamDef(String params, List<String> errors) {
			double[] ll;
			try {
				ll = Util.parseDblList(params);
			} catch (NumberFormatException nfe) {
				errors.add(I18n.defFormat("invalid number: %s", nfe.getLocalizedMessage()));
				return null;
			}
			if (ll.length != 2) {
				errors.add(
						I18n.defFormat("invalid number of parameters (2 required, min and max value): '%s'", params));
				return null;
			}
			DblUniformDef res = new DblUniformDef();
			res.setMinValue(ll[0]);
			res.setMaxValue(ll[1]);
			return res;
		}

		@Override
		public DblStreamDef streamToStreamDef(DblSequence stream) {
			if (stream instanceof DblUniformRange) {
				DblUniformRange s = (DblUniformRange) stream;
				DblUniformDef def = new DblUniformDef();

				def.setMinValue(s.getMin());
				def.setMaxValue(s.getMax());

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
		return I18n.defFormat("%s(%s,%s)", FACTORY.getTypeString(), getMinValue(), getMaxValue());
	}

	@Override
	public DblSequence createStream() {
		return new DblUniformRange(getMinValue(), getMaxValue());
	}

	public double getMinValue() {
		return minValue;
	}

	public void setMinValue(double minValue) {
		firePropertyChange(PARAM_MIN_VALUE, this.minValue, this.minValue = minValue);
	}

	public double getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(double maxValue) {
		firePropertyChange(PARAM_MAX_VALUE, this.maxValue, this.maxValue = maxValue);
	}

	static {
		registerStreamFactory(DblUniformDef.FACTORY);
	}

}
