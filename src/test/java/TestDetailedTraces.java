
/*
This file is part of jasima, the Java simulator for manufacturing and logistics.
 
Copyright 2010-2022 jasima contributors (see license.txt)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
import org.junit.Test;

import jasima.core.util.ExperimentTest;
import jasima.core.util.TraceFileProducer;
import jasima.shopSim.core.PR;
import jasima.shopSim.core.batchForming.BatchForming;
import jasima.shopSim.core.batchForming.BestOfFamilyBatching;
import jasima.shopSim.core.batchForming.HighestJobBatchingMBS;
import jasima.shopSim.core.batchForming.MostCompleteBatch;
import jasima.shopSim.models.dynamicShop.DynamicShopExperiment;
import jasima.shopSim.models.mimac.MimacExperiment;
import jasima.shopSim.models.mimac.MimacExperiment.DataSet;
import jasima.shopSim.prioRules.basic.SPT;
import jasima.shopSim.prioRules.basic.TieBreakerFASFS;
import jasima.shopSim.prioRules.weighted.WMOD;
import util.FileChecker;

/**
 * This is basically the same as {@link TestForAllResults}, but checks the
 * complete details of a trace file.
 * 
 * @author Torsten Hildebrandt
 */
public class TestDetailedTraces extends ExperimentTest {

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
		runMimac4rAndCheck(pr, batchForming, "log_4r_BOF.txt", "testInstances/traceMimac4rBestOfFamily.txt");
	}

	@Test
	public void mimac4rResultsShouldBeReproducibleMostCompleteBatch() {
		PR pr = new WMOD().setFinalTieBreaker(new TieBreakerFASFS());
		BatchForming batchForming = new MostCompleteBatch();
		runMimac4rAndCheck(pr, batchForming, "log_4r_MCB.txt", "testInstances/traceMimac4rMostComplete.txt");
	}

	@Test
	public void mimac4rResultsShouldBeReproducibleHighestJobBatchingMBS() {
		PR pr = new WMOD().setFinalTieBreaker(new TieBreakerFASFS());
		HighestJobBatchingMBS batchForming = new HighestJobBatchingMBS(5.0 / 8.0);
		runMimac4rAndCheck(pr, batchForming, "log_4r_MBS.txt", "testInstances/traceMimac4rMBS.txt");
	}

	private void runMimac4rAndCheck(PR pr, BatchForming batchForming, String outFile, String realFile) {
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
