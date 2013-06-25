package jasima.shopSim.util.modelDef.streams;

import jasima.core.util.Util;

import java.util.List;

public class IntUniformDefFactory extends StreamDefFact {

	public IntUniformDefFactory() {
		super();
	}

	@Override
	public String getTypeString() {
		return IntUniformDef.getTypeString();
	}

	@Override
	public IntUniformDef stringToStreamDef(String params, List<String> errors) {
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

}
