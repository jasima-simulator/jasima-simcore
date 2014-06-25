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
package jasima.shopSim.core;

import jasima.core.random.RandomFactory;
import jasima.core.random.continuous.DblConst;
import jasima.core.random.continuous.DblStream;
import jasima.core.random.discrete.IntStream;
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
 * @author Torsten Hildebrandt, 2010-03-12
 * @version 
 *          "$Id$"
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

		RandomFactory fact = getShop().getRndStreamFactory();
		init(getArrivalProcess(), prefix + "arrivalStream", fact);
		init(getDueDateFactors(), prefix + "dueDateStream", fact);
		init(getJobWeights(), prefix + "weightStream", fact);
		init(getMachIdx(), prefix + "machIdxStream", fact);
		init(getProcTimes(), prefix + "procTimesStream", fact);
		init(getNumOps(), prefix + "numOpsStream", fact);
	}

	static protected void init(DblStream dblStream, String streamName,
			RandomFactory fact) {
		if (dblStream == null)
			return;
		fact.initNumberStream(dblStream, streamName);
		dblStream.init();
	}

	@Override
	public Job createNextJob() {
		Job j = newJobInstance();
		j.setJobType(index);
		j.setOps(getRouteForJob());
		j.setRoute(route);

		j.setRelDate(getArrivalProcess().nextDbl());

		j.setDueDate(j.getRelDate() + getDueDateFactors().nextDbl()
				* j.procSum());
		j.setWeight(getJobWeights().nextDbl());

		return j;
	}

	protected Operation[] getRouteForJob() {
		return route != null ? route.ops() : createRoute();
	}

	protected Operation[] createRoute() {
		// machine order
		final int n = getNumOps() != null ? getNumOps().nextInt()
				: getShop().machines.length;
		assert n > 0;

		Operation[] ops = new Operation[n];

		// initially all false
		boolean[] machineChosen = new boolean[getShop().machines.length];

		for (int i = 0; i < n; i++) {
			// TODO: change: not very elegant but works for now
			int mi = -1;
			do {
				mi = getMachIdx().nextInt();
			} while (machineChosen[mi]);

			WorkStation m = getShop().machines[mi];
			machineChosen[mi] = true;

			Operation o = ops[i] = new Operation();
			o.machine = m;
			o.procTime = getProcTimes().nextDbl();
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
