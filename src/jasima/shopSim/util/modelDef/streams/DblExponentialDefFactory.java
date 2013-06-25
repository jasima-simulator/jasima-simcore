package jasima.shopSim.util.modelDef.streams;


import java.util.List;

public class DblExponentialDefFactory extends StreamDefFact {

	public DblExponentialDefFactory() {
		super();
	}

	@Override
	public String getTypeString() {
		return DblExponentialDef.getTypeString();
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

}
