/*******************************************************************************
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This file is part of jasima, v1.2.
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
import jasima.shopSim.core.Job;
import jasima.shopSim.core.JobShop;

import java.util.Map;

/**
 * Collects a variety of job statistics: cMax (completion time of last job
 * finished), percentage tardy, number of tardy jobs, flowtime, tardiness. For
 * additional kpi's see {@link ExtendedJobStatCollector}.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version 
 *          "$Id$"
 * @see ExtendedJobStatCollector
 */
public class BasicJobStatCollector extends ShopListenerBase {

	private static final long serialVersionUID = -4011992602302111428L;

	private SummaryStat flowtime;
	private SummaryStat tardiness;
	private int numTardy;
	private int numFinished;
	private double cMax;

	@Override
	protected void init(Simulation sim) {
		flowtime = new SummaryStat("flowtime");
		tardiness = new SummaryStat("tardiness");
		numTardy = 0;
		numFinished = 0;
		cMax = 0.0;
	}

	@Override
	protected void jobFinished(JobShop shop, Job j) {
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
	public void produceResults(Simulation sim, Map<String, Object> res) {
		put(res, flowtime);
		put(res, tardiness);

		res.put("tardPercentage", ((double) numTardy) / numFinished);
		res.put("numTardy", numTardy);

		res.put("cMax", cMax);
	}

	public static void put(Map<String, Object> res, SummaryStat ss) {
		res.put(ss.getName(), ss);
	}

	@Override
	public String toString() {
		return "BasicJobStatCollector";
	}

}
