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
import jasima.core.random.RandomFactory;
import jasima.core.util.ExperimentTest;
import jasima.shopSim.core.PR;
import jasima.shopSim.core.batchForming.BatchForming;
import jasima.shopSim.core.batchForming.BestOfFamilyBatching;
import jasima.shopSim.core.batchForming.HighestJobBatchingMBS;
import jasima.shopSim.core.batchForming.MostCompleteBatch;
import jasima.shopSim.models.holthaus.DynamicShopExperiment;
import jasima.shopSim.models.mimac.MimacExperiment;
import jasima.shopSim.models.mimac.MimacExperiment.DataSet;
import jasima.shopSim.prioRules.basic.SPT;
import jasima.shopSim.prioRules.basic.TieBreakerFASFS;
import jasima.shopSim.prioRules.weighted.WMOD;
import jasima.shopSim.util.TraceFileProducer;

import org.junit.BeforeClass;
import org.junit.Test;

import util.FileChecker;

/**
 * This is basically the same as {@link TestForAllResults}, but checks the
 * complete details of a trace file.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version $Id$
 */
public class TestDetailedTraces extends ExperimentTest {

	@BeforeClass
	public static void setUp() {
		System.setProperty(RandomFactory.RANDOM_FACTORY_PROP_KEY,
				RandomFactory.class.getName());
	}

	@Test
	public void holthausResultsShouldBeReproducible() {
		DynamicShopExperiment e = new DynamicShopExperiment();
		e.setInitialSeed(42);
		e.setSequencingRule(new SPT().setFinalTieBreaker(new TieBreakerFASFS()));

		e.addShopListener(new TraceFileProducer("log_HH.txt"));

		e.runExperiment();
		e.printResults();

		FileChecker.checkFiles("log_HH.txt", "testInstances/traceHolthaus.txt");
	}

	@Test
	public void mimac4rResultsShouldBeReproducibleBestOfFamilyBatching() {
		PR pr = new WMOD().setFinalTieBreaker(new TieBreakerFASFS());
		BatchForming batchForming = new BestOfFamilyBatching();
		runMimac4rAndCheck(pr, batchForming, "log_4r_BOF.txt",
				"testInstances/traceMimac4rBestOfFamily.txt");
	}

	@Test
	public void mimac4rResultsShouldBeReproducibleMostCompleteBatch() {
		PR pr = new WMOD().setFinalTieBreaker(new TieBreakerFASFS());
		BatchForming batchForming = new MostCompleteBatch();
		runMimac4rAndCheck(pr, batchForming, "log_4r_MCB.txt",
				"testInstances/traceMimac4rMostComplete.txt");
	}

	@Test
	public void mimac4rResultsShouldBeReproducibleHighestJobBatchingMBS() {
		PR pr = new WMOD().setFinalTieBreaker(new TieBreakerFASFS());
		HighestJobBatchingMBS batchForming = new HighestJobBatchingMBS(
				5.0 / 8.0);
		runMimac4rAndCheck(pr, batchForming, "log_4r_MBS.txt",
				"testInstances/traceMimac4rMBS.txt");
	}

	private void runMimac4rAndCheck(PR pr, BatchForming batchForming,
			String outFile, String realFile) {
		MimacExperiment e = new MimacExperiment();
		e.setInitialSeed(42);
		e.setScenario(DataSet.FAB4r);
		e.setSimulationLength(5 * 365 * 24 * 60);
		e.setSequencingRule(pr);
		e.setBatchForming(batchForming);
		e.addShopListener(new TraceFileProducer(outFile));
		e.runExperiment();
		e.printResults();

		FileChecker.checkFiles(outFile, realFile);
	}

}
