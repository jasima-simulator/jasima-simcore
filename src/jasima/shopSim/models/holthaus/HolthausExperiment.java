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
package jasima.shopSim.models.holthaus;

import jasima.core.random.continuous.DblConst;
import jasima.core.random.continuous.DblDistribution;
import jasima.core.random.continuous.DblStream;
import jasima.core.random.discrete.IntUniformRange;
import jasima.core.simulation.arrivalprocess.ArrivalsStationary;
import jasima.core.util.Pair;
import jasima.core.util.Util;
import jasima.shopSim.core.DynamicJobSource;
import jasima.shopSim.core.Job;
import jasima.shopSim.core.JobShop;
import jasima.shopSim.core.JobShopExperiment;
import jasima.shopSim.core.JobSource;
import jasima.shopSim.core.Operation;
import jasima.shopSim.core.WorkStation;
import jasima.shopSim.util.JobShopListenerBase;

import org.apache.commons.math3.distribution.ExponentialDistribution;

/**
 * Simulates dynamic job shops and flow shops, based on some parameters. See
 * Holthaus and Rajendran (1999) for details.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version 
 *          "$Id$"
 */
public class HolthausExperiment extends JobShopExperiment {

	private static final long serialVersionUID = -7289579178397939550L;

	public enum Scenario {
		JOB_SHOP, FLOW_SHOP
	};

	private static final double DEF_UTIL = 0.85d;
	private static final double DEF_DUE_DATE = 4.0d;
	private static final int DEF_MACHINES = 10;
	private static final int DEF_OPS_MIN = -1;
	private static final int DEF_OPS_MAX = -1;
	private static final Integer DEF_PROC_MIN = 1;
	private static final Integer DEF_PROC_MAX = 49;

	private static final double MINUTES_PER_DAY = 24 * 60;

	private double utilLevel = DEF_UTIL;
	private double dueDateFactor = DEF_DUE_DATE;
	private int numMachines = DEF_MACHINES;
	private Scenario scenario = Scenario.JOB_SHOP;
	private DblStream weights = null;

	private Pair<Integer, Integer> numOps = new Pair<Integer, Integer>(
			DEF_OPS_MIN, DEF_OPS_MAX);
	private Pair<Integer, Integer> opProcTime = new Pair<Integer, Integer>(
			DEF_PROC_MIN, DEF_PROC_MAX);

	private int stopAfterNumJobs = 2500;

	protected JobSource src;

	@Override
	public void init() {
		super.init();

		@SuppressWarnings("serial")
		JobShopListenerBase stopSrc = new JobShopListenerBase() {
			int maxJob = getStopAfterNumJobs();
			int numJobs = maxJob;

			@Override
			protected void jobFinished(JobShop shop, Job j) {
				if (j.getJobNum() < maxJob) {
					if (--numJobs == 0) {
						src.stopArrivals = true;
					}
				}
			}
		};
		shop.installSimulationListener(stopSrc, false);
	}

	@Override
	protected void createShop() {
		super.createShop();

		createMachines();

		src = createJobSource();
		shop.addJobSource(src);

		shop.setMaxJobsFinished(10 * getStopAfterNumJobs());
	}

	private void createMachines() {
		for (int n = 0; n < getNumMachines(); n++) {
			WorkStation m = new WorkStation(1);
			shop.addMachine(m);
		}
	}

	protected JobSource createJobSource() {
		DynamicJobSource src = new DynamicJobSource() {

			@Override
			protected Operation[] createRoute() {
				final int n = getNumOps().nextInt();
				assert n > 0;

				Operation[] ops = Util.initializedArray(n, Operation.class);

				// initially all false
				boolean[] machineChosen = new boolean[getNumMachines()];

				for (int i = 0; i < n; i++) {
					int mi = -1;
					do {
						mi = getMachIdx().nextInt();
					} while (machineChosen[mi]);

					WorkStation m = shop.machines[mi];
					machineChosen[mi] = true;

					if (getScenario() == Scenario.JOB_SHOP) {
						ops[i].machine = m;
					}
				}

				if (getScenario() == Scenario.FLOW_SHOP) {
					int k = 0;
					for (int i = 0; i < shop.machines.length; i++) {
						if (machineChosen[i])
							ops[k++].machine = shop.machines[i];
					}
				}

				// procTimes
				for (Operation o : ops) {
					o.procTime = getProcTimes().nextDbl();
				}

				return ops;
			}
		};

		double iaMean = calcIaMean();

		ArrivalsStationary arrivals = new ArrivalsStationary();
		arrivals.setInterArrivalTimes(new DblDistribution(
				new ExponentialDistribution(iaMean)));
		arrivals.setName("arrivalStream");
		src.setArrivalProcess(arrivals);

		IntUniformRange numOps = new IntUniformRange("numOpsStream",
				getNumOpsMin() > 0 ? getNumOpsMin() : getNumMachines(),
				getNumOpsMax() > 0 ? getNumOpsMax() : getNumMachines());
		src.setNumOps(numOps);

		src.setProcTimes(new IntUniformRange("procTimesStream",
				getOpProcTimeMin(), getOpProcTimeMax()));

		src.setMachIdx(new IntUniformRange("machIdxStream", 0,
				getNumMachines() - 1));

		src.setDueDateFactors(new DblConst(getDueDateFactor()));

		if (getWeights() != null)
			try {
				src.setJobWeights(getWeights().clone());
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException(e);
			}

		return src;
	}

	@Override
	protected void done() {
		// was simulation stopped early?
		aborted = (shop.jobsStarted - shop.jobsFinished) > 0 ? 1 : 0;

		super.done();
	}

	private double calcIaMean() {
		int opsMin = getNumOpsMin() > 0 ? getNumOpsMin() : getNumMachines();
		int opsMax = getNumOpsMax() > 0 ? getNumOpsMax() : getNumMachines();

		double meanOps = 0.5d * (opsMax + opsMin);
		double meanOpProc = 0.5d * (getOpProcTimeMax() + getOpProcTimeMin());

		double jobsPerDay = getUtilLevel() * getNumMachines() * MINUTES_PER_DAY
				/ (meanOps * meanOpProc);
		return (1.0d * MINUTES_PER_DAY / jobsPerDay);
	}

	public double getUtilLevel() {
		return utilLevel;
	}

	public void setUtilLevel(double utilLevel) {
		if (utilLevel < 0.0d || utilLevel > 1.0d)
			throw new IllegalArgumentException("" + utilLevel);

		this.utilLevel = utilLevel;
	}

	public double getDueDateFactor() {
		return dueDateFactor;
	}

	public void setDueDateFactor(double dueDateFactor) {
		this.dueDateFactor = dueDateFactor;
	}

	public int getNumMachines() {
		return numMachines;
	}

	public void setNumMachines(int numMachines) {
		if (numMachines < 1)
			throw new IllegalArgumentException("" + numMachines);

		this.numMachines = numMachines;
	}

	public int getNumOpsMin() {
		return numOps.a;
	}

	public int getNumOpsMax() {
		return numOps.b;
	}

	public void setNumOps(int min, int max) {
		if (min < 0 || (max < min))
			throw new IllegalArgumentException("[" + min + ";" + max + "]");
		numOps = new Pair<Integer, Integer>(min, max);
	}

	public int getOpProcTimeMin() {
		return opProcTime.a;
	}

	/** Sets the minimum processing time of an operation. */
	public void setOpProcTimeMin(int min) {
		opProcTime = new Pair<Integer, Integer>(min, opProcTime.b);
	}

	public int getOpProcTimeMax() {
		return opProcTime.b;
	}

	/** Sets the maximum processing time of an operation. */
	public void setOpProcTimeMax(int max) {
		opProcTime = new Pair<Integer, Integer>(opProcTime.a, max);
	}

	public void setOpProcTime(int min, int max) {
		if (min < 0 || (max < min))
			throw new IllegalArgumentException("[" + min + ";" + max + "]");
		opProcTime = new Pair<Integer, Integer>(min, max);
	}

	public void setScenario(Scenario scenario) {
		this.scenario = scenario;
	}

	public Scenario getScenario() {
		return scenario;
	}

	public int getStopAfterNumJobs() {
		return stopAfterNumJobs;
	}

	/**
	 * The job source is stopped after a certain number of jobs were completed.
	 * Jobs are counted in the order they entered the system. If, e.g.,
	 * {@code stopAfterNumJobs} is 2500 the job source is stopped after all of
	 * the first 2500 jobs were completed (note: this is is not necessarily the
	 * same as the first 2500 jobs completed).
	 * 
	 * @param stopAfterNumJobs
	 *            The number of jobs after which to stop, default: 2500.
	 */
	public void setStopAfterNumJobs(int stopAfterNumJobs) {
		this.stopAfterNumJobs = stopAfterNumJobs;
	}

	public DblStream getWeights() {
		return weights;
	}

	/**
	 * Sets the weights to be used for each job. The default setting is to
	 * assign a weight of 1 for each job when this attribute is {@code null}.
	 * 
	 * @param weights
	 *            A {@link DblStream} to determine job weight. Default: each job
	 *            gets a weight of 1.
	 */
	public void setWeights(DblStream weights) {
		this.weights = weights;
	}

}
