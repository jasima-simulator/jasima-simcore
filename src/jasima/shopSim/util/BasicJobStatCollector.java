/*******************************************************************************
 * Copyright (c) 2010-2013 Torsten Hildebrandt and jasima contributors
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
 *
 * $Id$
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
 * Collects some basic job statistics: cMax (completion time of last job
 * finished), percentage tardy, lateness, number of tardy jobs, flowtime,
 * tardiness, conditional tardiness, and weighted variants of the latter 4
 * objective functions.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version 
 *          "$Id$"
 */
public class BasicJobStatCollector extends JobShopListenerBase {

	private static final long serialVersionUID = -6311778884767987852L;

	private SummaryStat lateness;
	private SummaryStat flowtime;
	private SummaryStat weightedFlowtime;
	private SummaryStat tardiness;
	private SummaryStat weightedTardiness;
	private SummaryStat conditionalTardiness;
	private SummaryStat weightedConditionalTardiness;
	private SummaryStat weightedTardinessWithWIP;
	private int numTardy;
	private double numTardyWeighted;
	private int numFinished;
	private double cMax;

	@Override
	protected void init(Simulation sim) {
		flowtime = new SummaryStat("flowtimes");
		tardiness = new SummaryStat("tardiness");
		lateness = new SummaryStat("lateness");
		weightedFlowtime = new SummaryStat("weightedFlowtimes");
		weightedTardiness = new SummaryStat("weightedTardiness");
		conditionalTardiness = new SummaryStat("conditionalTardiness");
		weightedConditionalTardiness = new SummaryStat(
				"weightedConditionalTardiness");
		numTardyWeighted = 0.0;
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
			for (int i = 0; i < m.numInGroup(); i++) {
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

		double ft = shop.simTime() - j.getRelDate();
		flowtime.value(ft);
		weightedFlowtime.value(j.getWeight() * ft);

		double late = shop.simTime() - j.getDueDate();
		lateness.value(late);

		double tard = Math.max(late, 0);
		double wTard = j.getWeight() * tard;
		tardiness.value(tard);
		weightedTardiness.value(wTard);

		if (tard > 0.0) {
			conditionalTardiness.value(tard);
			weightedConditionalTardiness.value(wTard);
			numTardy++;
			numTardyWeighted += j.getWeight();
		}

		numFinished++;
	}

	@Override
	public void produceResults(Simulation sim, Map<String, Object> res) {
		Util.putMeanMaxVar(flowtime, "flow", res);
		Util.putMeanMaxVar(weightedFlowtime, "weightedFlow", res);

		Util.putMeanMaxVar(lateness, "late", res);

		Util.putMeanMaxVar(tardiness, "tard", res);
		Util.putMeanMaxVar(weightedTardiness, "weightedTard", res);
		Util.putMeanMaxVar(weightedTardinessWithWIP, "weightedTardWIP", res);

		Util.putMeanMaxVar(conditionalTardiness, "condTard", res);
		Util.putMeanMaxVar(weightedConditionalTardiness, "weightedCondTard",
				res);

		res.put("tardPercentage", ((double) numTardy) / numFinished);
		res.put("numTardy", numTardy);
		res.put("weightedNumTardy", numTardyWeighted);

		res.put("cMax", cMax);
	}

	@Override
	public String toString() {
		return "BasicJobStatCollector";
	}

}
