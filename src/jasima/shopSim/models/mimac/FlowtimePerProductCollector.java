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
package jasima.shopSim.models.mimac;

import jasima.core.simulation.Simulation;
import jasima.core.statistics.SummaryStat;
import jasima.core.util.Util;
import jasima.shopSim.core.Job;
import jasima.shopSim.core.JobShop;
import jasima.shopSim.util.JobShopListenerBase;

import java.util.HashMap;
import java.util.Map;

/**
 * Collects flowtimes seperately for each product.
 * 
 * @version $Id$
 */
public class FlowtimePerProductCollector extends JobShopListenerBase {

	private HashMap<Integer, SummaryStat> flowtimesPerProduct;

	@Override
	protected void init(Simulation sim) {
		flowtimesPerProduct = new HashMap<Integer, SummaryStat>();
	}

	@Override
	protected void jobFinished(JobShop shop, Job j) {
		super.jobFinished(shop, j);

		if (!shouldCollect(j))
			return;

		double flowtime = shop.simTime() - j.getRelDate();

		SummaryStat stats = flowtimesPerProduct.get(j.getJobType());
		if (stats == null) {
			stats = new SummaryStat();
			flowtimesPerProduct.put(j.getJobType(), stats);
		}
		stats.value(flowtime);
	}

	@Override
	protected void produceResults(Simulation sim, Map<String, Object> res) {
		for (Integer prodTypeId : flowtimesPerProduct.keySet()) {
			String prefix = "p" + prodTypeId;
			SummaryStat stats = flowtimesPerProduct.get(prodTypeId);

			Util.putMeanMaxVar(stats, prefix + ".flow", res);
			res.put(prefix + ".numFinished", stats.numObs());
		}
	}

}
