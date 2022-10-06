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
package jasima.shopSim.util;

import java.util.Map;

import jasima.core.simulation.SimComponent;
import jasima.core.statistics.SummaryStat;
import jasima.shopSim.core.Job;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.Shop;
import jasima.shopSim.core.ShopListenerBase;
import jasima.shopSim.core.WorkStation;

/**
 * Collects a variety of job statistics that are not produced by the
 * {@link BasicJobStatCollector}.
 * 
 * <ul>
 * <li>lateness
 * <li>noProcTime (flowtime minus the sum of all processing times. It therefore
 * only measures reducible components of the flowtime, i.e., waiting and setup
 * times.)
 * <li>weightedFlowtime
 * <li>weightedTardiness
 * <li>conditionalTardiness
 * <li>weightedConditionalTardiness
 * <li>weightedTardinessWithWIP
 * <li>numTardyWeighted
 * </ul>
 * 
 * @author Torsten Hildebrandt
 * @see BasicJobStatCollector
 */
public class ExtendedJobStatCollector extends ShopListenerBase {

	private SummaryStat lateness;
	private SummaryStat noProcTime;
	private SummaryStat weightedFlowtime;
	private SummaryStat weightedTardiness;
	private SummaryStat conditionalTardiness;
	private SummaryStat weightedConditionalTardiness;
	private SummaryStat weightedTardinessWithWIP;
	private double numTardyWeighted;
	private Shop shop;

	public ExtendedJobStatCollector() {
		super();
	}

	@Override
	public void init(SimComponent sim) {
		noProcTime = new SummaryStat();
		lateness = new SummaryStat();
		weightedFlowtime = new SummaryStat();
		weightedTardiness = new SummaryStat();
		conditionalTardiness = new SummaryStat();
		weightedConditionalTardiness = new SummaryStat();
		numTardyWeighted = 0.0;

		shop = null;
	}

	@Override
	public void done(SimComponent sim) {
		weightedTardinessWithWIP = new SummaryStat(weightedTardiness);

		if (shop != null)
			for (SimComponent sc : shop.machines().getChildren()) {
				WorkStation m = (WorkStation) sc;
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
		assert this.shop == null || this.shop == shop;

		this.shop = shop;
	}

	@Override
	public void jobFinished(Shop shop, Job j) {
		if (!shouldCollect(j))
			return;

		double ft = shop.simTime() - j.getRelDate();
		weightedFlowtime.value(j.getWeight() * ft);

		noProcTime.value(ft - j.procSum());

		double late = shop.simTime() - j.getDueDate();
		lateness.value(late);

		double tard = Math.max(late, 0);
		double wTard = j.getWeight() * tard;
		weightedTardiness.value(wTard);

		if (tard > 0.0) {
			conditionalTardiness.value(tard);
			weightedConditionalTardiness.value(wTard);
			numTardyWeighted += j.getWeight();
		}
	}

	@Override
	public void produceResults(SimComponent sim, Map<String, Object> res) {
		res.put("noProcTime", noProcTime);
		res.put("weightedFlowtimes", weightedFlowtime);
		res.put("lateness", lateness);
		res.put("weightedTardiness", weightedTardiness);
		res.put("weightedTardinessWithWIP", weightedTardinessWithWIP);
		res.put("conditionalTardiness", conditionalTardiness);
		res.put("weightedConditionalTardiness", weightedConditionalTardiness);

		res.put("weightedNumTardy", numTardyWeighted);
	}

	@Override
	public String toString() {
		return "ExtendedJobStatCollector";
	}

}
