package jasima.shopSim.util.modelDef.streams;

import jasima.core.random.continuous.DblStream;
import jasima.core.random.continuous.DblTriangular;
import jasima.core.util.Util;

import java.util.List;
import java.util.Locale;

public class DblTriangularDef extends StreamDef {

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
				errors.add(String.format("invalid number: %s",
						nfe.getLocalizedMessage()));
				return null;
			}
			if (ll.length != 3) {
				errors.add(String
						.format("invalid number of parameters (3 required, min, mode, and max value): '%s'",
								params));
				return null;
			}

			DblTriangularDef res = new DblTriangularDef();
			res.setMinValue(ll[0]);
			res.setModeValue(ll[1]);
			res.setMaxValue(ll[2]);
			return res;
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
		return String.format(Locale.US, "%s(%s,%s,%s)", FACTORY.getTypeString(),
				getMinValue(), getModeValue(), getMaxValue());
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
