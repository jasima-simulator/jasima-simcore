import jasima.core.simulation.SimComponentBase;
import jasima.core.simulation.SimProcess.MightBlock;

public abstract class SimEntity extends SimComponentBase {

	public abstract void lifecycle() throws MightBlock;
	
}
