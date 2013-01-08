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
package jasima.core.experiment;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import jasima.core.experiment.OCBAExperiment.ProblemType;
import jasima.core.statistics.SummaryStat;
import jasima.core.util.ExcelSaver;
import jasima.shopSim.models.holthaus.HolthausExperiment;
import jasima.shopSim.prioRules.basic.SPT;
import jasima.shopSim.prioRules.basic.TieBreakerFASFS;
import jasima.shopSim.prioRules.upDownStream.PTPlusWINQPlusNPT;
import jasima.shopSim.util.BasicJobStatCollector;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;

import org.junit.Test;

/**
 * Basic tests for the {@link OCBAExperiment} class.
 * 
 * @version $Id$
 */
public class OCBATest {

	@Test
	public void prioRuleSelectionShouldGiveExpectedResults() {
		// create and configure base experiment
		HolthausExperiment he = new HolthausExperiment();
		he.setUtilLevel(0.9);
		he.addShopListener(new BasicJobStatCollector());

		// create OCBA experiment
		OCBAExperiment ocbaExperiment = new OCBAExperiment();
		ocbaExperiment.setInitialSeed(23);

		// set base experiment to use
		ocbaExperiment.setBaseExperiment(he);

		// define configurations to test
		ocbaExperiment.addFactor("sequencingRule",
				new SPT().setFinalTieBreaker(new TieBreakerFASFS()));
		ocbaExperiment.addFactor("sequencingRule", new PTPlusWINQPlusNPT()
				.setFinalTieBreaker(new TieBreakerFASFS()));

		// define objective function
		ocbaExperiment.setObjective("flowMean");
		ocbaExperiment.setProblemType(ProblemType.MINIMIZE);

		// no fixed budget, run until we are pretty sure to have the best
		// configuration
		ocbaExperiment.setNumReplications(0);
		ocbaExperiment.setPcsLevel(0.95);

		// optionally produce an Excel file with results and details
		ocbaExperiment.addNotifierListener(new ExcelSaver());

		// run
		ocbaExperiment.runExperiment();
		ocbaExperiment.printResults();

		Map<String, Object> res = ocbaExperiment.getResults();

		int[] av = (int[]) res.get("allocationVector");
		assertArrayEquals("selection frequency", new int[] { 22, 13 }, av);

		double pcs = (Double) res.get("pcs");
		assertEquals("pcs", 0.97722897, pcs, 0.00001);
	}

	/**
	 * Simple test experiment producing a normally distributed random number as
	 * a result.
	 */
	public static class TextExp extends Experiment {
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
			resultMap.put("mean", rnd.nextGaussian() + mean);
		}

		@Override
		public String toString() {
			return "exp(mean=" + mean + ")";
		}

	}

	@Test
	public void maximizationShouldGiveSameResultsIn1000Runs() throws Exception {
		int NUM_ITERS = 1000;
		int MAX_REPS = 100;
		String OBJ = "mean";
		double[] means = new double[] { 2.0, 2.1, 3.0, 8.0, 8.1, 8.09 };
		ProblemType type = ProblemType.MAXIMIZE;

		performManyOCBARunsAndCheck(NUM_ITERS, MAX_REPS, OBJ, means, type,
				new int[] { 0, 0, 0, 78, 649, 273 }, 552.96, 0.6045642572645857);
	}

	@Test
	public void minimizationShouldGiveSameResultsIn1000Runs() throws Exception {
		int NUM_ITERS = 1000;
		int MAX_REPS = 100;
		String OBJ = "mean";
		double[] means = new double[] { 2.0, 2.1, 3.0, 8.0, 8.1, 8.09 };
		ProblemType type = ProblemType.MINIMIZE;

		performManyOCBARunsAndCheck(NUM_ITERS, MAX_REPS, OBJ, means, type,
				new int[] { 880, 120, 0, 0, 0, 0 }, 375.595, 0.901348254380218);
	}

	@Test
	public void maximizationNoOverallBudgetShouldGiveSameResults()
			throws Exception {
		int NUM_ITERS = 100;
		int MAX_REPS = 0;
		String OBJ = "mean";
		double[] means = new double[] { 2.0, 2.1, 3.0, 8.0, 8.1, 8.09 };
		ProblemType type = ProblemType.MAXIMIZE;

		performManyOCBARunsAndCheck(NUM_ITERS, MAX_REPS, OBJ, means, type,
				new int[] { 0, 0, 0, 9, 74, 17 }, 55957.1, 0.9518400899460198);
	}

	private void performManyOCBARunsAndCheck(int numIters, int maxReps,
			String obs, double[] means, ProblemType type,
			int[] selFreqExpected, double expsPerRunExpected,
			double avgPCSExpected) {
		int[] ocbaResults = new int[means.length];

		SummaryStat evals = new SummaryStat();
		SummaryStat pcs = new SummaryStat();

		Random rnd = new Random(743);

		for (int n = 0; n < numIters; n++) {
			long seed = rnd.nextLong();

			Map<String, Object> res = performSingleOCBARun(maxReps, obs, means,
					type, seed);

			TextExp best = (TextExp) res.get("bestConfiguration");
			int bestIdx = indexOf(best.mean, means);
			evals.value(((Number) res.get("numEvaluations")).doubleValue());
			pcs.value((Double) res.get("pcs"));

			ocbaResults[bestIdx]++;
		}

		System.out.println("freq. selected as best:\t"
				+ Arrays.toString(ocbaResults));
		System.out.println("experiments executed:\t" + evals.mean() + "\t"
				+ evals.min() + "\t" + evals.max() + "\t" + evals.stdDev());
		System.out.println("PCS at end:\t" + pcs.mean() + "\t" + pcs.min()
				+ "\t" + pcs.max() + "\t" + pcs.stdDev());

		assertArrayEquals("selection frequency", selFreqExpected, ocbaResults);
		assertEquals("avgEvaluations", expsPerRunExpected, evals.mean(), 0.01);
		assertEquals("avgPCS", avgPCSExpected, pcs.mean(), 0.0001);
	}

	private Map<String, Object> performSingleOCBARun(int maxReps, String obj,
			double[] means, ProblemType type, long seed) {
		OCBAExperiment exp = new OCBAExperiment();
		exp.setPcsLevel(0.95);
		exp.setBaseExperiment(new TextExp());

		for (double mean : means)
			exp.addFactors("mean", mean);

		exp.setDetailedResults(true);
		exp.setProblemType(type);
		exp.setObjective(obj);
		exp.setMinReplicationsPerConfiguration(5);
		exp.setNumReplications(maxReps);

		exp.setInitialSeed(seed);

		exp.runExperiment();

		Map<String, Object> res = exp.getResults();
		return res;
	}

	private static int indexOf(double mean, double[] perf) {
		for (int i = 0; i < perf.length; i++) {
			if (perf[i] == mean)
				return i;
		}

		return -1;
	}

}
