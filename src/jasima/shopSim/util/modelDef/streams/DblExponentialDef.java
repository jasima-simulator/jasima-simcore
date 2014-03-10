package jasima.shopSim.util.modelDef.streams;

import jasima.core.random.continuous.DblDistribution;
import jasima.core.random.continuous.DblStream;

import java.util.List;

import org.apache.commons.math3.distribution.ExponentialDistribution;

public class DblExponentialDef extends StreamDef {

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

	};

	private double mean = 1.0;

	public DblExponentialDef() {
		super();
	}

	@Override
	public String toString() {
		String params = Double.toString(getMean());
		return String.format("%s(%s)", FACTORY.getTypeString(), params);
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

}
