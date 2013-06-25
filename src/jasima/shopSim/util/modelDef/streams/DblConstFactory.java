package jasima.shopSim.util.modelDef.streams;

import jasima.core.util.Util;

import java.util.List;

public class DblConstFactory extends StreamDefFact {

	public DblConstFactory() {
		super();
	}

	@Override
	public String getTypeString() {
		return DblConstDef.getTypeString();
	}

	@Override
	public DblConstDef stringToStreamDef(String params, List<String> errors) {
		double[] ll;
		try {
			ll = Util.parseDblList(params);
		} catch (NumberFormatException nfe) {
			errors.add(String.format("invalid number: %s",
					nfe.getLocalizedMessage()));
			return null;
		}

		DblConstDef res = new DblConstDef();
		res.setValues(ll);
		return res;
	}
}