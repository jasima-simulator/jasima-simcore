package jasima.shopSim.util.modelDef.streams;

import jasima.core.random.continuous.DblStream;
import jasima.core.random.discrete.IntUniformRange;
import jasima.core.util.Util;

import java.util.List;

public class IntUniformDef extends StreamDef {

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
				errors.add(String.format("invalid number: %s",
						nfe.getLocalizedMessage()));
				return null;
			}
			if (ll.length != 2) {
				errors.add(String
						.format("invalid number of parameters (2 required, min and max value): '%s'",
								params));
				return null;
			}

			IntUniformDef res = new IntUniformDef();
			res.setMinValue(ll[0]);
			res.setMaxValue(ll[1]);
			return res;
		}

	};

	private int minValue = 0;
	private int maxValue = 10;

	public IntUniformDef() {
		super();
	}

	@Override
	public String toString() {
		return String.format("%s(%d,%d)", FACTORY.getTypeString(), getMinValue(),
				getMaxValue());
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

}
