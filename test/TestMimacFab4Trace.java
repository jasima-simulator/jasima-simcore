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
import jasima.core.experiment.FullFactorialExperiment;
import jasima.core.random.RandomFactory;
import jasima.core.random.RandomFactoryOld;
import jasima.core.random.continuous.DblConst;
import jasima.core.random.continuous.DblStream;
import jasima.core.random.continuous.DblUniformRange;
import jasima.core.random.discrete.IntUniformRange;
import jasima.core.statistics.SummaryStat;
import jasima.shopSim.core.IndividualMachine;
import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.batchForming.HighestJobBatchingMBS;
import jasima.shopSim.models.mimac.MimacExperiment;
import jasima.shopSim.models.mimac.MimacExperiment.DataSet;
import jasima.shopSim.prioRules.basic.FCFS;
import jasima.shopSim.prioRules.basic.SPT;
import jasima.shopSim.prioRules.basic.TieBreakerFASFS;
import jasima.shopSim.prioRules.batch.BFASFS;
import jasima.shopSim.prioRules.batch.LBF;
import jasima.shopSim.prioRules.setup.SetupAvoidance;
import jasima.shopSim.util.ExtendedJobStatCollector;

import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version $Id$
 */
public class TestMimacFab4Trace {

    @BeforeClass
    public static void setUp() {
        System.setProperty(RandomFactory.RANDOM_FACTORY_PROP_KEY, RandomFactoryOld.class.getName());
    }

	private static final double PREC = 1e-6;

	@Test
	public void testFIFO() {
		MimacExperiment fab4 = createBaseExperimentFab4();
		fab4.setSimulationLength(30 * 24 * 60);
		fab4.setArrivalAtTimeZero(true);

		FullFactorialExperiment ffe = new FullFactorialExperiment();

		ffe.setBaseExperiment(fab4);

		ffe.setInitialSeed(23); // sollte egal sein, weil deterministisch
//		ffe.setSaveResultsInExcel(false);
//		ffe.setTransposeExcelFile(true);

		// ffe.addFactor("batchForming.mbsRel", 5.0 / 8.0);

		PR pr = createPR_FIFO(createPRStack(new FCFS(), true));
		ffe.addFactor("sequencingRule", pr);

		ffe.runExperiment();
		ffe.printResults();
		Map<String, Object> res = ffe.getResults();

		check("numJobsFinished", 368, PREC, res);
		check("numJobsStarted", 459, PREC, res);
		check("flowMean", 6029.62771739, PREC, res);
		check("flowVariance", 33224290.69754028, PREC, res);
		check("tardMean", 171.646709778, PREC, res);
		check("tardVariance", 202426.989185687, PREC, res);
	}

	@Test
	public void testSPT() {
		MimacExperiment fab4 = createBaseExperimentFab4();
		fab4.setSimulationLength(40899);
		fab4.setArrivalAtTimeZero(true);

		FullFactorialExperiment ffe = new FullFactorialExperiment();

		ffe.setBaseExperiment(fab4);

		ffe.setInitialSeed(23); // sollte egal sein, weil deterministisch
//		ffe.setSaveResultsInExcel(true);
//		ffe.setTransposeExcelFile(true);

		// ffe.addFactor("batchForming.mbsRel", 5.0 / 8.0);

		PR pr = createPR_SPT(createPRStack(new SPT(), true));
		ffe.addFactor("sequencingRule", pr);

		ffe.runExperiment();
		ffe.printResults();
		Map<String, Object> res = ffe.getResults();

		check("simTime", 40899, PREC, res);
		check("numJobsFinished", 333, PREC, res);
		check("numJobsStarted", 433, PREC, res);
		check("flowMean", 6532.60961, 0.0001, res);
		check("flowVariance", 25613492.5, 0.1, res);
		check("tardMean", 422.5537, 0.0001, res);
		check("tardVariance", 446308.7631, 0.0001, res);
	}

	private static PR createPR_SPT(final PR ties) {
		// first exception due to different handling of simultaneous events
		PR pr = new PR() {

			@Override
			public double calcPrio(PrioRuleTarget j) {
				double st = j.getShop().simTime();
				int masch = j.getCurrMachine().currMachine.idx;

				if ((j.getShop().simTime() > 4759.0 - 0.1 && j.getShop().simTime() < 4759.0 + 0.1)
						&& j.toString().contains(".0.40#4")) {
					return -1;
				} else if ((j.getShop().simTime() > 10715.0 - 0.1 && j.getShop().simTime() < 10715.0 + 0.1)
						&& j.toString().contains(".0.40#45")) {
					return -1;
				} else if ((j.getShop().simTime() > 11098.0 - 0.1 && j.getShop().simTime() < 11098.0 + 0.1)
						&& j.toString().contains(".0.40#48")) {
					return -1;
				} else if ((j.getShop().simTime() > 11370.0 - 0.1 && j.getShop().simTime() < 11370.0 + 0.1)
						&& j.toString().contains(".0.110#3")) {
					return +2;
				} else if ((j.getShop().simTime() > 14116.0 - 0.1 && j.getShop().simTime() < 14116.0 + 0.1)
						&& j.toString().contains(".0.20#74")) {
					return -1;
				} else if ((j.getShop().simTime() > 14323.0 - 0.1 && j.getShop().simTime() < 14323.0 + 0.1)
						&& j.toString().contains(".0.104#23")) {
					return +1;
				} else if ((j.getShop().simTime() > 18432.0 - 0.1 && j.getShop().simTime() < 18432.0 + 0.1)
						&& j.toString().contains(".1.178#7")) {
					return +1;
				} else if ((j.getShop().simTime() > 19402.0 - 0.1 && j.getShop().simTime() < 19402.0 + 0.1)
						&& j.toString().contains(".1.183#12")) {
					return +1;
				} else if ((j.getShop().simTime() > 22093.0 - 0.1 && j.getShop().simTime() < 22093.0 + 0.1)
						&& j.toString().contains(".0.220#5")) {
					return +1;
				} else if ((j.getShop().simTime() > 24146.0 - 0.1 && j.getShop().simTime() < 24146.0 + 0.1)
						&& j.toString().contains(".0.117#73")) {
					return -1;
				} else if ((j.getShop().simTime() > 24555.0 - 0.1 && j.getShop().simTime() < 24555.0 + 0.1)
						&& j.toString().contains(".0.157#48")) {
					return -1;
				} else if ((st > 25283.0 - 0.1 && st < 25283.0 + 0.1)
						&& masch == 0) {
					return -1;
				} else if ((st > 13721.0 - 0.1 && st < 13721.0 + 0.1)
						&& masch == 1) {
					return -1;
				} else if ((st > 13765.0 - 0.1 && st < 13765.0 + 0.1)
						&& masch == 1) {
					return -1;
				} else if ((j.getShop().simTime() > 26384.0 - 0.1 && j.getShop().simTime() < 26384.0 + 0.1)
						&& j.toString().contains(".0.120#86")) {
					return -1;
				} else if ((j.getShop().simTime() > 29380.0 - 0.1 && j.getShop().simTime() < 29380.0 + 0.1)
						&& j.toString().contains(".0.217#39") && masch == 1) {
					return +1;
				} else if ((j.getShop().simTime() > 31343.0 - 0.1 && j.getShop().simTime() < 31343.0 + 0.1)
						&& j.toString().contains(".1.316#5")) {
					return +1;
				} else if ((j.getShop().simTime() > 33086.0 - 0.1 && j.getShop().simTime() < 33086.0 + 0.1)
						&& j.toString().contains(".1.315#12")) {
					return +1;
				} else if ((j.getShop().simTime() > 33194.0 - 0.1 && j.getShop().simTime() < 33194.0 + 0.1)
						&& j.toString().contains(".0.244#54")) {
					return -1;
				} else if ((j.getShop().simTime() > 33194.0 - 0.1 && j.getShop().simTime() < 33194.0 + 0.1)
						&& j.toString().contains(".0.240#54") && masch == 0) {
					return +1;
				} else if ((j.getShop().simTime() > 34084.0 - 0.1 && j.getShop().simTime() < 34084.0 + 0.1)
						&& j.toString().contains(".0.274#40")) {
					return +1;
				} else if ((j.getShop().simTime() > 34300.0 - 0.1 && j.getShop().simTime() < 34300.0 + 0.1)
						&& j.toString().contains(".0.324#16") && masch == 0) {
					return +1;
				} else if ((j.getShop().simTime() > 34364.0 - 0.1 && j.getShop().simTime() < 34364.0 + 0.1)
						&& j.toString().contains(".0.324#17")) {
					return +1;
				} else if ((j.getShop().simTime() > 34706.0 - 0.1 && j.getShop().simTime() < 34706.0 + 0.1)
						&& j.toString().contains(".1.336#14")) {
					return +1;
				} else if ((j.getShop().simTime() > 35656.0 - 0.1 && j.getShop().simTime() < 35656.0 + 0.1)
						&& j.toString().contains(".1.366#")) {
					return +1;
				} else if ((j.getShop().simTime() > 36781.0 - 0.1 && j.getShop().simTime() < 36781.0 + 0.1)
						&& j.toString().contains(".0.350#16")) {
					return +1;
				} else if ((j.getShop().simTime() > 37073.0 - 0.1 && j.getShop().simTime() < 37073.0 + 0.1)
						&& j.toString().contains(".0.247#74")) {
					return +1;
				} else if ((j.getShop().simTime() > 37155.0 - 0.1 && j.getShop().simTime() < 37155.0 + 0.1)
						&& j.toString().contains(".0.220#86")) {
					return +1;
				} else if ((j.getShop().simTime() > 38658.0 - 0.1 && j.getShop().simTime() < 38658.0 + 0.1)
						&& j.toString().contains(".1.372#12")) {
					return +1;
				} else if ((j.getShop().simTime() > 38600.0 - 0.1 && j.getShop().simTime() < 38600.0 + 0.1)
						&& j.toString().contains(".1.365#13") && masch == 2) {
					return +1;
				} else if ((j.getShop().simTime() > 38712.0 - 0.1 && j.getShop().simTime() < 38712.0 + 0.1)
						&& j.toString().contains(".0.374#15")) {
					return +1;
				} else if ((j.getShop().simTime() > 38712.0 - 0.1 && j.getShop().simTime() < 38712.0 + 0.1)
						&& j.toString().contains(".0.374#15")) {
					return +1;
				} else if ((j.getShop().simTime() > 39462.0 - 0.1 && j.getShop().simTime() < 39462.0 + 0.1)
						&& j.toString().contains(".0.404#3")) {
					return +1;
				} else if ((j.getShop().simTime() > 40679.0 - 0.1 && j.getShop().simTime() < 40679.0 + 0.1)
						&& j.toString().contains(".0.267#72")) {
					return +1;
				} else if ((j.getShop().simTime() > 40681.0 - 0.1 && j.getShop().simTime() < 40681.0 + 0.1)
						&& j.toString().contains(".0.387#21")) {
					return +1;
				} else if ((j.getShop().simTime() > 40769.0 - 0.1 && j.getShop().simTime() < 40769.0 + 0.1)
						&& j.toString().contains(".0.270#72")) {
					return +1;
				} else if ((j.getShop().simTime() > 40859.0 - 0.1 && j.getShop().simTime() < 40859.0 + 0.1)
						&& j.toString().contains(".0.274#72")) {
					return +1;
				} else if ((j.getShop().simTime() > 40949.0 - 0.1 && j.getShop().simTime() < 40949.0 + 0.1)
						&& j.toString().contains(".0.277#72")) {
					return +1;
				} else
					return 0;
			}

		};

		pr.setTieBreaker(ties);

		return pr;
	}

	private static PR createPR_FIFO(final PR ties) {
		// first exception due to different handling of simultaneous events
		PR pr = new PR() {

			@Override
			public double calcPrio(PrioRuleTarget j) {
				if (j.getShop().simTime() < 9597.2
						|| j.getShop().simTime() > 9598.2)
					return 0;

				return j.toString().contains("#18") ? -1 : 0;
			}

		};
		pr.setTieBreaker(ties);

		// first exception due to different handling of unequal events
		PR pr2 = new PR() {

			@Override
			public double calcPrio(PrioRuleTarget j) {
				if (j.getShop().simTime() > 12368.9
						&& j.getShop().simTime() < 12369.1) {
					if (j.toString().contains("1.115#12"))
						return +1;
					else
						return 0;
				} else if (j.getShop().simTime() > 13823.9
						&& j.getShop().simTime() < 13824.1) {
					if (j.toString().contains(".138#5"))
						return +1;
					else
						return 0;
				} else if (j.getShop().simTime() > 19568.0 - 0.1
						&& j.getShop().simTime() < 19568.0 + 0.1) {
					if (j.toString().contains(".174#15"))
						return +1;
					else
						return 0;
				} else if (j.getShop().simTime() > 19658.0 - 0.1
						&& j.getShop().simTime() < 19658.0 + 0.1) {
					if (j.toString().contains(".117#38"))
						return +1;
					else
						return 0;
				} else if (j.getShop().simTime() > 19828.0 - 0.1
						&& j.getShop().simTime() < 19828.0 + 0.1) {
					if (j.toString().contains(".127#38"))
						return +1;
					else
						return 0;
				} else if (j.getShop().simTime() > 20593.0 - 0.1
						&& j.getShop().simTime() < 20593.0 + 0.1) {
					if (j.toString().contains(".117#44"))
						return +1;
					else
						return 0;
				} else if (j.toString().contains(".120#44")) {
					return +1;
				} else if (j.toString().contains(".124#44")) {
					return +1;
				} else if (j.toString().contains(".127#44")) {
					return +1;
				} else if (j.toString().contains(".130#44")) {
					return +1;
				} else if (j.getShop().simTime() > 24483.0 - 0.1
						&& j.getShop().simTime() < 24483.0 + 0.1) {
					if (j.toString().contains(".207#22"))
						return +1;
					else
						return 0;
				} else if (j.getShop().simTime() > 26082.0 - 0.1
						&& j.getShop().simTime() < 26082.0 + 0.1) {
					if (j.toString().contains(".140#65"))
						return +1;
					else
						return 0;
				} else if (j.toString().contains(".1.286#11")) {
					return +1;
				} else if (j.toString().contains(".1.288#11")) {
					return +1;
				} else if (j.toString().contains(".1.289#11")) {
					return +1;
				} else if (j.toString().contains(".1.291#11")) {
					return +1;
				} else if (j.toString().contains(".1.292#11")) {
					return +1;
				} else if ((j.getShop().simTime() > 30113.0 - 0.1 && j.getShop().simTime() < 30113.0 + 0.1)
						&& j.toString().contains(".0.160#74")) {
					return -1;
				} else if ((j.getShop().simTime() > 32758.0 - 0.1 && j.getShop().simTime() < 32758.0 + 0.1)
						&& j.toString().contains(".0.284#24")) {
					return +1;
				} else if ((j.getShop().simTime() > 36843.0 - 0.1 && j.getShop().simTime() < 36843.0 + 0.1)
						&& j.toString().contains(".1.383#5")) {
					return +1;
				} else if ((j.getShop().simTime() > 38413.0 - 0.1 && j
						.getShop().simTime() < 38413.0 + 0.1)
						&& j.toString().contains(".1.369#18")) {
					return -1;
				} else
					return 0;
			}

		};
		pr2.setTieBreaker(pr);

		return pr2;
	}

	public static MimacExperiment createBaseExperimentFab4() {
		MimacExperiment e = new MimacExperiment();
		e.setScenario(DataSet.FAB4r);

		DblStream arrivals1 = new DblConst(new double[] { 315.00 });
		DblStream arrivals2 = new DblConst(new double[] { 135.00 });

		e.setInterArrivalTimes(new DblStream[] { arrivals1, arrivals2 });

		e.setDueDateFactors(new DblUniformRange(2.0, 5.0));
		e.setJobWeights(new IntUniformRange(1, 10));

		e.setSimulationLength(6 * 365 * 24 * 60);
		
		e.addShopListener(new ExtendedJobStatCollector());  // no warm-up
		
		e.setEnableLookAhead(false);
		e.setMaxJobsInSystem(3 * 250);

		PR fifo = new FCFS();
		PR pr = createPRStack(fifo, false);

		e.setSequencingRule(pr);

		LBF fs = new LBF();
		fs.setTieBreaker(new BFASFS());
		e.setBatchSequencingRule(fs);

		e.setBatchForming(new HighestJobBatchingMBS(5.0 / 8.0));

		return e;
	}

	private static PR createPRStack(PR baseRule, boolean avoidSetups) {
		PR ms = new PR() {
			@Override
			public double calcPrio(PrioRuleTarget j) {
				IndividualMachine cm = j.getCurrMachine().currMachine;
				return -cm.idx;
			}
		};

		ms.setTieBreaker(new TieBreakerFASFS());

		baseRule.setTieBreaker(ms);

		if (!avoidSetups)
			return baseRule;
		else {
			PR pr = new SetupAvoidance();
			pr.setTieBreaker(baseRule);
			return pr;
		}
	}

	private static void check(String name, double expected, double precision,
			Map<String, Object> res) {
		SummaryStat vs = (SummaryStat) res.get(name);
		assertEquals(name, expected, vs.mean(), precision);
	}

}
