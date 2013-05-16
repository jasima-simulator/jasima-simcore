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
package jasima.shopSim.core.batchForming;

import jasima.shopSim.core.Batch;
import jasima.shopSim.core.Job;
import jasima.shopSim.core.Operation;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.PriorityQueue;
import jasima.shopSim.core.WorkStation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class implements the rule that the batch that uses most of the available
 * capacity is selected. Ties are broken with the underlying sequencing rule
 * that selects the family with the highest priority job.
 * 
 * @author Christoph Pickardt, 2010-09-07
 * @author Torsten Hildebrandt, 2010-09-29
 * @version 
 *          "$Id$"
 */
public class MostCompleteBatch extends BatchForming {

	private double maxWaitRelative;

	private double maxWait;

	public MostCompleteBatch(double maxWaitRelative) {
		super();

		setMaxWaitRelative(maxWaitRelative);
	}

	public MostCompleteBatch() {
		this(0.0d);
	}

	public double maxProcTimeWaiting(PriorityQueue<?> q) {
		// find maximum time in which a jobs currently waiting can be finished
		// (incl. setups)
		double[][] setupMatrix = getOwner().getSetupMatrix();
		int currSetupState = getOwner().currMachine.setupState;

		double res = 0.0d;
		for (int i = 0, n = q.size(); i < n; i++) {
			PrioRuleTarget j = q.get(i);
			if (!j.isFuture()) {
				Operation o = j.getCurrentOperation();
				double timeToComplete = setupMatrix[currSetupState][o.setupState]
						+ o.procTime
						+ setupMatrix[o.setupState][currSetupState];
				if (timeToComplete > res) {
					res = timeToComplete;
				}
			}
		}
		return res;
	}

	@Override
	public void formBatches() {
		maxWait = getMaxWaitRelative() * maxProcTimeWaiting(getOwner().queue);
		Batch b = efficientBatching();
		if (b == null) {
			defaultBatching();
		} else {
			possibleBatches.add(b);
		}

		assert possibleBatches.size() == 1;
	}

	private Batch efficientBatching() {
		double maxRbs = 0.0d;
		List<Job> maxFam = null;
		boolean tie = false;
		boolean hasIncompatible = false;

		// try efficient way first if situation is clear
		for (List<Job> js : getOwner().getJobsByFamily().values()) {
			if (js.size() == 0)
				continue;

			Operation o = js.get(0).getCurrentOperation();
			if (WorkStation.BATCH_INCOMPATIBLE.equals(o.batchFamily)) {
				hasIncompatible = true;
				continue;
			}

			double arriveInTimeJobs = 0.0d;
			for (int i = 0; i < js.size(); i++) {
				if (js.get(i).getArriveTime() - js.get(i).getShop().simTime() <= maxWait)
					arriveInTimeJobs++;
			}
			double rbs = Math.min(1.0, (arriveInTimeJobs / o.maxBatchSize));
			if (rbs == maxRbs) {
				tie = true;
			} else if (rbs > maxRbs) {
				maxRbs = rbs;
				maxFam = js;
				tie = false;
			}
		}
		if (!tie && !hasIncompatible) {
			Job j = maxFam.get(0);
			int mbs = j.getCurrentOperation().maxBatchSize;
			int arriveInTimeJobs = 0;
			for (int i = 0; i < maxFam.size(); i++) {
				if (maxFam.get(i).getArriveTime()
						- maxFam.get(i).getShop().simTime() <= maxWait)
					arriveInTimeJobs++;
			}
			if (arriveInTimeJobs > mbs)
				return null;

			Batch b = new Batch(getOwner().shop());
			if (j.getArriveTime() - j.getShop().simTime() <= maxWait)
				b.addToBatch(j);
			for (int n = 1; n < maxFam.size(); n++) {
				if (maxFam.get(n).getArriveTime()
						- maxFam.get(n).getShop().simTime() <= maxWait)
					b.addToBatch(maxFam.get(n));
			}

			assert b.numJobsInBatch() == maxRbs * mbs;
			return b;
		} else {
			return null;
		}
	}

	private void defaultBatching() {
		final PriorityQueue<Job> q = getOwner().queue;
		// detailed approach below
		orderedJobs = ensureCapacity(orderedJobs, q.size());
		q.getAllElementsInOrder(orderedJobs);
		int numJobs = q.size();

		// split jobs of each family
		Map<String, List<Job>> jobsByFamily = splitFamilies(orderedJobs,
				numJobs);

		// form batches as large as possible
		formBatches(jobsByFamily);

		// first tie breaker
		// if (possibleBatches.size() > 1)
		// handleTiesByFamilySize(jobsByFamily);

		// second tie breaker
		if (possibleBatches.size() > 1)
			handleTiesByBasePrio(numJobs);
	}

	private void formBatches(Map<String, List<Job>> jobsByFamily) {
		double maxRBS = 0.0d;

		for (List<Job> famJobs : jobsByFamily.values()) {
			Operation o = famJobs.get(0).getCurrentOperation();

			if (famJobs.size() < maxRBS * o.maxBatchSize)
				continue;

			if (WorkStation.BATCH_INCOMPATIBLE.equals(o.batchFamily)) {
				for (Job j : famJobs) {
					if (j.getArriveTime() - j.getShop().simTime() <= maxWait) {
						Batch b = new Batch(getOwner().shop());
						b.addToBatch(j);
						possibleBatches.add(b);
					}
				}
				maxRBS = 1.0;
			} else {
				Batch b = new Batch(getOwner().shop());
				// make batch as full as possible
				int i = 0;
				while (i < famJobs.size()
						&& b.numJobsInBatch() < o.maxBatchSize) {
					if (famJobs.get(i).getArriveTime()
							- famJobs.get(i).getShop().simTime() <= maxWait)
						b.addToBatch(famJobs.get(i));
					i++;
				}

				if ((maxRBS * o.maxBatchSize) <= b.numJobsInBatch()
						&& 0 < b.numJobsInBatch()) {
					if (maxRBS * o.maxBatchSize < b.numJobsInBatch()) {
						possibleBatches.clear();
						maxRBS = ((double) b.numJobsInBatch() / o.maxBatchSize);
					}
					possibleBatches.add(b);
				}
			}
		}
	}

	private void handleTiesByFamilySize(Map<String, List<Job>> map) {
		ArrayList<Batch> bs = possibleBatches
				.getAllElements(new ArrayList<Batch>(possibleBatches.size()));
		possibleBatches.clear();

		int bestFamSize = 0;

		for (int i = 0, n = bs.size(); i < n; i++) {
			Batch b = bs.get(i);
			String bf = b.job(0).getCurrentOperation().batchFamily;

			int famSize = map.get(bf).size();

			if (famSize > bestFamSize) {
				bestFamSize = famSize;
				possibleBatches.clear();
				possibleBatches.add(b);
			} else if (famSize == bestFamSize) {
				possibleBatches.add(b);
			}
		}
	}

	private void handleTiesByBasePrio(int numJobs) {
		Batch best = possibleBatches.get(0);
		int bestIdx = indexOf(best.job(0), orderedJobs, numJobs);

		for (int i = 1, n = possibleBatches.size(); i < n; i++) {
			Batch b = possibleBatches.get(i);
			int idx = indexOf(b.job(0), orderedJobs, numJobs);
			if (idx < bestIdx) {
				bestIdx = idx;
				best = b;
			}
		}

		possibleBatches.clear();
		possibleBatches.add(best);
	}

	public double getMaxWaitRelative() {
		return maxWaitRelative;
	}

	public void setMaxWaitRelative(double maxWaitRelative) {
		if (maxWaitRelative < 0.0 || maxWaitRelative > 1.0)
			throw new IllegalArgumentException("maxWaitRelative "
					+ maxWaitRelative + " has to be within [0,1]!");

		this.maxWaitRelative = maxWaitRelative;
	}

	@Override
	public String getName() {
		return "MCB(" + getMaxWaitRelative() + ")";
	}

}
