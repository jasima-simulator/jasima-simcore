package jasima.core.simulation;

import java.util.Map;

import jasima.core.simulation.SimComponent.ProduceResultsMessage;
import jasima.core.simulation.SimComponent.SimComponentEvent;
import jasima.core.simulation.SimComponent.SimComponentLifeCycleMessage;
import jasima.core.util.observer.NotifierListener;

public interface SimComponentLifeCycleListener extends NotifierListener<SimComponent, SimComponentEvent> {

	@Override
	default void inform(SimComponent o, SimComponentEvent msg) {
		if (msg == SimComponentLifeCycleMessage.INIT) {
			init(o);
		} else if (msg == SimComponentLifeCycleMessage.BEFORE_RUN) {
			simStart(o);
		} else if (msg == SimComponentLifeCycleMessage.AFTER_RUN) {
			simEnd(o);
		} else if (msg == SimComponentLifeCycleMessage.DONE) {
			done(o);
		} else if (msg instanceof ProduceResultsMessage) {
			ProduceResultsMessage pe = (ProduceResultsMessage) msg;
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
