/*******************************************************************************
 * This file is part of jasima, v1.3, the Java simulator for manufacturing and 
 * logistics.
 *  
 * Copyright (c) 2015 		jasima solutions UG
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
package jasima.shopSim.util;

import static jasima.shopSim.util.BasicJobStatCollector.put;

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
		noProcTime = new SummaryStat("noProcTime");
		lateness = new SummaryStat("lateness");
		weightedFlowtime = new SummaryStat("weightedFlowtimes");
		weightedTardiness = new SummaryStat("weightedTardiness");
		conditionalTardiness = new SummaryStat("conditionalTardiness");
		weightedConditionalTardiness = new SummaryStat("weightedConditionalTardiness");
		numTardyWeighted = 0.0;

		shop = null;
	}

	@Override
	public void done(SimComponent sim) {
		weightedTardinessWithWIP = new SummaryStat(weightedTardiness);
		weightedTardinessWithWIP.setName("weightedTardinessWithWIP");

		if (shop != null)
			for (SimComponent sc : shop.machines().getComponents()) {
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
		put(res, noProcTime);
		put(res, weightedFlowtime);
		put(res, lateness);
		put(res, weightedTardiness);
		put(res, weightedTardinessWithWIP);
		put(res, conditionalTardiness);
		put(res, weightedConditionalTardiness);

		res.put("weightedNumTardy", numTardyWeighted);
	}

	@Override
	public String toString() {
		return "ExtendedJobStatCollector";
	}

}
