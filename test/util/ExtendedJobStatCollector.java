/*******************************************************************************
 * This file is part of jasima, v1.3, the Java simulator for manufacturing and 
 * logistics.
 *  
 * Copyright (c) 2015      jasima solutions UG
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package util;

import jasima.core.simulation.Simulation;
import jasima.core.statistics.SummaryStat;
import jasima.core.util.Util;
import jasima.shopSim.core.Job;
import jasima.shopSim.core.JobShop;
import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.WorkStation;
import jasima.shopSim.prioRules.basic.TieBreakerFASFS;
import jasima.shopSim.util.ShopListenerBase;

import java.util.Map;

/**
 * This is an old version now only used to maintain compatibility with old test
 * cases. Don't use for anything new!
 * <p>
 * 
 * 
 * Collects a variety of job statistics: cMax (completion time of last job
 * finished), percentage tardy, lateness, number of tardy jobs, flowtime,
 * tardiness, conditional tardiness, and weighted variants of the latter 4
 * objective functions. A further statistic, "noProcTime", is computed as
 * flowtime minus the sum of all processing times. It therefore only measures
 * reducible components of the flowtime, i.e., waiting and setup times.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version 
 *          "$Id$"
 */
@Deprecated
public class ExtendedJobStatCollector extends ShopListenerBase {

	private static final long serialVersionUID = -6311778884767987852L;

	private SummaryStat lateness;
	private SummaryStat flowtime;
	private SummaryStat noProcTime;
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
		noProcTime = new SummaryStat("noProcTime");
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
		weightedTardinessWithWIP = new SummaryStat(weightedTardiness);

		JobShop shop = (JobShop) sim;
		for (WorkStation m : shop.machines) {
			PR pr = m.queue.getSequencingRule();

			m.queue.setSequencingRule(new TieBreakerFASFS());
			for (Job j : m.queue.getAllElementsInOrder(new Job[m.queue.size()])) {
				storeWIPJob(j);
			}
			m.queue.setSequencingRule(pr);

			for (int i = 0; i < m.numInGroup(); i++) {
				PrioRuleTarget j = m.getProcessedJob(i);
				if (j != null)
					storeWIPJob(j);
			}
		}
	}

	/**
	 * Updates statistics after simulation ended with data from a job that is
	 * still processed on the shop floor.
	 */
	protected void storeWIPJob(PrioRuleTarget job) {
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

		noProcTime.value(ft - j.procSum());

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
		Util.putMeanMaxVar(noProcTime, "noProc", res);
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
		return "ExtendedJobStatCollector";
	}

}
