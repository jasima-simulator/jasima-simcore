package jasima.core.simulation.generic;

import static jasima.core.simulation.generic.Q.enter;
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
		enter(seizedBy);
	}

	public boolean trySeize() {
		SimProcess<?> p = SimContext.currentProcess();
		return seizedBy.tryPut(p);
	}

	public void release() {
		leave(seizedBy);
	}

	public void seize(int numResources) throws MightBlock {
		if (numResources<1)
			throw new IllegalArgumentException();
		
		for (int i=0; i<numResources; i++) {
			seize();
		}
	}

	public boolean trySeize(int numResources) {
		if (numResources<1)
			throw new IllegalArgumentException();

		if (numAvailable()<numResources)
			return false;
		
		for (int i=0; i<numResources; i++) {
			trySeize();
		}
		return true;
	}

	public void release(int numResources) {
		if (numResources<1)
			throw new IllegalArgumentException();
		
		for (int i=0; i<numResources; i++) {
			release();
		}
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
