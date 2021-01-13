package jasima.core.simulation.generic;

import jasima.core.simulation.SimComponentLifeCycleListener.ResultsListener;
import jasima.core.simulation.SimContext;
import jasima.core.simulation.Simulation;
import jasima.core.simulation.generic.Q.QEvents;
import jasima.core.statistics.TimeWeightedSummaryStat;

public class QStatCollector<ITEM> {
	private final Q<ITEM> q;
	private final Simulation sim;

	private final TimeWeightedSummaryStat numItems;

	public QStatCollector(Q<ITEM> q, Simulation sim) {
		super();

		this.q = q;
		this.sim = sim;

		numItems = new TimeWeightedSummaryStat();

		q.addListener((queue, event) -> {
			if (event == QEvents.ITEM_ADDED || event == QEvents.ITEM_REMOVED) {
				recordValue();
			}
		});

		recordValue();

		sim.getRootComponent().addListener(ResultsListener.class, (sc, res) -> {
			TimeWeightedSummaryStat stats = new TimeWeightedSummaryStat(numItems);
			stats.value(q.numItems(), sim.simTime()); // properly "close" stats
			res.put("qStats", stats);
		});
	}

	private void recordValue() {
		SimContext.trace(numItems.mean(), q.numItems(), numItems);
		numItems.value(q.numItems(), sim.simTime());
		SimContext.trace(numItems.mean(),numItems);
	}

}
