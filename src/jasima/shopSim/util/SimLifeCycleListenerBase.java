package jasima.shopSim.util;

import java.util.Map;

import jasima.core.simulation.Simulation;
import jasima.core.simulation.Simulation.ProduceResultsEvent;
import jasima.core.simulation.Simulation.SimLifeCycleEvent;
import jasima.core.util.observer.NotifierService;
import jasima.core.util.observer.Subscriber;

public class SimLifeCycleListenerBase implements Subscriber {

	public SimLifeCycleListenerBase() {
		super();
	}

	@Override
	public void register(NotifierService ns) {
		ns.addSubscription(SimLifeCycleEvent.class, this);
	}

	@Override
	public void inform(Object o, Object e) {
		if (e == SimLifeCycleEvent.INIT) {
			init((Simulation) o);
		} else if (e == SimLifeCycleEvent.BEFORE_RUN) {
			simStart((Simulation) o);
		} else if (e == SimLifeCycleEvent.AFTER_RUN) {
			simEnd((Simulation) o);
		} else if (e == SimLifeCycleEvent.DONE) {
			done((Simulation) o);
		} else if (e instanceof ProduceResultsEvent) {
			ProduceResultsEvent pe = (ProduceResultsEvent) e;
			produceResults((Simulation) o, pe.resultMap);
		} else {
			handleOther(o, e);
		}
	}

	protected void init(Simulation sim) {
	}

	protected void simStart(Simulation sim) {
	}

	protected void simEnd(Simulation sim) {
	}

	protected void done(Simulation sim) {
	}

	protected void produceResults(Simulation sim, Map<String, Object> resultMap) {
	}

	protected void handleOther(Object o, Object e) {
	}

}
