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
