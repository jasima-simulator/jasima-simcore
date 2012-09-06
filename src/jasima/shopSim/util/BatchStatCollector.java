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
package jasima.shopSim.util;

import jasima.core.simulation.Simulation;
import jasima.core.statistics.SummaryStat;
import jasima.core.util.Util;
import jasima.shopSim.core.Job;
import jasima.shopSim.core.JobShop;

import java.util.Map;

/**
 * Collects batch statistics (flowtime and tardiness). A batch consists of a
 * certain number of jobs stated in succession.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>, 2012-08-21
 */
public class BatchStatCollector extends JobShopListenerBase {

	private static final long serialVersionUID = 7842780799230820976L;

	private int numBatches = 1;
	private int batchSize = 2000;

	private SummaryStat[] batchFlowtimes;
	private SummaryStat[] batchTardiness;

	@Override
	protected void produceResults(Simulation sim, Map<String, Object> res) {
		for (int i = 0; i < numBatches; i++) {
			String prefix = "b" + i;
			Util.putMeanMaxVar(batchFlowtimes[i], prefix + ".flow", res);
			Util.putMeanMaxVar(batchTardiness[i], prefix + ".tard", res);
			res.put(prefix + ".numFinished", batchFlowtimes[i].numObs());
		}
	}

	@Override
	protected void init(Simulation sim) {
		batchFlowtimes = new SummaryStat[numBatches];
		batchTardiness = new SummaryStat[numBatches];
		for (int i = 0; i < numBatches; i++) {
			batchFlowtimes[i] = new SummaryStat();
			batchTardiness[i] = new SummaryStat();
		}
	}

	@Override
	protected void jobReleased(JobShop shop, Job j) {
	}

	@Override
	protected void jobFinished(JobShop shop, Job j) {
		if (j.getJobNum() < getIgnoreFirst())
			return;

		int batchNum = (j.getJobNum() - getIgnoreFirst()) / getBatchSize();
		if (batchNum < getNumBatches()) {
			double flowtime = shop.simTime() - j.getRelDate();
			double tard = Math.max(shop.simTime() - j.getDueDate(), 0);

			batchFlowtimes[batchNum].value(flowtime);
			batchTardiness[batchNum].value(tard);
		}
	}

	public int getNumBatches() {
		return numBatches;
	}

	public void setNumBatches(int numBatches) {
		this.numBatches = numBatches;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

}
