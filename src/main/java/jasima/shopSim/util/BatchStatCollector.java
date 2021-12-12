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

import java.util.Map;

import jasima.core.simulation.SimComponent;
import jasima.core.statistics.SummaryStat;
import jasima.core.util.Util;
import jasima.shopSim.core.Job;
import jasima.shopSim.core.Shop;
import jasima.shopSim.core.ShopListenerBase;

/**
 * Collects batch statistics (flowtime and tardiness). A batch consists of a
 * certain number of jobs started in succession.
 * 
 * @author Torsten Hildebrandt, 2012-08-21
 */
public class BatchStatCollector extends ShopListenerBase {

	private int numBatches = 1;
	private int batchSize = 2000;

	private SummaryStat[] batchFlowtimes;
	private SummaryStat[] batchTardiness;

	@Override
	public void produceResults(SimComponent c, Map<String, Object> res) {
		for (int i = 0; i < numBatches; i++) {
			String prefix = "b" + i;
			Util.putMeanMaxVar(batchFlowtimes[i], prefix + ".flow", res);
			Util.putMeanMaxVar(batchTardiness[i], prefix + ".tard", res);
			res.put(prefix + ".numFinished", batchFlowtimes[i].numObs());
		}
	}

	@Override
	public void init(SimComponent sim) {
		batchFlowtimes = new SummaryStat[numBatches];
		batchTardiness = new SummaryStat[numBatches];
		for (int i = 0; i < numBatches; i++) {
			batchFlowtimes[i] = new SummaryStat();
			batchTardiness[i] = new SummaryStat();
		}
	}

	@Override
	public void jobReleased(Shop shop, Job j) {
	}

	@Override
	public void jobFinished(Shop shop, Job j) {
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
