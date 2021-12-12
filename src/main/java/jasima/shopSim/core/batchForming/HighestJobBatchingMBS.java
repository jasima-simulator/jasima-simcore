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
package jasima.shopSim.core.batchForming;

import java.util.List;

import jasima.shopSim.core.Batch;
import jasima.shopSim.core.Job;
import jasima.shopSim.core.Operation;
import jasima.shopSim.core.PriorityQueue;
import jasima.shopSim.core.WorkStation;

/**
 * This class creates a single batch for the family with the highest priority
 * job, where jobs for the batch are selected according to the used sequencing
 * rule.
 * 
 * @author Christoph Pickardt, 2011-01-14
 */
public class HighestJobBatchingMBS extends BatchForming {

	private static final long serialVersionUID = -1651682966642185315L;

	private double mbsRel;

	public HighestJobBatchingMBS() {
		this(0.0);
	}

	public HighestJobBatchingMBS(double mbsRel) {
		super();

		if (mbsRel < 0.0 || mbsRel > 1.0)
			throw new IllegalArgumentException("min batch size has to be in [0.0,1.0] " + mbsRel);
		this.mbsRel = mbsRel;
	}

	@Override
	public void formBatches() {
		// try an efficient way first. If this doesn't work, use other variant
		Batch b = efficientBatching();
		if (b == null) {
			defaultBatchForming();
		} else {
			possibleBatches.add(b);
		}
	}

	private Batch efficientBatching() {
		// try an efficient way first. If this doesn't work, use other variant
		final PriorityQueue<Job> q = getOwner().queue;

		Job largest = q.peekLargest();
		Operation o = largest.getCurrentOperation();
		String bf = o.getBatchFamily();

		Batch b = new Batch(largest.getShop());

		if (WorkStation.BATCH_INCOMPATIBLE.equals(o.getBatchFamily()) || o.getMaxBatchSize() == 1
				|| largest.isFuture()) {
			b.addToBatch(largest);
			return b;
		}

		List<Job> js = getOwner().getJobsByFamily().get(bf);

		int minSize = (int) Math.ceil(getMbsRel() * o.getMaxBatchSize());

		for (int i = 0, n = js.size(); i < n; i++) {
			Job j = js.get(i);
			if (!j.isFuture())
				b.addToBatch(j);
		}
		assert b.numJobsInBatch() >= 1;

		if (b.numJobsInBatch() >= minSize && b.numJobsInBatch() <= o.getMaxBatchSize())
			return b;
		else
			return null;

	}

	private void defaultBatchForming() {
		final PriorityQueue<Job> q = getOwner().queue;

		orderedJobs = ensureCapacity(orderedJobs, q.size());
		q.getAllElementsInOrder(orderedJobs);
		int numJobs = q.size();

		for (int i = 0; i < numJobs; i++) {
			Job j = orderedJobs[i];
			if (j == null)
				continue;
			orderedJobs[i] = null;

			Operation o = j.getCurrentOperation();
			String bf = o.getBatchFamily();

			if (WorkStation.BATCH_INCOMPATIBLE.equals(o.getBatchFamily()) || o.getMaxBatchSize() == 1 || j.isFuture()) {
				Batch batch = new Batch(j.getShop());
				batch.addToBatch(j);
				possibleBatches.add(batch);
				return;
			}

			List<Job> js = getOwner().getJobsByFamily().get(bf);
			int minSize = (int) Math.ceil(getMbsRel() * o.getMaxBatchSize());
			if (js.size() < minSize)
				continue;

			// fill batch with compatible jobs that already have arrived
			Batch batch = new Batch(j.getShop());

			batch.addToBatch(j);
			for (int n = i + 1; n < numJobs; n++) {
				Job j2 = orderedJobs[n];
				if (j2 == null)
					continue;

				Operation o2 = j2.getCurrentOperation();
				if (!bf.equals(o2.getBatchFamily()))
					continue;

				assert o2.getMaxBatchSize() == o.getMaxBatchSize();
				orderedJobs[n] = null;

				if (!j2.isFuture())
					batch.addToBatch(j2);

				if (batch.numJobsInBatch() == o2.getMaxBatchSize())
					break; // for n

			}

			if ((((double) batch.numJobsInBatch()) / o.getMaxBatchSize()) >= getMbsRel()) {
				possibleBatches.add(batch);
				return;
			}

		}

		// no batch large enough to start, just return
	}

	@Override
	public String getName() {
		return "MBS(" + getMbsRel() + ")";
	}

	public double getMbsRel() {
		return mbsRel;
	}

	public void setMbsRel(double mbsRel) {
		this.mbsRel = mbsRel;
	}

}
