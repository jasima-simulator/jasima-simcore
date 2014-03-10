package jasima.shopSim.util.modelDef.streams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import jasima.core.random.continuous.DblStream;
import jasima.shopSim.util.modelDef.PropertySupport;

public abstract class StreamDef extends PropertySupport {

	public interface StreamDefFact {

		public String getTypeString();

		public StreamDef stringToStreamDef(String params, List<String> errors);

	}

	public abstract DblStream createStream();

	public static StreamDef parseDblStream(String s, List<String> errors) {
		StringTokenizer sst = new StringTokenizer(s, "()", false);
		ArrayList<String> ss = new ArrayList<String>();
		while (sst.hasMoreTokens()) {
			ss.add(sst.nextToken().trim());
		}
		if (ss.size() != 2) {
			errors.add("invalid stream configuration '" + s + "'");
			return null;
		}

		String type = ss.get(0);
		String parms = ss.get(1);

		StreamDefFact fact = streamFactoryReg.get(type);
		if (fact == null)
			errors.add(String.format(
					"Invalid stream type '%s'. Supported types are: '%s'.",
					type,
					streamFactoryReg.keySet().toString()
							.replaceAll("[\\[\\]]", "")));

		StreamDef res = fact.stringToStreamDef(parms, errors);
		return res;
	}

	private static HashMap<String, StreamDefFact> streamFactoryReg = new HashMap<String, StreamDefFact>();

	public static void registerStreamFactory(StreamDefFact fact) {
		streamFactoryReg.put(fact.getTypeString(), fact);
	}

	static {
		registerStreamFactory(DblConstDef.FACTORY);
		registerStreamFactory(DblExponentialDef.FACTORY);
		registerStreamFactory(DblUniformDef.FACTORY);
		registerStreamFactory(DblTriangularDef.FACTORY);
		registerStreamFactory( IntUniformDef.FACTORY);
		registerStreamFactory( IntEmpDef.FACTORY);
	}

}
