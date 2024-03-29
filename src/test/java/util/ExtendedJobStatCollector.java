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
package util;

import java.util.Map;

import jasima.core.simulation.SimComponent;
import jasima.core.statistics.SummaryStat;
import jasima.core.util.Util;
import jasima.shopSim.core.Job;
import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.Shop;
import jasima.shopSim.core.ShopListenerBase;
import jasima.shopSim.core.WorkStation;
import jasima.shopSim.prioRules.basic.TieBreakerFASFS;

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
 * @author Torsten Hildebrandt
 */
@Deprecated
public class ExtendedJobStatCollector extends ShopListenerBase {

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

	private Shop shop;

	@Override
	public void init(SimComponent c) {
		flowtime = new SummaryStat();
		noProcTime = new SummaryStat();
		tardiness = new SummaryStat();
		lateness = new SummaryStat();
		weightedFlowtime = new SummaryStat();
		weightedTardiness = new SummaryStat();
		conditionalTardiness = new SummaryStat();
		weightedConditionalTardiness = new SummaryStat();
		numTardyWeighted = 0.0;
		numTardy = 0;
		numFinished = 0;
		cMax = 0.0;
	}

	@Override
	public void done(SimComponent c) {
		weightedTardinessWithWIP = new SummaryStat(weightedTardiness);

		if (shop != null)
			for (WorkStation m : shop.getMachines()) {
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
	 * Updates statistics after simulation ended with data from a job that is still
	 * processed on the shop floor.
	 */
	protected void storeWIPJob(PrioRuleTarget job) {
		for (int i = 0; i < job.numJobsInBatch(); i++) {
			Job j = job.job(i);
			if (j.isFuture())
				continue;

			double tard = Math.max(j.getShop().simTime() - j.getCurrentOperationDueDate(), 0);
			weightedTardinessWithWIP.value(j.getWeight() * tard);
		}
	}

	@Override
	public void jobReleased(Shop shop, Job j) {
		this.shop = shop;
	}

	@Override
	public void jobFinished(Shop shop, Job j) {
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
	public void produceResults(SimComponent shop, Map<String, Object> res) {
		Util.putMeanMaxVar(flowtime, "flow", res);
		Util.putMeanMaxVar(noProcTime, "noProc", res);
		Util.putMeanMaxVar(weightedFlowtime, "weightedFlow", res);

		Util.putMeanMaxVar(lateness, "late", res);

		Util.putMeanMaxVar(tardiness, "tard", res);
		Util.putMeanMaxVar(weightedTardiness, "weightedTard", res);
		Util.putMeanMaxVar(weightedTardinessWithWIP, "weightedTardWIP", res);

		Util.putMeanMaxVar(conditionalTardiness, "condTard", res);
		Util.putMeanMaxVar(weightedConditionalTardiness, "weightedCondTard", res);

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
