package jasima.core.simulation.generic;

import jasima.core.simulation.SimLifecycleListener.ResultsListener;
import jasima.core.simulation.Simulation;
import jasima.core.simulation.generic.Q.QEvents;
import jasima.core.statistics.TimeWeightedSummaryStat;

public class QLengthStatsCollector {
	// TODO: make this a SimComponent, so we can use lifecycle events?

	// TODO: handle resetStats properly

	private final Q<?> q;
	private final Simulation sim;

	private final TimeWeightedSummaryStat numItems;

	public QLengthStatsCollector(Q<?> q, Simulation sim) {
		super();

		this.q = q;
		this.sim = sim;

		numItems = new TimeWeightedSummaryStat();

		q.addListener((queue, event) -> {
			if (event == QEvents.ITEM_ADDED || event == QEvents.ITEM_REMOVED) {
				recordValue();
			}
		});

		recordValue(); // record initial value

		sim.addListener(ResultsListener.class, res -> {
			res.put(q.toString() + ".qLength", statsNumItems());
		});
	}

	private void recordValue() {
		numItems.value(q.numItems(), sim.simTime());
	}

	public TimeWeightedSummaryStat statsNumItems() {
		TimeWeightedSummaryStat stats = new TimeWeightedSummaryStat(numItems);
		stats.value(q.numItems(), sim.simTime()); // properly "close" stats
		return stats;
	}

}
