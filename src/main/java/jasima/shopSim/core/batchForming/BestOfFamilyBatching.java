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
package jasima.shopSim.core.batchForming;

import java.util.List;
import java.util.Map;

import jasima.shopSim.core.Batch;
import jasima.shopSim.core.Job;
import jasima.shopSim.core.Operation;
import jasima.shopSim.core.PriorityQueue;
import jasima.shopSim.core.WorkStation;

/**
 * <p>
 * This class creates a single batch per family according to the used sequencing
 * rule. Later a machine's sequencingRule (optionally batchSequencingRule) is
 * used to choose one of them to process.
 * </p>
 * <p>
 * If there are more than maxBatchSize jobs for a family, jobs are sequenced
 * using the sequencingRule first and the best maxBatchSize jobs of the family
 * are used to form the batch.
 * 
 * @author Torsten Hildebrandt, 2010-10-25
 */
public class BestOfFamilyBatching extends BatchForming {

	private static final long serialVersionUID = 4249713710542519941L;

	@Override
	public void formBatches() {
		final PriorityQueue<Job> q = getOwner().queue;

		orderedJobs = ensureCapacity(orderedJobs, q.size());
		q.getAllElementsInOrder(orderedJobs);
		int numJobs = q.size();

		// split jobs of each family
		Map<String, List<Job>> jobsByFamily = splitFamilies(orderedJobs, numJobs);

		// form two batches per family, one without future jobs
		for (List<Job> famJobs : jobsByFamily.values()) {
			formFamilyBatches(famJobs);
		}
	}

	private void formFamilyBatches(List<Job> famJobs) {
		assert famJobs.size() > 0;

		Job j = famJobs.get(0);
		Operation o = j.getCurrentOperation();
		assert WorkStation.BATCH_INCOMPATIBLE.equals(o.getBatchFamily()) ? o.getMaxBatchSize() == 1 : true;

		// make batches as full as possible
		Batch b = new Batch(getOwner().shop());
		Batch b2 = new Batch(getOwner().shop());
		for (int i = 0, n = famJobs.size(); i < n; i++) {
			Job job = famJobs.get(i);
			if (b.numJobsInBatch() < o.getMaxBatchSize())
				b.addToBatch(job);
			if (!job.isFuture())
				b2.addToBatch(job);
			if (b2.numJobsInBatch() == o.getMaxBatchSize())
				break;
		}
		possibleBatches.add(b);
		if (b.isFuture() && b2.numJobsInBatch() > 0)
			possibleBatches.add(b2);
	}

	@Override
	public String getName() {
		return "BOF";
	}

}
