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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jasima.shopSim.core.Batch;
import jasima.shopSim.core.Job;
import jasima.shopSim.core.Operation;
import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.PriorityQueue;
import jasima.shopSim.core.WorkStation;

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

	private static final long serialVersionUID = 6670837045346111285L;

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
			String bf = j.getCurrentOperation().getBatchFamily();

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
		op.setMachine(opJ.getMachine());
		op.setBatchFamily(opJ.getBatchFamily());
		op.setSetupState(opJ.getSetupState());
		op.setProcTime(opJ.getProcTime());
		op.setMaxBatchSize(opJ.getMaxBatchSize());

		b.op = op;
	}

	private boolean checkBatchData(Batch b) {
		Operation opJ = b.job(0).getCurrentOperation();
		for (int i = 1; i < b.numJobsInBatch(); i++) {
			Operation op = b.job(i).getCurrentOperation();
			assert opJ.getBatchFamily().equals(op.getBatchFamily());
			assert opJ.getMaxBatchSize() == op.getMaxBatchSize();
			assert Math.abs(opJ.getProcTime() - op.getProcTime()) < 1e-6;
			assert opJ.getSetupState() == op.getSetupState();
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
	public BatchForming clone() {
		try {
			BatchForming bf = (BatchForming) super.clone();
			bf.possibleBatches = null;
			bf.orderedJobs = new Job[] {};
			return bf;
		} catch (CloneNotSupportedException cantHappen) {
			throw new AssertionError(cantHappen);
		}
	}

	public WorkStation getOwner() {
		return owner;
	}

	public void setOwner(WorkStation o) {
		owner = o;
	}

}
