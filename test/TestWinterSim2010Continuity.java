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
import static org.junit.Assert.assertEquals;
import jasima.core.experiment.MultipleReplicationExperiment;
import jasima.core.random.RandomFactory;
import jasima.core.random.RandomFactoryOld;
import jasima.core.random.continuous.DblDistribution;
import jasima.core.random.continuous.DblStream;
import jasima.core.random.continuous.DblUniformRange;
import jasima.core.random.discrete.IntUniformRange;
import jasima.core.statistics.SummaryStat;
import jasima.shopSim.core.PR;
import jasima.shopSim.core.batchForming.BatchForming;
import jasima.shopSim.core.batchForming.HighestJobBatchingMBS;
import jasima.shopSim.core.batchForming.MostCompleteBatch;
import jasima.shopSim.models.mimac.MimacExperiment;
import jasima.shopSim.models.mimac.MimacExperiment.DataSet;
import jasima.shopSim.prioRules.basic.CR;
import jasima.shopSim.prioRules.basic.EDD;
import jasima.shopSim.prioRules.basic.FCFS;
import jasima.shopSim.prioRules.basic.ODD;
import jasima.shopSim.prioRules.basic.SPT;
import jasima.shopSim.prioRules.basic.TieBreakerFASFS;
import jasima.shopSim.prioRules.meta.IgnoreFutureJobs;
import jasima.shopSim.prioRules.meta.Inverse;
import jasima.shopSim.prioRules.setup.ATCS;
import jasima.shopSim.prioRules.setup.SST;
import jasima.shopSim.prioRules.weighted.LW;
import jasima.shopSim.prioRules.weighted.WMDD;
import jasima.shopSim.prioRules.weighted.WMOD;
import jasima.shopSim.prioRules.weighted.WSPT;

import java.util.Map;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.junit.Before;
import org.junit.Test;

import util.ExtendedJobStatCollector;
import util.Wintersim2010GPRules.GPRuleSize09;
import util.Wintersim2010GPRules.GPRuleSize110;
import util.Wintersim2010GPRules.GPRuleSize12;
import util.Wintersim2010GPRules.GPRuleSize120;
import util.Wintersim2010GPRules.GPRuleSize122;
import util.Wintersim2010GPRules.GPRuleSize16;
import util.Wintersim2010GPRules.GPRuleSize199;
import util.Wintersim2010GPRules.GPRuleSize20;
import util.Wintersim2010GPRules.GPRuleSize33;
import util.Wintersim2010GPRules.GPRuleSize43;
import util.Wintersim2010GPRules.GPRuleSize98;
import util.Wintersim2010GPRules.GPRuleSize99;

/**
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version $Id: TestWinterSim2010Continuity.java 550 2015-01-23 15:07:23Z
 *          thildebrandt@gmail.com $
 */
@SuppressWarnings("deprecation")
public class TestWinterSim2010Continuity {

	public static void main(String[] args) throws Exception {
		org.junit.runner.JUnitCore
				.main(new String[] { TestWinterSim2010Continuity.class
						.getName() });
	}

	@Before
	public void setUp() {
		System.setProperty(RandomFactory.RANDOM_FACTORY_PROP_KEY,
				RandomFactoryOld.class.getName());
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

	private Map<String, Object> runExp(BatchForming bf, PR seqRule) {
		MimacExperiment e = createBaseExperiment();

		e.setSequencingRule(seqRule);
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
		pr.setFinalTieBreaker(new TieBreakerFASFS());

		if (setupAvoidance) {
			PR ms = new SST();
			ms.setTieBreaker(pr);
			pr = ms;
		}

		return new IgnoreFutureJobs(pr);
	}

	private void check(String name, double expected, double precision,
			Map<String, Object> res) {
		SummaryStat vs = (SummaryStat) res.get(name);
		assertEquals(name, expected, vs.mean(), precision);
	}

	@Test
	public void PR_FIFO() throws Exception {
		Map<String, Object> res = runExp(new MostCompleteBatch(),
				createPRStack(maxWeight(new FCFS()), true));

		check("numJobsStarted", 32812.4000, 0.0001, res);
		check("numJobsFinished", 32708.4500, 0.0001, res);
		check("baseExperiment.expAborted", 0.0, 0.0001, res);
		check("flowMean.mean", 10070.2970, 0.0001, res);
		check("tardMean.mean", 1753.1818, 0.0001, res);
		// check("tardPercentage", 0.3125, 0.0001, res);
		check("tardPercentage", 0.3736, 0.0001, res);
		check("weightedTardMean.mean", 3677.5170, 0.0001, res);
	}

	private PR maxWeight(PR pr) {
		LW lw = new LW();
		lw.setTieBreaker(pr);
		return lw;
	}

	@Test
	public void PR_SPT() throws Exception {
		Map<String, Object> res = runExp(new MostCompleteBatch(),
				createPRStack(maxWeight(new SPT()), true));

		check("numJobsStarted", 32812.4000, 0.0001, res);
		check("numJobsFinished", 32707.0500, 0.0001, res);
		check("baseExperiment.expAborted", 0.0, 0.0001, res);
		check("flowMean.mean", 10101.1469, 0.0001, res);
		check("tardMean.mean", 1770.0235, 0.0001, res);
		// check("tardPercentage", 0.3191, 0.0001, res);
		check("tardPercentage", 0.3814, 0.0001, res);
		check("weightedTardMean.mean", 3742.1392, 0.0001, res);
	}

	@Test
	public void PR_EDD() throws Exception {
		Map<String, Object> res = runExp(new MostCompleteBatch(),
				createPRStack(maxWeight(new EDD()), true));

		check("numJobsStarted", 32812.4000, 0.0001, res);
		check("numJobsFinished", 32709.5000, 0.0001, res);
		check("baseExperiment.expAborted", 0.0, 0.0001, res);
		check("flowMean.mean", 10018.6897, 0.0001, res);
		check("tardMean.mean", 1642.8410, 0.0001, res);
		// check("tardPercentage", 0.3052, 0.0001, res);
		check("tardPercentage", 0.3648, 0.0001, res);
		check("weightedTardMean.mean", 3437.3248, 0.0001, res);
	}

	@Test
	public void PR_ODD() throws Exception {
		Map<String, Object> res = runExp(new MostCompleteBatch(),
				createPRStack(maxWeight(new ODD()), true));

		check("numJobsStarted", 32812.4000, 0.0001, res);
		check("numJobsFinished", 32708.1500, 0.0001, res);
		check("baseExperiment.expAborted", 0.0, 0.0001, res);
		check("flowMean.mean", 10057.2664, 0.0001, res);
		check("tardMean.mean", 1679.9226, 0.0001, res);
		// check("tardPercentage", 0.3097, 0.0001, res);
		check("tardPercentage", 0.3701, 0.0001, res);
		check("weightedTardMean.mean", 3454.8380, 0.0001, res);
	}

	@Test
	public void PR_BuggyCR() throws Exception {
		// there was a bug in the CR-implementation used in the paper, actually
		// computing the invers of the CR rule
		Map<String, Object> res = runExp(new MostCompleteBatch(),
				createPRStack(maxWeight(new Inverse(new CR())), true));

		check("numJobsStarted", 32812.4000, 0.0001, res);
		check("numJobsFinished", 32707.6500, 0.0001, res);
		check("baseExperiment.expAborted", 0.0, 0.0001, res);
		check("flowMean.mean", 10170.5404, 0.0001, res);
		check("tardMean.mean", 1944.7933, 0.0001, res);
		// check("tardPercentage", 0.3072, 0.0001, res);
		check("tardPercentage", 0.3672, 0.0001, res);
		check("weightedTardMean.mean", 4218.3188, 0.0001, res);
	}

	@Test
	public void PR_CR() throws Exception {
		Map<String, Object> res = runExp(new MostCompleteBatch(),
				createPRStack(maxWeight(new CR()), true));

		check("numJobsStarted", 32812.4000, 0.0001, res);
		check("numJobsFinished", 32709.5500, 0.0001, res);
		check("baseExperiment.expAborted", 0.0, 0.0001, res);
		check("flowMean.mean", 10041.8125, 0.0001, res);
		check("tardMean.mean", 1657.5252, 0.0001, res);
		// check("tardPercentage", 0.3109, 0.0001, res);
		check("tardPercentage", 0.3716, 0.0001, res);
		check("weightedTardMean.mean", 3430.1873, 0.0001, res);
	}

	@Test
	public void WSPT() throws Exception {
		Map<String, Object> res = runExp(new MostCompleteBatch(),
				createPRStack(new WSPT(), true));

		check("numJobsStarted", 32812.4000, 0.0001, res);
		check("numJobsFinished", 32706.5000, 0.0001, res);
		check("baseExperiment.expAborted", 0.0, 0.0001, res);
		check("flowMean.mean", 10153.5018, 0.0001, res);
		check("tardMean.mean", 1803.6232, 0.0001, res);
		// check("tardPercentage", 0.3218, 0.0001, res);
		check("tardPercentage", 0.3846, 0.0001, res);
		check("weightedTardMean.mean", 3819.4722, 0.0001, res);
	}

	@Test
	public void WMOD() throws Exception {
		Map<String, Object> res = runExp(new HighestJobBatchingMBS(5.0 / 8.0),
				createPRStack(new WMOD(), true));

		check("numJobsStarted", 32812.4000, 0.0001, res);
		check("numJobsFinished", 32697.4500, 0.0001, res);
		check("baseExperiment.expAborted", 0.0, 0.0001, res);
		check("flowMean.mean", 11082.5380, 0.0001, res);
		check("tardMean.mean", 1046.7995, 0.0001, res);
		// check("tardPercentage", 0.4229, 0.0001, res);
		check("tardPercentage", 0.5053, 0.0001, res);
		check("weightedTardMean.mean", 2548.0594, 0.0001, res);
	}

	@Test
	public void WMDD() throws Exception {
		Map<String, Object> res = runExp(new HighestJobBatchingMBS(5.0 / 8.0),
				createPRStack(new WMDD(), true));

		check("numJobsStarted", 32812.4000, 0.0001, res);
		check("numJobsFinished", 32692.3000, 0.0001, res);
		check("baseExperiment.expAborted", 0.0, 0.0001, res);
		check("flowMean.mean", 11505.4612, 0.0001, res);
		check("tardMean.mean", 1783.2683, 0.0001, res);
		// check("tardPercentage", 0.3355, 0.0001, res);
		check("tardPercentage", 0.4009, 0.0001, res);
		check("weightedTardMean.mean", 4244.5752, 0.0001, res);
	}

	@Test
	public void ATCS() throws Exception {
		Map<String, Object> res = runExp(new HighestJobBatchingMBS(5.0 / 8.0),
				createPRStack(new ATCS(4.5, 0.01), false));

		check("numJobsStarted", 32812.4000, 0.0001, res);
		check("numJobsFinished", 32700.1000, 0.0001, res);
		check("baseExperiment.expAborted", 0.0, 0.0001, res);
		check("flowMean.mean", 10983.6102, 0.0001, res);
		check("tardMean.mean", 1014.3326, 0.0001, res);
		// check("tardPercentage", 0.3498, 0.0001, res);
		check("tardPercentage", 0.4180, 0.0001, res);
		check("weightedTardMean.mean", 2356.2522, 0.0001, res);
	}

	@Test
	public void GPRuleSize09() throws Exception {
		Map<String, Object> res = runExp(new HighestJobBatchingMBS(),
				createPRStack(new GPRuleSize09(), false));

		check("numJobsStarted", 32812.4000, 0.0001, res);
		check("numJobsFinished", 32709.3000, 0.0001, res);
		check("baseExperiment.expAborted", 0.0, 0.0001, res);
		check("flowMean.mean", 10032.9246, 0.0001, res);
		check("tardMean.mean", 580.5907, 0.0001, res);
		// check("tardPercentage", 0.2650, 0.0001, res);
		check("tardPercentage", 0.3167, 0.0001, res);
		check("weightedTardMean.mean", 1679.9557, 0.0001, res);
	}

	@Test
	public void GPRuleSize09_2() throws Exception {
		Map<String, Object> res = runExp(new HighestJobBatchingMBS(1.0 / 8.0),
				createPRStack(new GPRuleSize09(), false));

		check("numJobsStarted", 32812.4000, 0.0001, res);
		check("numJobsFinished", 32709.3000, 0.0001, res);
		check("baseExperiment.expAborted", 0.0, 0.0001, res);
		check("flowMean.mean", 10032.9246, 0.0001, res);
		check("tardMean.mean", 580.5907, 0.0001, res);
		// check("tardPercentage", 0.2650, 0.0001, res);
		check("tardPercentage", 0.3167, 0.0001, res);
		check("weightedTardMean.mean", 1679.9557, 0.0001, res);
	}

	@Test
	public void GPRuleSize12() throws Exception {
		Map<String, Object> res = runExp(new HighestJobBatchingMBS(1.0 / 8.0),
				createPRStack(new GPRuleSize12(), false));

		check("numJobsStarted", 32812.4000, 0.0001, res);
		check("numJobsFinished", 32708.6000, 0.0001, res);
		check("baseExperiment.expAborted", 0.0, 0.0001, res);
		check("flowMean.mean", 10101.0870, 0.0001, res);
		check("tardMean.mean", 880.6234, 0.0001, res);
		// check("tardPercentage", 0.2165, 0.0001, res);
		check("tardPercentage", 0.2587, 0.0001, res);
		check("weightedTardMean.mean", 1472.4718, 0.0001, res);
	}

	@Test
	public void GPRuleSize16() throws Exception {
		Map<String, Object> res = runExp(new HighestJobBatchingMBS(1.0 / 8.0),
				createPRStack(new GPRuleSize16(), false));

		check("numJobsStarted", 32812.4000, 0.0001, res);
		check("numJobsFinished", 32710.1000, 0.0001, res);
		check("baseExperiment.expAborted", 0.0, 0.0001, res);
		check("flowMean.mean", 10029.1028, 0.0001, res);
		check("tardMean.mean", 383.4620, 0.0001, res);
		// check("tardPercentage", 0.2241, 0.0001, res);
		check("tardPercentage", 0.2679, 0.0001, res);
		check("weightedTardMean.mean", 1031.1627, 0.0001, res);
	}

	@Test
	public void GPRuleSize20() throws Exception {
		Map<String, Object> res = runExp(new HighestJobBatchingMBS(1.0 / 8.0),
				createPRStack(new GPRuleSize20(), false));

		check("numJobsStarted", 32812.4000, 0.0001, res);
		check("numJobsFinished", 32708.5000, 0.0001, res);
		check("baseExperiment.expAborted", 0.0, 0.0001, res);
		check("flowMean.mean", 10078.0765, 0.0001, res);
		check("tardMean.mean", 409.4973, 0.0001, res);
		// check("tardPercentage", 0.1909, 0.0001, res);
		check("tardPercentage", 0.2281, 0.0001, res);
		check("weightedTardMean.mean", 915.9900, 0.0001, res);
	}

	@Test
	public void GPRuleSize33() throws Exception {
		Map<String, Object> res = runExp(new HighestJobBatchingMBS(1.0 / 8.0),
				createPRStack(new GPRuleSize33(), false));

		check("numJobsStarted", 32812.4000, 0.0001, res);
		check("numJobsFinished", 32708.5000, 0.0001, res);
		check("baseExperiment.expAborted", 0.0, 0.0001, res);
		check("flowMean.mean", 10162.4517, 0.0001, res);
		check("tardMean.mean", 364.1026, 0.0001, res);
		// check("tardPercentage", 0.2372, 0.0001, res);
		check("tardPercentage", 0.2835, 0.0001, res);
		check("weightedTardMean.mean", 797.1564, 0.0001, res);
	}

	@Test
	public void GPRuleSize43() throws Exception {
		Map<String, Object> res = runExp(new HighestJobBatchingMBS(1.0 / 8.0),
				createPRStack(new GPRuleSize43(), false));

		check("numJobsStarted", 32812.4000, 0.0001, res);
		check("numJobsFinished", 32708.5500, 0.0001, res);
		check("baseExperiment.expAborted", 0.0, 0.0001, res);
		check("flowMean.mean", 10133.4703, 0.0001, res);
		check("tardMean.mean", 342.7144, 0.0001, res);
		// check("tardPercentage", 0.2395, 0.0001, res);
		check("tardPercentage", 0.2862, 0.0001, res);
		check("weightedTardMean.mean", 755.3759, 0.0001, res);
	}

	@Test
	public void GPRuleSize98() throws Exception {
		Map<String, Object> res = runExp(new HighestJobBatchingMBS(1.0 / 8.0),
				createPRStack(new GPRuleSize98(), false));

		check("numJobsStarted", 32812.4000, 0.0001, res);
		check("numJobsFinished", 32708.1500, 0.0001, res);
		check("baseExperiment.expAborted", 0.0, 0.0001, res);
		check("flowMean.mean", 10253.5779, 0.0001, res);
		check("tardMean.mean", 388.6433, 0.0001, res);
		// check("tardPercentage", 0.2129, 0.0001, res);
		check("tardPercentage", 0.2545, 0.0001, res);
		check("weightedTardMean.mean", 793.1874, 0.0001, res);
	}

	@Test
	public void GPRuleSize99() throws Exception {
		Map<String, Object> res = runExp(new HighestJobBatchingMBS(1.0 / 8.0),
				createPRStack(new GPRuleSize99(), false));

		check("numJobsStarted", 32812.4000, 0.0001, res);
		check("numJobsFinished", 32709.0500, 0.0001, res);
		check("baseExperiment.expAborted", 0.0, 0.0001, res);
		check("flowMean.mean", 10092.4825, 0.0001, res);
		check("tardMean.mean", 371.7807, 0.0001, res);
		// check("tardPercentage", 0.2012, 0.0001, res);
		check("tardPercentage", 0.2404, 0.0001, res);
		check("weightedTardMean.mean", 724.8903, 0.0001, res);
	}

	@Test
	public void GPRuleSize110() throws Exception {
		Map<String, Object> res = runExp(new HighestJobBatchingMBS(1.0 / 8.0),
				createPRStack(new GPRuleSize110(), false));

		check("numJobsStarted", 32812.4000, 0.0001, res);
		check("numJobsFinished", 32706.7500, 0.0001, res);
		check("baseExperiment.expAborted", 0.0, 0.0001, res);
		check("flowMean.mean", 10164.0467, 0.0001, res);
		check("tardMean.mean", 343.5415, 0.0001, res);
		// check("tardPercentage", 0.2372, 0.0001, res);
		check("tardPercentage", 0.2835, 0.0001, res);
		check("weightedTardMean.mean", 728.5141, 0.0001, res);
	}

	@Test
	public void GPRuleSize120() throws Exception {
		Map<String, Object> res = runExp(new HighestJobBatchingMBS(1.0 / 8.0),
				createPRStack(new GPRuleSize120(), false));

		check("numJobsStarted", 32812.4000, 0.0001, res);
		check("numJobsFinished", 32707.6000, 0.0001, res);
		check("baseExperiment.expAborted", 0.0, 0.0001, res);
		check("flowMean.mean", 10133.3648, 0.0001, res);
		check("tardMean.mean", 410.8700, 0.0001, res);
		// check("tardPercentage", 0.1857, 0.0001, res);
		check("tardPercentage", 0.2219, 0.0001, res);
		check("weightedTardMean.mean", 746.2192, 0.0001, res);
	}

	@Test
	public void GPRuleSize122() throws Exception {
		Map<String, Object> res = runExp(new HighestJobBatchingMBS(1.0 / 8.0),
				createPRStack(new GPRuleSize122(), false));

		check("numJobsStarted", 32812.4000, 0.0001, res);
		check("numJobsFinished", 32709.2000, 0.0001, res);
		check("baseExperiment.expAborted", 0.0, 0.0001, res);
		check("flowMean.mean", 10163.9971, 0.0001, res);
		check("tardMean.mean", 367.7475, 0.0001, res);
		// check("tardPercentage", 0.1910, 0.0001, res);
		check("tardPercentage", 0.2283, 0.0001, res);
		check("weightedTardMean.mean", 728.1442, 0.0001, res);
	}

	@Test
	public void GPRuleSize199() throws Exception {
		Map<String, Object> res = runExp(new HighestJobBatchingMBS(1.0 / 8.0),
				createPRStack(new GPRuleSize199(), false));

		check("numJobsStarted", 32812.4000, 0.0001, res);
		check("numJobsFinished", 32709.0500, 0.0001, res);
		check("baseExperiment.expAborted", 0.0, 0.0001, res);
		check("flowMean.mean", 10105.1822, 0.0001, res);
		check("tardMean.mean", 396.8288, 0.0001, res);
		// check("tardPercentage", 0.1941, 0.0001, res);
		check("tardPercentage", 0.2320, 0.0001, res);
		check("weightedTardMean.mean", 697.0609, 0.0001, res);
	}

}
