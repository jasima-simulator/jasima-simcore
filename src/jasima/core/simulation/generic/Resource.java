package jasima.core.simulation.generic;

import jasima.core.util.observer.ObservableValue;
import jasima.core.util.observer.ObservableValues;

public class Resource {

	private ObservableValue<Boolean> canSeize;
	
	public Resource(String string, int i) {
		canSeize = ObservableValues.observable(true);
	}

	public Resource(String string) {
		this(string, 1);
	}
	
	public void seize() {
	}

	public boolean trySeize() {
		return false;
	}

	public ObservableValue<Boolean>  canSeize() {
		return canSeize;
	}

	public void release() {
	}

	// private Channel<?> waiting;
//	private Semaphore server;
//	private ConditionQueue allBusy;
//
//	public Resource() {
//		super();
//	}
//	
//	public void seize() {
//		SimProcess<?> p = SimProcess.current();
//		
//		while (waiting.isFull()) {
//			allBusy.await();
//		}
//		
//		
//	}
//	
//	public void release() {
//		
//	}

}
