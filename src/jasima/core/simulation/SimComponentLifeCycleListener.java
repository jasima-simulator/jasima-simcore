package jasima.core.simulation;

import java.util.Map;

import jasima.core.simulation.Simulation.ProduceResultsEvent;
import jasima.core.simulation.Simulation.SimComponentLifeCycleEvent;
import jasima.core.util.observer.NotifierListener;

public interface SimComponentLifeCycleListener extends NotifierListener<SimComponent, Object> {

	default void inform(SimComponent o, Object msg) {
		if (msg == SimComponentLifeCycleEvent.INIT) {
			init(o);
		} else if (msg == SimComponentLifeCycleEvent.BEFORE_RUN) {
			simStart(o);
		} else if (msg == SimComponentLifeCycleEvent.AFTER_RUN) {
			simEnd(o);
		} else if (msg == SimComponentLifeCycleEvent.DONE) {
			done(o);
		} else if (msg instanceof ProduceResultsEvent) {
			ProduceResultsEvent pe = (ProduceResultsEvent) msg;
			produceResults(o, pe.resultMap);
		} else {
			handleOther(o, msg);
		}
	}

	default void init(SimComponent c) {
	}

	default void simStart(SimComponent c) {
	}

	default void simEnd(SimComponent c) {
	}

	default void done(SimComponent c) {
	}

	default void produceResults(SimComponent c, Map<String, Object> resultMap) {
	}

	default void handleOther(SimComponent c, Object msg) {
	}

}
