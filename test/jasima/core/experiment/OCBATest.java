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
package jasima.core.experiment;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;

import jasima.core.experiment.OCBAExperiment.ProblemType;
import jasima.core.run.ConsoleRunner;
import jasima.core.statistics.SummaryStat;
import jasima.shopSim.models.dynamicShop.DynamicShopExperiment;
import jasima.shopSim.prioRules.basic.SPT;
import jasima.shopSim.prioRules.basic.TieBreakerFASFS;
import jasima.shopSim.prioRules.upDownStream.PTPlusWINQPlusNPT;
import util.ExtendedJobStatCollector;

/**
 * Basic tests for the {@link OCBAExperiment} class.
 */
@SuppressWarnings("deprecation")
public class OCBATest {

	@Test
//	@Ignore
	public void prioRuleSelectionShouldGiveExpectedResults() {
		// create and configure base experiment
		DynamicShopExperiment he = new DynamicShopExperiment();
		he.setUtilLevel(0.9);
		he.addShopListener(new ExtendedJobStatCollector());

//		ConsoleRunner.run(he);

		// create OCBA experiment
		OCBAExperiment ocbaExperiment = new OCBAExperiment();
		ocbaExperiment.setInitialSeed(23);

		// set base experiment to use
		ocbaExperiment.setBaseExperiment(he);

		// define configurations to test
		ocbaExperiment.addFactor("sequencingRule", new SPT().setFinalTieBreaker(new TieBreakerFASFS()));
		ocbaExperiment.addFactor("sequencingRule", new PTPlusWINQPlusNPT().setFinalTieBreaker(new TieBreakerFASFS()));

		// define objective function
		ocbaExperiment.setObjective("flowMean");
		ocbaExperiment.setProblemType(ProblemType.MINIMIZE);

		// no fixed budget, run until we are pretty sure to have the best
		// configuration
		ocbaExperiment.setNumReplicationsPerConfiguration(0);
		ocbaExperiment.setPcsLevel(0.95);
		ocbaExperiment.setMinReplicationsPerConfiguration(5);

		// optionally produce an Excel file with results and details
//		 ocbaExperiment.addListener(new ExcelSaver());

		// run
		Map<String, Object> res = ConsoleRunner.run(ocbaExperiment);

		int[] av = (int[]) res.get("allocationVector");
		assertArrayEquals("selection frequency", new int[] { 22, 13 }, av);

		double pcs = (Double) res.get("pcs");
		assertEquals("pcs", 0.97722897, pcs, 0.00001);
	}

	/**
	 * Simple test experiment producing a normally distributed random number as a
	 * result.
	 */
	public static class TestExp extends Experiment {
		private static final long serialVersionUID = 4751403145008829877L;

		private double mean;
		private Random rnd;

		public void setMean(double mean) {
			this.mean = mean;
		}

		public double getMean() {
			return mean;
		}

		@Override
		public void init() {
			super.init();
			rnd = new Random(getInitialSeed());
		}

		@Override
		protected void performRun() {

		}

		@Override
		public void produceResults() {
			super.produceResults();
			double res = rnd.nextGaussian() + mean;
//			System.out.println("value: "+res);
			resultMap.put("mean", res);
			resultMap.put("meanAsSummaryStat", SummaryStat.summarize(res));
		}

		@Override
		public String toString() {
			return "exp(mean=" + mean + ")";
		}

	}

	@Test
	public void testSingleRun() throws Exception {
		int MAX_REPS = 100;
		String OBJ = "mean";
		Double[] means = new Double[] { 2.0, 2.1, 3.0, 8.0, 8.1, 8.09 };
		ProblemType type = ProblemType.MAXIMIZE;

		Map<String, Object> res = performSingleOCBARun(MAX_REPS, OBJ, means, type, 23);
		System.out.println(res.toString());
	}

	@Test
	public void testSingleRunForSummaryStat() throws Exception {
		int MAX_REPS = 100;
		String OBJ = "meanAsSummaryStat";
		Double[] means = new Double[] { 2.0, 2.1, 3.0, 8.0, 8.1, 8.09 };
		ProblemType type = ProblemType.MAXIMIZE;

		Map<String, Object> res = performSingleOCBARun(MAX_REPS, OBJ, means, type, 23);
		System.out.println(res.toString());
	}

	@Test
	public void testSingleRun1() throws Exception {
		int MAX_REPS = 100;
		String OBJ = "mean";
		Double[] means = new Double[] { 2.0 };
		ProblemType type = ProblemType.MAXIMIZE;

		Map<String, Object> res = performSingleOCBARun(MAX_REPS, OBJ, means, type, 23, 0.0);
		System.out.println(res.toString());
	}

	@Test
	@Ignore
	public void maximizationShouldGiveSameResultsIn1000Runs() throws Exception {
		int NUM_ITERS = 1000;
		int MAX_REPS = 100;
		String OBJ = "mean";
		Double[] means = new Double[] { 2.0, 2.1, 3.0, 8.0, 8.1, 8.09 };
		ProblemType type = ProblemType.MAXIMIZE;

		performManyOCBARunsAndCheck(NUM_ITERS, MAX_REPS, OBJ, means, type, new int[] { 0, 0, 0, 80, 652, 268 }, 553.53,
				0.602976739490846);
	}

	@Test
	@Ignore
	public void minimizationShouldGiveSameResultsIn1000Runs() throws Exception {
		int NUM_ITERS = 1000;
		int MAX_REPS = 100;
		String OBJ = "mean";
		Double[] means = new Double[] { 2.0, 2.1, 3.0, 8.0, 8.1, 8.09 };
		ProblemType type = ProblemType.MINIMIZE;

		performManyOCBARunsAndCheck(NUM_ITERS, MAX_REPS, OBJ, means, type, new int[] { 880, 120, 0, 0, 0, 0 }, 375.595,
				0.901348254380218);
	}

	@Test
	@Ignore
	public void maximizationNoOverallBudgetShouldGiveSameResults() throws Exception {
		int NUM_ITERS = 100;
		int MAX_REPS = 0;
		String OBJ = "mean";
		Double[] means = new Double[] { 2.0, 2.1, 3.0, 8.0, 8.1, 8.09 };
		ProblemType type = ProblemType.MAXIMIZE;

		performManyOCBARunsAndCheck(NUM_ITERS, MAX_REPS, OBJ, means, type, new int[] { 0, 0, 0, 9, 75, 16 }, 36839.95,
				0.9518640709807842);
	}

	private void performManyOCBARunsAndCheck(int numIters, int maxReps, String obs, Double[] means, ProblemType type,
			int[] selFreqExpected, double expsPerRunExpected, double avgPCSExpected) {
		System.out.println();
		int[] ocbaResults = new int[means.length];

		SummaryStat evals = new SummaryStat();
		SummaryStat pcs = new SummaryStat();

		Random rnd = new Random(743);

		for (int n = 0; n < numIters; n++) {
			long seed = rnd.nextLong();

			Map<String, Object> res = performSingleOCBARun(maxReps, obs, means, type, seed);

			TestExp best = (TestExp) res.get("bestConfiguration");
			int bestIdx = indexOf(best.mean, means);
			evals.value(((Number) res.get("numEvaluations")).doubleValue());
			pcs.value((Double) res.get("pcs"));

			ocbaResults[bestIdx]++;
		}

		System.out.println("freq. selected as best:\t" + Arrays.toString(ocbaResults));
		System.out.println("experiments executed:\t" + evals.mean() + "\t" + evals.min() + "\t" + evals.max() + "\t"
				+ evals.stdDev());
		System.out.println("PCS at end:\t" + pcs.mean() + "\t" + pcs.min() + "\t" + pcs.max() + "\t" + pcs.stdDev());

		assertArrayEquals("selection frequency", selFreqExpected, ocbaResults);
		assertEquals("avgEvaluations", expsPerRunExpected, evals.mean(), 0.01);
		assertEquals("avgPCS", avgPCSExpected, pcs.mean(), 0.0001);
	}

	private Map<String, Object> performSingleOCBARun(int maxReps, String obj, Double[] means, ProblemType type,
			long seed) {
		return performSingleOCBARun(maxReps, obj, means, type, seed, 0.95);
	}

	private Map<String, Object> performSingleOCBARun(int maxReps, String obj, Double[] means, ProblemType type,
			long seed, double pcsLevel) {
		OCBAExperiment exp = new OCBAExperiment();
		exp.setPcsLevel(pcsLevel);
		exp.setBaseExperiment(new TestExp());
//exp.setAllowParallelExecution(false);
		exp.addFactors("mean", means);

		exp.setDetailedResults(true);
		exp.setProblemType(type);
		exp.setObjective(obj);
		exp.setMinReplicationsPerConfiguration(5);
		exp.setNumReplicationsPerConfiguration(maxReps);

		exp.setInitialSeed(seed);

		return exp.runExperiment();
	}

	private static int indexOf(double mean, Double[] perf) {
		for (int i = 0; i < perf.length; i++) {
			if (perf[i] == mean)
				return i;
		}

		return -1;
	}

}
