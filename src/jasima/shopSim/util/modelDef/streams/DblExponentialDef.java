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

import jasima.core.random.continuous.DblDistribution;
import jasima.core.random.continuous.DblStream;

import java.util.List;

import org.apache.commons.math3.distribution.ExponentialDistribution;

public class DblExponentialDef extends DblStreamDef {

	public static final String PARAM_MEAN = "mean";
	public static final String TYPE_STRING = "dblExp";

	public static final StreamDefFact FACTORY = new StreamDefFact() {
		@Override
		public String getTypeString() {
			return TYPE_STRING;
		}

		@Override
		public DblExponentialDef stringToStreamDef(String params,
				List<String> errors) {
			double d;
			try {
				d = Double.parseDouble(params);
			} catch (NumberFormatException nfe) {
				errors.add(String.format("invalid number: %s",
						nfe.getLocalizedMessage()));
				return null;
			}

			DblExponentialDef res = new DblExponentialDef();
			res.setMean(d);
			return res;
		}

		@Override
		public DblStreamDef streamToStreamDef(DblStream stream) {
			if (stream instanceof DblDistribution) {
				DblDistribution s = (DblDistribution) stream;
				if (s.getDistribution() instanceof ExponentialDistribution) {
					ExponentialDistribution dist = (ExponentialDistribution) s
							.getDistribution();
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
		return String.format("%s(%s)", FACTORY.getTypeString(), getMean());
	}

	@Override
	public DblStream createStream() {
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
