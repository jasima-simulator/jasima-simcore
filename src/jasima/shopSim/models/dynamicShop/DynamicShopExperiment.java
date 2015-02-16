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
package jasima.shopSim.models.dynamicShop;

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
import jasima.shopSim.util.BasicJobStatCollector;
import jasima.shopSim.util.ShopListenerBase;

import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.math3.distribution.ExponentialDistribution;

/**
 * Simulates dynamic job shops and flow shops, based on some parameters. See
 * Rajendran, C.; Holthaus O.:
 * "A Comparative Study of Dispatching Rules in Dynamic Flowshops and Jobshops",
 * European Journal of Operational Research 116 (1999) 1, S. 156-170 for
 * details.
 * <p>
 * An experiment of this type by default contains a
 * {@code BasicJobStatCollector}.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version 
 *          "$Id$"
 * @see BasicJobStatCollector
 */
public class DynamicShopExperiment extends JobShopExperiment {

	private static final long serialVersionUID = -7289579158397939550L;

	public enum Scenario {
		JOB_SHOP, FLOW_SHOP
	};

	private static final double MINUTES_PER_DAY = 24 * 60;

	private double utilLevel = 0.85d;
	private DblStream dueDateFactor = new DblConst(4.0d);
	private int numMachines = 10;
	private Scenario scenario = Scenario.JOB_SHOP;
	private DblStream weights = null;
	private Pair<Integer, Integer> numOps = new Pair<Integer, Integer>(-1, -1);
	private DblStream procTimes = new IntUniformRange(1, 49);
	private int stopArrivalsAfterNumJobs = 2500;

	protected JobSource src;

	public DynamicShopExperiment() {
		super();
		addShopListener(new BasicJobStatCollector());
	}

	@Override
	public void init() {
		super.init();

		if (getScenario() == null)
			throw new IllegalArgumentException(String.format(Util.DEF_LOCALE,
					"No scenario specified, should be one of %s.",
					Arrays.toString(Scenario.values())));

		Objects.requireNonNull(procTimes);

		@SuppressWarnings("serial")
		ShopListenerBase stopSrc = new ShopListenerBase() {
			int maxJob = getStopArrivalsAfterNumJobs();
			int numJobs = maxJob;

			@Override
			protected void jobFinished(JobShop shop, Job j) {
				// stop arrivals after the first, e.g., 2500, jobs were
				// completed
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
	protected void configureShop() {
		super.configureShop();

		createMachines();

		src = createJobSource();
		shop.addJobSource(src);

		if (getStopAfterNumJobs() <= 0)
			shop.setStopAfterNumJobs(10 * getStopArrivalsAfterNumJobs());
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

		int min = getNumOpsMin() > 0 ? getNumOpsMin() : getNumMachines();
		int max = getNumOpsMax() > 0 ? getNumOpsMax() : getNumMachines();
		if (min > max)
			throw new IllegalArgumentException(String.format(Util.DEF_LOCALE,
					"invalid range for numOps: [%d; %d]", getNumOpsMin(),
					getNumOpsMax()));
		if (max > getNumMachines())
			throw new IllegalArgumentException(
					String.format(
							Util.DEF_LOCALE,
							"Can't have more operations (%d) than there are machines (%d).",
							max, getNumMachines()));
		IntUniformRange numOps = new IntUniformRange("numOpsStream", min, max);
		src.setNumOps(numOps);

		DblStream procTimes2 = Util.cloneIfPossible(getProcTimes());
		procTimes2.setName("procTimesStream");
		src.setProcTimes(procTimes2);

		src.setMachIdx(new IntUniformRange("machIdxStream", 0,
				getNumMachines() - 1));

		src.setDueDateFactors(Util.cloneIfPossible(getDueDateFactor()));

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
		double meanOpProc = getProcTimes().getNumericalMean();

		double jobsPerDay = getUtilLevel() * getNumMachines() * MINUTES_PER_DAY
				/ (meanOps * meanOpProc);
		return (1.0d * MINUTES_PER_DAY / jobsPerDay);
	}

	public double getUtilLevel() {
		return utilLevel;
	}

	/**
	 * Sets the desired utilization level for all machines. Machine utilization
	 * approaches this value in the long term; short term results might differ
	 * due to random influences in the arrival process.
	 */
	public void setUtilLevel(double utilLevel) {
		if (utilLevel < 0.0d || utilLevel > 1.0d)
			throw new IllegalArgumentException("" + utilLevel);

		this.utilLevel = utilLevel;
	}

	public DblStream getDueDateFactor() {
		return dueDateFactor;
	}

	/**
	 * Sets the due date tightness of jobs by specifying a due date factor. The
	 * {@link DblStream} is used to calculate a job's due date as a multiple of
	 * a job's processing time. If for instance a due date factor of 2 is
	 * returned for a certain job then the due date is set to the job's release
	 * date plus twice the raw processing time of all operations of this job.
	 */
	public void setDueDateFactor(DblStream dueDateFactor) {
		this.dueDateFactor = dueDateFactor;
	}

	public int getNumMachines() {
		return numMachines;
	}

	/**
	 * Sets the number of machines on the shop floor.
	 */
	public void setNumMachines(int numMachines) {
		if (numMachines < 1)
			throw new IllegalArgumentException("" + numMachines);

		this.numMachines = numMachines;
	}

	/** Returns the minimum number of operations of a job. */
	public int getNumOpsMin() {
		return numOps.a;
	}

	/**
	 * Sets the minimum number of operations of a job. Setting this to a value
	 * {@code <=0} uses the number of machines, i.e., each job has to visit each
	 * machine exactly once.
	 */
	public void setNumOpsMin(int min) {
		numOps = new Pair<Integer, Integer>(min, numOps.b);
	}

	/**
	 * Returns the maximum number of operations of a job. Setting this to a
	 * value {@code <=0} uses the number of machines, i.e., a job with the
	 * maximum number of operations has to visit each machine exactly once.
	 */
	public int getNumOpsMax() {
		return numOps.b;
	}

	/** Sets the maximum number of operations of a job. */
	public void setNumOpsMax(int max) {
		numOps = new Pair<Integer, Integer>(numOps.a, max);
	}

	public void setNumOps(int min, int max) {
		if (min < 0 || (max < min))
			throw new IllegalArgumentException("[" + min + ";" + max + "]");
		numOps = new Pair<Integer, Integer>(min, max);
	}

	/**
	 * Sets the scenario to use. This can be either {@code JOB_SHOP} or
	 * {@code FLOW_SHOP}.
	 */
	public void setScenario(Scenario scenario) {
		this.scenario = scenario;
	}

	public Scenario getScenario() {
		return scenario;
	}

	public int getStopArrivalsAfterNumJobs() {
		return stopArrivalsAfterNumJobs;
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
	public void setStopArrivalsAfterNumJobs(int stopAfterNumJobs) {
		this.stopArrivalsAfterNumJobs = stopAfterNumJobs;
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

	public DblStream getProcTimes() {
		return procTimes;
	}

	/**
	 * Determines the processing times for each operation. This is a mandatory
	 * setting.
	 */
	public void setProcTimes(DblStream procTimes) {
		this.procTimes = procTimes;
	}

}
