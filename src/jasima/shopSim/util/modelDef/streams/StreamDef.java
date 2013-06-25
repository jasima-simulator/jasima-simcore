package jasima.shopSim.util.modelDef.streams;

import jasima.core.random.continuous.DblStream;
import jasima.shopSim.util.modelDef.PropertySupport;

public abstract class StreamDef extends PropertySupport {

	public abstract DblStream createStream();

}
