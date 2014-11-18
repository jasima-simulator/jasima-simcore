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
import static org.junit.Assert.assertEquals;
import jasima.core.experiment.MultipleReplicationExperiment;
import jasima.core.random.RandomFactory;
import jasima.core.random.continuous.DblDistribution;
import jasima.core.random.continuous.DblStream;
import jasima.core.random.continuous.DblUniformRange;
import jasima.core.random.discrete.IntUniformRange;
import jasima.core.statistics.SummaryStat;
import jasima.shopSim.core.Job;
import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.batchForming.BatchForming;
import jasima.shopSim.core.batchForming.BestOfFamilyBatching;
import jasima.shopSim.models.mimac.MimacExperiment;
import jasima.shopSim.models.mimac.MimacExperiment.DataSet;
import jasima.shopSim.prioRules.basic.FASFS;
import jasima.shopSim.prioRules.batch.LBF;
import jasima.shopSim.prioRules.batch.MaxBatchSize;
import jasima.shopSim.prioRules.setup.SST;

import java.util.Map;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.junit.Before;
import org.junit.Test;

import util.ExtendedJobStatCollector;

/**
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version $Id$
 */
public class TestBestOfFamilyBatching {

	@Before
	public void setUp() {
		System.out.println("setting up");
		System.setProperty(RandomFactory.RANDOM_FACTORY_PROP_KEY,
				RandomFactory.class.getName());
	}

	public static MimacExperiment createBaseExperiment() {
		MimacExperiment e = new MimacExperiment();
		e.setScenario(DataSet.FAB4r);

		DblStream arrivals1 = new DblDistribution(new ExponentialDistribution(
				1440d / 4.5));
		DblStream arrivals2 = new DblDistribution(new ExponentialDistribution(
				1440d / 10.5));
		e.setInterArrivalTimes(new DblStream[] { arrivals1, arrivals2 });

		e.setDueDateFactors(new DblUniformRange(2.0, 5.0));
		e.setJobWeights(new IntUniformRange(1, 10));

		e.setSimulationLength(6 * 365 * 24 * 60);

		ExtendedJobStatCollector stats = new ExtendedJobStatCollector();
		stats.setInitialPeriod(1 * 365 * 24 * 60);
		e.addShopListener(stats);

		e.setEnableLookAhead(false);
		e.setMaxJobsInSystem(3 * 250);
		//
		// PR<Machine, Job> pr = new FBFO();
		// pr.setTieBreaker(new FSFO<Machine>());
		// e.setSequencingRule(pr);
		//
		// e.setBatchingRule(new GreedyBatching());

		return e;
	}

	private Map<String, Object> runExp(BatchForming bf, PR seqRule,
			PR batchSeqRule) {
		MimacExperiment e = createBaseExperiment();

		e.setSequencingRule(seqRule);
		e.setBatchSequencingRule(batchSeqRule);
		e.setBatchForming(bf);

		MultipleReplicationExperiment mre = new MultipleReplicationExperiment();
		mre.setBaseExperiment(e);
		mre.setMaxReplications(20);
		mre.setInitialSeed(8346);

		mre.runExperiment();
		mre.printResults();

		return mre.getResults();
	}

	public static PR createPRStack(PR pr, boolean setupAvoidance) {
		pr.setTieBreaker(new FASFS());

		if (setupAvoidance) {
			PR ms = new SST();
			ms.setTieBreaker(pr);
			pr = ms;
		}

		return pr;
	}

	private PR createBatchSequencingPR(PR r0) {
		PR pr0 = new MaxBatchSize();

		PR pr1 = new LBF();
		pr0.setTieBreaker(pr1);

		PR setupAvoid = new SST();
		pr1.setTieBreaker(setupAvoid);

		PR firstJobWeight = new PR() {
			@Override
			public double calcPrio(PrioRuleTarget j) {
				Job job = j.job(0);
				return job.getWeight();
			}
		};
		setupAvoid.setTieBreaker(firstJobWeight);

		firstJobWeight.setTieBreaker(r0);

		PR firstJobFSFO = new PR() {

			@Override
			public double calcPrio(PrioRuleTarget j) {
				Job job = j.job(0);
				return -job.getJobNum();
			}
		};
		r0.setTieBreaker(firstJobFSFO);

		return pr0;
	}

	private void check(String name, double expected, double precision,
			Map<String, Object> res) {
		SummaryStat vs = (SummaryStat) res.get(name);
		assertEquals(name, expected, vs.mean(), precision);
	}

	@Test
	public void PR_FIFO() throws Exception {
		PR firstJobFBFO = new PR() {

			@Override
			public double calcPrio(PrioRuleTarget j) {
				Job job = j.job(0);
				return -job.getArriveTime();
			}
		};

		PR br = createBatchSequencingPR(firstJobFBFO);

		Map<String, Object> res = runExp(new BestOfFamilyBatching(), br, null);

		check("flowMean", 10069.4671, 0.0001, res);
		check("tardMean", 1750.3194, 0.0001, res);
		check("tardPercentage", 0.3753, 0.0001, res);
		check("weightedTardMean", 3691.5796, 0.0001, res);
		check("numJobsFinished", 32777.4500, 0.0001, res);
		check("baseExperiment.expAborted", 0.0, 0.0001, res);
		// check("flowMean", 12295.6741, 0.0001, res);
		// check("tardMean", 3858.6216, 0.0001, res);
		// check("tardPercentage", 0.3320, 0.0001, res);
		// check("weightedTardMean", 6044.1091, 0.0001, res);
		// check("numJobsFinished", 32685.6000, 0.0001, res);
		// check("baseExperiment.expAborted", 0.0, 0.0001, res);
	}

	@Test
	public void PR_SPT() throws Exception {
		PR firstJobSPT = new PR() {

			@Override
			public double calcPrio(PrioRuleTarget j) {
				Job job = j.job(0);
				return -job.currProcTime();
			}
		};

		PR br = createBatchSequencingPR(firstJobSPT);

		Map<String, Object> res = runExp(new BestOfFamilyBatching(), br, null);

		check("flowMean", 10101.8194, 0.0001, res);
		check("tardMean", 1772.3249, 0.0001, res);
		check("tardPercentage", 0.3823, 0.0001, res);
		check("weightedTardMean", 3766.1882, 0.0001, res);
		check("numJobsFinished", 32777.4500, 0.0001, res);
		check("baseExperiment.expAborted", 0.0, 0.0001, res);
		// check("flowMean", 12350.4961, 0.0001, res);
		// check("tardMean", 3895.8505, 0.0001, res);
		// check("tardPercentage", 0.3391, 0.0001, res);
		// check("weightedTardMean", 6160.6321, 0.0001, res);
		// check("numJobsFinished", 32684.2000, 0.0001, res);
		// check("baseExperiment.expAborted", 0.0, 0.0001, res);
	}

	@Test
	public void PR_EDD() throws Exception {
		PR firstJobEDD = new PR() {

			@Override
			public double calcPrio(PrioRuleTarget j) {
				Job job = j.job(0);
				return -job.getDueDate();
			}
		};

		PR br = createBatchSequencingPR(firstJobEDD);

		Map<String, Object> res = runExp(new BestOfFamilyBatching(), br, null);

		check("flowMean", 10034.7960, 0.0001, res);
		check("tardMean", 1658.0615, 0.0001, res);
		check("tardPercentage", 0.3659, 0.0001, res);
		check("weightedTardMean", 3467.2397, 0.0001, res);
		check("numJobsFinished", 32776.8500, 0.0001, res);
		check("baseExperiment.expAborted", 0.0, 0.0001, res);
		// check("flowMean", 12278.4196, 0.0001, res);
		// check("tardMean", 3785.4248, 0.0001, res);
		// check("tardPercentage", 0.3275, 0.0001, res);
		// check("weightedTardMean", 5842.4113, 0.0001, res);
		// check("numJobsFinished", 32685.3000, 0.0001, res);
		// check("baseExperiment.expAborted", 0.0, 0.0001, res);
	}

	@Test
	public void PR_ODD() throws Exception {
		PR firstJobODD = new PR() {

			@Override
			public double calcPrio(PrioRuleTarget j) {
				Job job = j.job(0);
				return -job.getCurrentOperationDueDate();
			}
		};

		PR br = createBatchSequencingPR(firstJobODD);

		Map<String, Object> res = runExp(new BestOfFamilyBatching(), br, null);

		check("flowMean", 10047.0413, 0.0001, res);
		check("tardMean", 1667.5664, 0.0001, res);
		check("tardPercentage", 0.3712, 0.0001, res);
		check("weightedTardMean", 3461.0373, 0.0001, res);
		check("numJobsFinished", 32778.4000, 0.0001, res);
		check("baseExperiment.expAborted", 0.0, 0.0001, res);
		// check("flowMean", 12290.5842, 0.0001, res);
		// check("tardMean", 3792.4357, 0.0001, res);
		// check("tardPercentage", 0.3303, 0.0001, res);
		// check("weightedTardMean", 5840.7422, 0.0001, res);
		// check("numJobsFinished", 32683.9500, 0.0001, res);
		// check("baseExperiment.expAborted", 0.0, 0.0001, res);
	}

	@Test
	public void PR_CR() throws Exception {
		PR firstJobCR = new PR() {

			@Override
			public double calcPrio(PrioRuleTarget j) {
				Job job = j.job(0);
				double procRem = job.remainingProcTime();

				return -((job.getDueDate() - job.getShop().simTime()) / procRem);
			}
		};

		PR br = createBatchSequencingPR(firstJobCR);

		Map<String, Object> res = runExp(new BestOfFamilyBatching(), br, null);

		check("flowMean", 10042.3550, 0.0001, res);
		check("tardMean", 1659.1827, 0.0001, res);
		check("tardPercentage", 0.3727, 0.0001, res);
		check("weightedTardMean", 3445.0120, 0.0001, res);
		check("numJobsFinished", 32778.4500, 0.0001, res);
		check("baseExperiment.expAborted", 0.0, 0.0001, res);
	}

}
