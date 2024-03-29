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
package jasima.shopSim.core;

import jasima.core.random.RandomFactory;
import jasima.core.random.continuous.DblConst;
import jasima.core.random.continuous.DblSequence;
import jasima.core.random.discrete.IntSequence;
import jasima.core.simulation.SimComponentContainer;
import jasima.core.simulation.arrivalprocess.ArrivalsStationary;

/**
 * This class can be used to create a stream of jobs characterized by various
 * random distributions. To use it, at least "interArrivalTimes" and "route"
 * have to be set.
 * <p>
 * Optionally jobs' due dates and weights can be specified using
 * "dueDateFactors" (default: constant 1.0) and "jobWeights" (default: constant
 * 1.0).
 * <p>
 * Instead of creating jobs following a fixed route, routes for a job shop can
 * also be created using this class. To do so, instead of specifying "route" the
 * two properties machIdx (machine order), procTimes (processing times) and
 * optionally numOps (number of operations, default: number of machines in shop)
 * have to be set. A route created this way will create a random route with no
 * machine being revisited.
 * 
 * @author Torsten Hildebrandt
 */
public class DynamicJobSource extends JobSource {

	private DblSequence arrivalProcess = new ArrivalsStationary();
	private DblSequence dueDateFactors = new DblConst(1.0);
	private DblSequence jobWeights = new DblConst(1.0);
	private IntSequence machIdx = null;
	private DblSequence procTimes = null;
	private IntSequence numOps = null;
	private Route route = null;

	@Override
	public void init() {
		String prefix = streamNamePrefix();

		super.init();

		RandomFactory fact = getSim().getRndStreamFactory();
		init(getArrivalProcess(), prefix + "arrivalStream", fact);
		init(getDueDateFactors(), prefix + "dueDateStream", fact);
		init(getJobWeights(), prefix + "weightStream", fact);
		init(getMachIdx(), prefix + "machIdxStream", fact);
		init(getProcTimes(), prefix + "procTimesStream", fact);
		init(getNumOps(), prefix + "numOpsStream", fact);
	}

	protected String streamNamePrefix() {
		return "source" + index + ".";
	}

	static protected void init(DblSequence dblStream, String streamName, RandomFactory fact) {
		if (dblStream == null)
			return;
		fact.initRndGen(dblStream, streamName);
	}

	@Override
	public Job createNextJob() {
		Job j = newJobInstance();
		j.setJobType(index);
		j.setOps(getRouteForJob());
		j.setRoute(route);

		j.setRelDate(getArrivalProcess().nextDbl());

		j.setDueDate(j.getRelDate() + getDueDateFactors().nextDbl() * j.procSum());
		j.setWeight(getJobWeights().nextDbl());

		return j;
	}

	protected Operation[] getRouteForJob() {
		return route != null ? route.ops() : createRoute();
	}

	protected Operation[] createRoute() {
		// machine order
		SimComponentContainer machines = getShop().machines();

		final int n = getNumOps() != null ? getNumOps().nextInt() : machines.numChildren();
		assert n > 0;

		Operation[] ops = new Operation[n];

		// initially all false
		boolean[] machineChosen = new boolean[machines.numChildren()];

		for (int i = 0; i < n; i++) {
			// TODO: change: not very elegant but works for now
			int mi = -1;
			do {
				mi = getMachIdx().nextInt();
			} while (machineChosen[mi]);

			WorkStation m = (WorkStation) machines.getChild(mi);
			machineChosen[mi] = true;

			Operation o = ops[i] = new Operation();
			o.setMachine(m);
			o.setProcTime(getProcTimes().nextDbl());
		}

		return ops;
	}

	//
	//
	// boring getters and setters for parameters below
	//
	//

	public void setDueDateFactors(DblSequence dueDateFactors) {
		this.dueDateFactors = dueDateFactors;
	}

	public DblSequence getDueDateFactors() {
		return dueDateFactors;
	}

	public void setJobWeights(DblSequence jobWeights) {
		this.jobWeights = jobWeights;
	}

	public DblSequence getJobWeights() {
		return jobWeights;
	}

	public Route getRoute() {
		return route;
	}

	public void setRoute(Route route) {
		this.route = route;
	}

	public void setMachIdx(IntSequence machIdx) {
		this.machIdx = machIdx;
	}

	public IntSequence getMachIdx() {
		return machIdx;
	}

	public void setProcTimes(DblSequence procTimes) {
		this.procTimes = procTimes;
	}

	public DblSequence getProcTimes() {
		return procTimes;
	}

	public void setNumOps(IntSequence numOps) {
		this.numOps = numOps;
	}

	public IntSequence getNumOps() {
		return numOps;
	}

	public DblSequence getArrivalProcess() {
		return arrivalProcess;
	}

	public void setArrivalProcess(DblSequence arrivalProcess) {
		this.arrivalProcess = arrivalProcess;
	}

}
