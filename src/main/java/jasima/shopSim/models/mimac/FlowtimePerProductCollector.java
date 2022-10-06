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
