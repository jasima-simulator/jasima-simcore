package jasima.core.simulation;

import java.util.Map;

import jasima.core.simulation.SimComponent.ProduceResultsEvent;
import jasima.core.simulation.SimComponent.SimComponentEvent;
import jasima.core.simulation.SimComponent.SimComponentLifeCycleMessage;
import jasima.core.util.observer.NotifierListener;

public interface SimComponentLifecycleListener extends NotifierListener<SimComponent, SimComponentEvent> {

	@Override
	default void inform(SimComponent c, SimComponentEvent msg) {
		if (msg == SimComponentLifeCycleMessage.INIT) {
			init(c);
		} else if (msg == SimComponentLifeCycleMessage.SIM_START) {
			simStart(c);
		} else if (msg == SimComponentLifeCycleMessage.RESET_STATS) {
			resetStats(c);
		} else if (msg == SimComponentLifeCycleMessage.SIM_END) {
			simEnd(c);
		} else if (msg == SimComponentLifeCycleMessage.DONE) {
			done(c);
		} else if (msg instanceof ProduceResultsEvent) {
			ProduceResultsEvent pe = (ProduceResultsEvent) msg;
			produceResults(c, pe.resultMap);
		} else {
			handleOther(c, msg);
		}
	}

	default void init(SimComponent c) {
	}

	@FunctionalInterface
	public interface InitListener extends SimComponentLifecycleListener {
		@Override
		void init(SimComponent c);
	}

	default void simStart(SimComponent c) {
	}

	@FunctionalInterface
	public interface SimStartListener extends SimComponentLifecycleListener {
		@Override
		void simStart(SimComponent c);
	}

	default void resetStats(SimComponent c) {
	}

	@FunctionalInterface
	public interface ResetStatsListener extends SimComponentLifecycleListener {
		@Override
		void resetStats(SimComponent c);
	}

	default void simEnd(SimComponent c) {
	}

	@FunctionalInterface
	public interface SimEndListener extends SimComponentLifecycleListener {
		@Override
		void simEnd(SimComponent c);
	}

	default void done(SimComponent c) {
	}

	@FunctionalInterface
	public interface DoneListener extends SimComponentLifecycleListener {
		@Override
		void done(SimComponent c);
	}

	default void produceResults(SimComponent c, Map<String, Object> resultMap) {
	}

	@FunctionalInterface
	public interface ResultsListener extends SimComponentLifecycleListener {
		@Override
		void produceResults(SimComponent c, Map<String, Object> resultMap);
	}

	default void handleOther(SimComponent c, Object msg) {
	}

	@FunctionalInterface
	public interface OtherListener extends SimComponentLifecycleListener {
		@Override
		void handleOther(SimComponent c, Object msg);
	}

}
