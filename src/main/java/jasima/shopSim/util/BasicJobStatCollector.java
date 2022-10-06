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
import jasima.shopSim.core.Shop;
import jasima.shopSim.core.ShopListenerBase;

/**
 * Collects a variety of job statistics: cMax (completion time of last job
 * finished), percentage tardy, number of tardy jobs, flowtime, tardiness. For
 * additional kpi's see {@link ExtendedJobStatCollector}.
 * 
 * @author Torsten Hildebrandt
 * @see ExtendedJobStatCollector
 */
public class BasicJobStatCollector extends ShopListenerBase {

	private SummaryStat flowtime;
	private SummaryStat tardiness;
	private int numTardy;
	private int numFinished;
	private double cMax;

	public BasicJobStatCollector() {
		super();
	}

	@Override
	public void init(SimComponent c) {
		flowtime = new SummaryStat();
		tardiness = new SummaryStat();
		numTardy = 0;
		numFinished = 0;
		cMax = 0.0;
	}

	@Override
	public void jobFinished(Shop shop, Job j) {
		if (!shouldCollect(j))
			return;

		cMax = shop.simTime();

		double ft = shop.simTime() - j.getRelDate();
		flowtime.value(ft);

		double late = shop.simTime() - j.getDueDate();
		double tard = Math.max(late, 0);
		tardiness.value(tard);

		if (tard > 0.0) {
			numTardy++;
		}

		numFinished++;
	}

	@Override
	public void produceResults(SimComponent c, Map<String, Object> res) {
		res.put("flowtime", flowtime);
		res.put("tardiness", tardiness);

		res.put("tardPercentage", ((double) numTardy) / numFinished);
		res.put("numTardy", numTardy);

		res.put("cMax", cMax);
	}

	@Override
	public String toString() {
		return "BasicJobStatCollector";
	}

}
