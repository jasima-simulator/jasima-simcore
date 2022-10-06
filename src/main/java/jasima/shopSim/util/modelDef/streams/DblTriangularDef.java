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

import java.util.List;
import java.util.Locale;

import jasima.core.random.continuous.DblSequence;
import jasima.core.random.continuous.DblTriangular;
import jasima.core.util.Util;

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
		public DblTriangularDef stringToStreamDef(String params, List<String> errors) {
			double[] ll;
			try {
				ll = Util.parseDblList(params);
			} catch (NumberFormatException nfe) {
				errors.add(defFormat("invalid number: %s", nfe.getLocalizedMessage()));
				return null;
			}
			if (ll.length != 3) {
				errors.add(defFormat(
						"invalid number of parameters (3 required, min, mode, and max value): '%s'", params));
				return null;
			}

			DblTriangularDef res = new DblTriangularDef();
			res.setMinValue(ll[0]);
			res.setModeValue(ll[1]);
			res.setMaxValue(ll[2]);
			return res;
		}

		@Override
		public DblStreamDef streamToStreamDef(DblSequence stream) {
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
		return String.format(Locale.US, "%s(%s,%s,%s)", FACTORY.getTypeString(), getMinValue(), getModeValue(),
				getMaxValue());
	}

	@Override
	public DblSequence createStream() {
		return new DblTriangular(getMinValue(), getModeValue(), getMaxValue());
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

	public double getModeValue() {
		return modeValue;
	}

	public void setModeValue(double modeValue) {
		firePropertyChange(PARAM_MODE_VALUE, this.modeValue, this.modeValue = modeValue);
	}

	static {
		registerStreamFactory(DblTriangularDef.FACTORY);
	}

}
