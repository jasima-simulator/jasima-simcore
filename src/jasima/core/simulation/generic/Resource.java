package jasima.core.simulation.generic;

import static jasima.core.simulation.generic.Q.leave;

import jasima.core.simulation.SimContext;
import jasima.core.simulation.SimProcess;
import jasima.core.simulation.SimProcess.MightBlock;

public class Resource {

	private Q<SimProcess<?>> seizedBy;

	public Resource(String string, int numResources) {
		seizedBy = new Q<>();
		setCapacity(numResources);
	}

	public Resource(String string) {
		this(string, 1);
	}

	public void seize() throws MightBlock {
		SimProcess<?> p = SimContext.currentProcess();
		seizedBy.put(p);
	}

	public boolean trySeize() {
		SimProcess<?> p = SimContext.currentProcess();
		return seizedBy.tryPut(p);
	}

	public void release() {
		leave(seizedBy);
	}

	public int numAvailable() {
		return seizedBy.numAvailable();
	}

	public int getCapacity() {
		return seizedBy.getCapacity();
	}

	public void setCapacity(int numResources) {
		seizedBy.setCapacity(numResources);
	}

}
