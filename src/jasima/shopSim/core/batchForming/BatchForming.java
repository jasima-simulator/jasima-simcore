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
package jasima.shopSim.core.batchForming;

import jasima.shopSim.core.Batch;
import jasima.shopSim.core.Job;
import jasima.shopSim.core.Operation;
import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.PriorityQueue;
import jasima.shopSim.core.WorkStation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for batch forming implementations.
 * <p>
 * When implementing formBatches() make sure not to remove any jobs from a
 * machine's queue as this is later done in the Machine class after the best
 * batch was selected and it is clear this batch can be started immediately.
 * 
 * @author Christoph Pickardt, 2010-05-27
 * @author Torsten Hildebrandt, 2010-03-18
 */
public abstract class BatchForming implements Cloneable, Serializable {

	public static int indexOf(Job j, Job[] js, int numElems) {
		for (int i = 0, n = numElems; i < n; i++) {
			if (j == js[i])
				return i;
		}

		// we should always find j in js
		throw new AssertionError();
	}

	public static Map<String, List<Job>> splitFamilies(Job[] jobs, int numElems) {
		Map<String, List<Job>> jobsPerFamily = new HashMap<String, List<Job>>();
		for (int i = 0; i < numElems; i++) {
			Job j = jobs[i];
			String bf = j.getCurrentOperation().batchFamily;

			List<Job> jobsInFamily = jobsPerFamily.get(bf);
			if (jobsInFamily == null) {
				jobsInFamily = new ArrayList<Job>();
				jobsPerFamily.put(bf, jobsInFamily);
			}

			jobsInFamily.add(j);
		}
		return jobsPerFamily;
	}

	public static Job[] ensureCapacity(Job[] jobs, int numElems) {
		if (jobs.length < numElems) {
			int size = Math.max(10, (numElems * 5) / 4); // +25%
			return new Job[size];
		} else
			return jobs;
	}

	private WorkStation owner;
	protected PriorityQueue<Batch> possibleBatches;
	protected Job[] orderedJobs = new Job[] {};

	public BatchForming() {
		super();
	}

	public abstract void formBatches();

	public PrioRuleTarget nextBatch() {
		if (possibleBatches == null)
			possibleBatches = new PriorityQueue<Batch>(getOwner());
		else
			possibleBatches.clear();

		possibleBatches.setSequencingRule(batchingRule());

		formBatches();

		if (possibleBatches.size() == 0)
			return null;

		for (int i = 0; i < possibleBatches.size(); i++) {
			final Batch b = possibleBatches.get(i);
			initBatchData(b);
		}

		if (possibleBatches.size() == 1)
			return possibleBatches.get(0);
		else {
			return possibleBatches.peekLargest();
		}
	}

	private PR batchingRule() {
		PR pr = getOwner().getBatchSequencingRule();
		return pr == null ? getOwner().queue.getSequencingRule() : pr;
	}

	private void initBatchData(Batch b) {
		assert checkBatchData(b);

		Job job = b.job(0);
		Operation opJ = job.getCurrentOperation();

		Operation op = new Operation();
		op.machine = opJ.machine;
		op.batchFamily = opJ.batchFamily;
		op.setupState = opJ.setupState;
		op.procTime = opJ.procTime;
		op.maxBatchSize = opJ.maxBatchSize;

		b.op = op;
	}

	private boolean checkBatchData(Batch b) {
		Operation opJ = b.job(0).getCurrentOperation();
		for (int i = 1; i < b.numJobsInBatch(); i++) {
			Operation op = b.job(i).getCurrentOperation();
			assert opJ.batchFamily.equals(op.batchFamily);
			assert opJ.maxBatchSize == op.maxBatchSize;
			assert Math.abs(opJ.procTime - op.procTime) < 1e-6;
			assert opJ.setupState == op.setupState;
		}
		return true;
	}

	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public BatchForming clone() throws CloneNotSupportedException {
		BatchForming bf = (BatchForming) super.clone();
		bf.possibleBatches = null;
		bf.orderedJobs = new Job[] {};
		return bf;
	}

	public BatchForming silentClone() {
		try {
			return clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public WorkStation getOwner() {
		return owner;
	}

	public void setOwner(WorkStation o) {
		owner = o;
	}

}
