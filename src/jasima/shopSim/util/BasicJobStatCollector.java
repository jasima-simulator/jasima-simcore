/*******************************************************************************
 * Copyright 2011, 2012 Torsten Hildebrandt and BIBA - Bremer Institut für Produktion und Logistik GmbH
 *
 * This file is part of jasima, v1.0.
 *
 * jasima is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jasima is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jasima.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package jasima.shopSim.util;

import jasima.core.simulation.Simulation;
import jasima.core.statistics.SummaryStat;
import jasima.core.util.Util;
import jasima.shopSim.core.Job;
import jasima.shopSim.core.JobShop;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.WorkStation;

import java.util.Map;

/**
 * Collects some basic job statistics like flowtime, tardiness, ...
 *  
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version $Id$
 */
public class BasicJobStatCollector extends JobShopListenerBase {

	private static final long serialVersionUID = -6311778884767987852L;

	private SummaryStat flowtimes;
	private SummaryStat weightedFlowtimes;
	private SummaryStat tardiness;
	private SummaryStat lateness;
	private SummaryStat weightedTardiness;
	private SummaryStat conditionalTardiness;
	private SummaryStat weightedTardinessWithWIP;
	private int numTardy;
	private int numFinished;
	private double cMax;

	@Override
	protected void init(Simulation sim) {
		flowtimes = new SummaryStat("flowtimes");
		tardiness = new SummaryStat("tardiness");
		lateness = new SummaryStat("lateness");
		weightedFlowtimes = new SummaryStat("weightedFlowtimes");
		weightedTardiness = new SummaryStat("weightedTardiness");
		conditionalTardiness = new SummaryStat("conditionalTardiness");
		numTardy = 0;
		numFinished = 0;
		cMax = 0.0;
	}

	@Override
	protected void done(Simulation sim) {
		JobShop shop = (JobShop) sim;
		
		weightedTardinessWithWIP = new SummaryStat(weightedTardiness);
		for (WorkStation m : shop.machines) {
			for (int i = 0, n = m.queue.size(); i < n; i++) {
				storeWIPJob(m.queue.get(i));
			}
			for (int i = 0; i < m.numInGroup; i++) {
				PrioRuleTarget j = m.getProcessedJob(i);
				if (j != null)
					storeWIPJob(j);
			}
		}
	}

	private void storeWIPJob(PrioRuleTarget job) {
		for (int i = 0; i < job.numJobsInBatch(); i++) {
			Job j = job.job(i);
			if (j.isFuture())
				continue;

			double tard = Math.max(
					j.getShop().simTime() - j.getCurrentOperationDueDate(), 0);
			weightedTardinessWithWIP.value(j.getWeight() * tard);
		}
	}

	@Override
	protected void jobReleased(JobShop shop, Job j) {

	}

	@Override
	protected void jobFinished(JobShop shop, Job j) {
		if (!shouldCollect(j))
			return;

		assert shop.simTime() >= cMax;
		cMax = shop.simTime();

		double flowtime = shop.simTime() - j.getRelDate();
		flowtimes.value(flowtime);

		double late = shop.simTime() - j.getDueDate();
		lateness.value(late);

		double tard = Math.max(late, 0);
		tardiness.value(tard);

		weightedTardiness.value(j.getWeight() * tard);
		weightedFlowtimes.value(j.getWeight() * flowtime);

		if (tard > 0.0) {
			conditionalTardiness.value(tard);
			numTardy++;
		}

		numFinished++;
	}

	@Override
	public void produceResults(Simulation sim, Map<String, Object> res) {
		Util.putMeanMaxVar(flowtimes, "flow", res);
		Util.putMeanMaxVar(weightedFlowtimes, "weightedFlow", res);
		Util.putMeanMaxVar(lateness, "late", res);
		Util.putMeanMaxVar(tardiness, "tard", res);
		Util.putMeanMaxVar(weightedTardiness, "weightedTard", res);
		Util.putMeanMaxVar(conditionalTardiness, "condTard", res);
		Util.putMeanMaxVar(weightedTardinessWithWIP, "weightedTardWIP", res);

		res.put("tardPercentage", ((double) numTardy) / numFinished);
		res.put("cMax", cMax);
	}

	@Override
	public String toString() {
		return "BasicJobStatCollector";
	}

}
