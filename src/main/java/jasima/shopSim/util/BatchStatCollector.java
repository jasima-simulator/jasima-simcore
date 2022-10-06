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
