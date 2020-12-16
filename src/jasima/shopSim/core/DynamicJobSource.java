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
package jasima.shopSim.core;

import jasima.core.random.RandomFactory;
import jasima.core.random.continuous.DblConst;
import jasima.core.random.continuous.DblStream;
import jasima.core.random.discrete.IntStream;
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

	private DblStream arrivalProcess = new ArrivalsStationary();
	private DblStream dueDateFactors = new DblConst(1.0);
	private DblStream jobWeights = new DblConst(1.0);
	private IntStream machIdx = null;
	private DblStream procTimes = null;
	private IntStream numOps = null;
	private Route route = null;

	@Override
	public void init() {
		String prefix = "source" + index + ".";

		super.init();

		RandomFactory fact = getSim().getRndStreamFactory();
		init(getArrivalProcess(), prefix + "arrivalStream", fact);
		init(getDueDateFactors(), prefix + "dueDateStream", fact);
		init(getJobWeights(), prefix + "weightStream", fact);
		init(getMachIdx(), prefix + "machIdxStream", fact);
		init(getProcTimes(), prefix + "procTimesStream", fact);
		init(getNumOps(), prefix + "numOpsStream", fact);
	}

	static protected void init(DblStream dblStream, String streamName, RandomFactory fact) {
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

	public void setDueDateFactors(DblStream dueDateFactors) {
		this.dueDateFactors = dueDateFactors;
	}

	public DblStream getDueDateFactors() {
		return dueDateFactors;
	}

	public void setJobWeights(DblStream jobWeights) {
		this.jobWeights = jobWeights;
	}

	public DblStream getJobWeights() {
		return jobWeights;
	}

	public Route getRoute() {
		return route;
	}

	public void setRoute(Route route) {
		this.route = route;
	}

	public void setMachIdx(IntStream machIdx) {
		this.machIdx = machIdx;
	}

	public IntStream getMachIdx() {
		return machIdx;
	}

	public void setProcTimes(DblStream procTimes) {
		this.procTimes = procTimes;
	}

	public DblStream getProcTimes() {
		return procTimes;
	}

	public void setNumOps(IntStream numOps) {
		this.numOps = numOps;
	}

	public IntStream getNumOps() {
		return numOps;
	}

	public DblStream getArrivalProcess() {
		return arrivalProcess;
	}

	public void setArrivalProcess(DblStream arrivalProcess) {
		this.arrivalProcess = arrivalProcess;
	}

}
