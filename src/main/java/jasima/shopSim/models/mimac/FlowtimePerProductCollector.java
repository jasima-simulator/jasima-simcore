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
package jasima.shopSim.models.mimac;

import java.util.HashMap;
import java.util.Map;

import jasima.core.simulation.SimComponent;
import jasima.core.statistics.SummaryStat;
import jasima.core.util.Util;
import jasima.shopSim.core.Job;
import jasima.shopSim.core.Shop;
import jasima.shopSim.core.ShopListenerBase;

/**
 * Collects flowtimes seperately for each product.
 * 
 * @author Torsten Hildebrandt
 */
public class FlowtimePerProductCollector extends ShopListenerBase {

	private HashMap<Integer, SummaryStat> flowtimesPerProduct;

	@Override
	public void init(SimComponent c) {
		flowtimesPerProduct = new HashMap<Integer, SummaryStat>();
	}

	@Override
	public void jobFinished(Shop shop, Job j) {
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
	public void produceResults(SimComponent c, Map<String, Object> res) {
		for (Integer prodTypeId : flowtimesPerProduct.keySet()) {
			String prefix = "p" + prodTypeId;
			SummaryStat stats = flowtimesPerProduct.get(prodTypeId);

			Util.putMeanMaxVar(stats, prefix + ".flow", res);
			res.put(prefix + ".numFinished", stats.numObs());
		}
	}

}
