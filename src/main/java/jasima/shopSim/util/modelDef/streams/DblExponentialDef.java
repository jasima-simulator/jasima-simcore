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

import org.apache.commons.math3.distribution.ExponentialDistribution;

import jasima.core.random.continuous.DblDistribution;
import jasima.core.random.continuous.DblSequence;

public class DblExponentialDef extends DblStreamDef {

	private static final long serialVersionUID = 332888720647354355L;

	public static final String PARAM_MEAN = "mean";
	public static final String TYPE_STRING = "dblExp";

	public static final StreamDefFact FACTORY = new StreamDefFact() {
		@Override
		public String getTypeString() {
			return TYPE_STRING;
		}

		@Override
		public DblExponentialDef stringToStreamDef(String params, List<String> errors) {
			double d;
			try {
				d = Double.parseDouble(params);
			} catch (NumberFormatException nfe) {
				errors.add(defFormat("invalid number: %s", nfe.getLocalizedMessage()));
				return null;
			}

			DblExponentialDef res = new DblExponentialDef();
			res.setMean(d);
			return res;
		}

		@Override
		public DblStreamDef streamToStreamDef(DblSequence stream) {
			if (stream instanceof DblDistribution) {
				DblDistribution s = (DblDistribution) stream;
				if (s.getDistribution() instanceof ExponentialDistribution) {
					ExponentialDistribution dist = (ExponentialDistribution) s.getDistribution();
					DblExponentialDef def = new DblExponentialDef();
					def.setMean(dist.getMean());
					return def;
				}
			}

			return null;
		}

	};

	private double mean = 1.0;

	public DblExponentialDef() {
		super();
	}

	@Override
	public String toString() {
		return defFormat("%s(%s)", FACTORY.getTypeString(), getMean());
	}

	@Override
	public DblSequence createStream() {
		return new DblDistribution(new ExponentialDistribution(getMean()));
	}

	public double getMean() {
		return mean;
	}

	public void setMean(double mean) {
		firePropertyChange(PARAM_MEAN, this.mean, this.mean = mean);
	}

	static {
		registerStreamFactory(DblExponentialDef.FACTORY);
	}

}
