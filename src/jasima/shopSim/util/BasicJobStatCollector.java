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
import jasima.shopSim.core.Job;
import jasima.shopSim.core.JobShop;

import java.util.Map;

/**
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
public class BasicJobStatCollector extends ShopListenerBase {

	private static final long serialVersionUID = -4380086876136803696L;

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

	private void put(Map<String, Object> res, SummaryStat ss) {
		res.put(ss.getName(), ss);
	}

	@Override
	public String toString() {
		return "BasicJobStatCollector";
	}

}
