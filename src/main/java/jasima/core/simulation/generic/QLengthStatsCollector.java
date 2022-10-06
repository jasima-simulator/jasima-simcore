/*
This file is part of jasima, the Java simulator for manufacturing and logistics.
 
Copyright 2010-2022 jasima contributors (see license.txt)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
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
