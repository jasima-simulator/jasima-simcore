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
package jasima.shopSim.core.batchForming;

import jasima.shopSim.core.Batch;
import jasima.shopSim.core.Job;
import jasima.shopSim.core.Operation;
import jasima.shopSim.core.PriorityQueue;
import jasima.shopSim.core.WorkStation;

import java.util.List;
import java.util.Map;

/**
 * This class creates a single batch per family according to the used sequencing
 * rule. Later a machine's sequencingRule (optionally batchSequencingRule) is
 * used to choose one of them to process.
 * <p />
 * If there are more than maxBatchSize jobs for a family, jobs are sequenced
 * using the sequencingRule first and the best maxBatchSize jobs of the family
 * are used to form the batch.
 * 
 * @author Torsten Hildebrandt, 2010-10-25
 * @version "$Id$"
 */
public class BestOfFamilyBatching extends BatchForming {

	@Override
	public void formBatches() {
		final PriorityQueue<Job> q = getOwner().queue;

		orderedJobs = ensureCapacity(orderedJobs, q.size());
		q.getAllElementsInOrder(orderedJobs);
		int numJobs = q.size();

		// split jobs of each family
		Map<String, List<Job>> jobsByFamily = splitFamilies(orderedJobs,
				numJobs);

		// form two batches per family, one without future jobs
		for (List<Job> famJobs : jobsByFamily.values()) {
			formFamilyBatches(famJobs);
		}
	}

	private void formFamilyBatches(List<Job> famJobs) {
		assert famJobs.size() > 0;

		Job j = famJobs.get(0);
		Operation o = j.getCurrentOperation();
		assert WorkStation.BATCH_INCOMPATIBLE.equals(o.batchFamily) ? o.maxBatchSize == 1
				: true;

		// make batches as full as possible
		Batch b = new Batch(getOwner().shop());
		Batch b2 = new Batch(getOwner().shop());
		for (int i = 0, n = famJobs.size(); i < n; i++) {
			Job job = famJobs.get(i);
			if (b.numJobsInBatch() < o.maxBatchSize)
				b.addToBatch(job);
			if (!job.isFuture())
				b2.addToBatch(job);
			if (b2.numJobsInBatch() == o.maxBatchSize)
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
