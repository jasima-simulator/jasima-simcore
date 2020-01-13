package jasima.core.simulation.generic;

import java.util.ArrayDeque;

public class Resource {
	
	private Channel<?> waiting;
	private Semaphore server;
	private ConditionQueue allBusy;

	public Resource() {
		super();
	}
	
	public void seize() {
		SimProcess<?> p = SimProcess.current();
		
		while (waiting.isFull()) {
			allBusy.await();
		}
		
		
	}
	
	public void release() {
		
	}

}
