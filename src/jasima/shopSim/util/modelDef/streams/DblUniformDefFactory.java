package jasima.shopSim.util.modelDef.streams;

import jasima.core.util.Util;

import java.util.List;

public class DblUniformDefFactory extends StreamDefFact {

	public DblUniformDefFactory() {
		super();
	}

	@Override
	public String getTypeString() {
		return DblUniformDef.getTypeString();
	}

	@Override
	public DblUniformDef stringToStreamDef(String params, List<String> errors) {
		double[] ll;
		try {
			ll = Util.parseDblList(params);
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
		DblUniformDef res = new DblUniformDef();
		res.setMinValue(ll[0]);
		res.setMaxValue(ll[1]);
		return res;
	}

}
