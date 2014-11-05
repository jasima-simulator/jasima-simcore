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
import jasima.core.experiment.FullFactorialExperiment;
import jasima.core.experiment.MultipleReplicationExperiment;
import jasima.core.random.RandomFactory;
import jasima.core.util.ExperimentTest;
import jasima.core.util.XmlUtil;
import jasima.shopSim.core.PR;
import jasima.shopSim.core.batchForming.BatchForming;
import jasima.shopSim.core.batchForming.BestOfFamilyBatching;
import jasima.shopSim.core.batchForming.HighestJobBatchingMBS;
import jasima.shopSim.core.batchForming.MostCompleteBatch;
import jasima.shopSim.models.dynamicShop.DynamicShopExperiment;
import jasima.shopSim.models.mimac.FlowtimePerProductCollector;
import jasima.shopSim.models.mimac.MimacExperiment;
import jasima.shopSim.models.mimac.MimacExperiment.DataSet;
import jasima.shopSim.prioRules.basic.FCFS;
import jasima.shopSim.prioRules.basic.SPT;
import jasima.shopSim.prioRules.basic.TieBreakerFASFS;
import jasima.shopSim.prioRules.meta.AdaptiveLAThreshold;
import jasima.shopSim.prioRules.setup.SetupAvoidance;
import jasima.shopSim.prioRules.weighted.WMOD;
import jasima.shopSim.util.BasicJobStatCollector;
import jasima.shopSim.util.BatchStatCollector;
import jasima.shopSim.util.MachineStatCollector;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version $Id: TestForAllResults.java 144 2013-11-25 13:23:16Z
 *          THildebrandt@gmail.com $
 */
public class TestForAllResults extends ExperimentTest {

	private static final boolean SAVE_ACTUAL = false;

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

		e.addShopListener(new BasicJobStatCollector());
		e.addMachineListener(new MachineStatCollector());

		BatchStatCollector batchStatCollector = new BatchStatCollector();
		batchStatCollector.setNumBatches(1);
		batchStatCollector.setBatchSize(2000);
		batchStatCollector.setIgnoreFirst(500);
		e.addShopListener(batchStatCollector);

		e.runExperiment();
		e.printResults();

		checkResults(e.getResults(), new File("testInstances/holthausRes.xml"));
	}

	@Test
	public void holthausResultsShouldBeReproducibleWithLAThreshold0() {
		DynamicShopExperiment e = new DynamicShopExperiment();
		e.setInitialSeed(42);
		e.setEnableLookAhead(true);
		e.setSequencingRule(new AdaptiveLAThreshold(0.0)
				.setFinalTieBreaker(new SPT()
						.setFinalTieBreaker(new TieBreakerFASFS())));

		e.addShopListener(new BasicJobStatCollector());
		e.addMachineListener(new MachineStatCollector());

		BatchStatCollector batchStatCollector = new BatchStatCollector();
		batchStatCollector.setNumBatches(1);
		batchStatCollector.setBatchSize(2000);
		batchStatCollector.setIgnoreFirst(500);
		e.addShopListener(batchStatCollector);

		e.runExperiment();
		e.printResults();

		checkResults(e.getResults(), new File("testInstances/holthausRes.xml"));
	}

	@Test
	public void holthausResultsShouldBeReproducibleWithLAThreshold50() {
		DynamicShopExperiment e = new DynamicShopExperiment();
		e.setInitialSeed(42);
		e.setEnableLookAhead(true);
		e.setSequencingRule(new AdaptiveLAThreshold(0.5)
				.setFinalTieBreaker(new SPT()
						.setFinalTieBreaker(new TieBreakerFASFS())));

		e.addShopListener(new BasicJobStatCollector());
		e.addMachineListener(new MachineStatCollector());

		BatchStatCollector batchStatCollector = new BatchStatCollector();
		batchStatCollector.setNumBatches(1);
		batchStatCollector.setBatchSize(2000);
		batchStatCollector.setIgnoreFirst(500);
		e.addShopListener(batchStatCollector);

		e.runExperiment();
		e.printResults();

		checkResults(e.getResults(), new File(
				"testInstances/holthausResLA50.xml"));
	}

	@Test
	public void holthausResultsShouldBeReproducibleWithLAThreshold100() {
		DynamicShopExperiment e = new DynamicShopExperiment();
		e.setInitialSeed(42);
		e.setEnableLookAhead(true);
		e.setSequencingRule(new AdaptiveLAThreshold(1.0)
				.setFinalTieBreaker(new SPT()
						.setFinalTieBreaker(new TieBreakerFASFS())));

		e.addShopListener(new BasicJobStatCollector());
		e.addMachineListener(new MachineStatCollector());

		BatchStatCollector batchStatCollector = new BatchStatCollector();
		batchStatCollector.setNumBatches(1);
		batchStatCollector.setBatchSize(2000);
		batchStatCollector.setIgnoreFirst(500);
		e.addShopListener(batchStatCollector);

		e.runExperiment();
		e.printResults();

		checkResults(e.getResults(), new File(
				"testInstances/holthausResLA100.xml"));
	}

	@Test
	public void holthausMREResultsShouldBeReproducible() {
		DynamicShopExperiment e = new DynamicShopExperiment();
		e.setInitialSeed(42);
		e.setSequencingRule(new SPT().setFinalTieBreaker(new TieBreakerFASFS()));
		e.addShopListener(new BasicJobStatCollector());
		e.addMachineListener(new MachineStatCollector());

		BatchStatCollector batchStatCollector = new BatchStatCollector();
		batchStatCollector.setNumBatches(1);
		batchStatCollector.setBatchSize(2000);
		batchStatCollector.setIgnoreFirst(500);
		e.addShopListener(batchStatCollector);

		MultipleReplicationExperiment mre = new MultipleReplicationExperiment(
				e, 7);
		mre.addKeepResultName("tardMean");
		mre.addKeepResultName("flowMean");
		mre.runExperiment();
		mre.printResults();

		checkResults(mre.getResults(), new File(
				"testInstances/holthausResMult.xml"));
	}

	@Test
	public void holthaus_MRE_FFE_ResultsShouldBeReproducible() {
		DynamicShopExperiment e = new DynamicShopExperiment();
		e.setInitialSeed(42);
		e.setSequencingRule(new SPT().setFinalTieBreaker(new TieBreakerFASFS()));
		e.addShopListener(new BasicJobStatCollector());
		e.addMachineListener(new MachineStatCollector());

		BatchStatCollector batchStatCollector = new BatchStatCollector();
		batchStatCollector.setNumBatches(1);
		batchStatCollector.setBatchSize(2000);
		batchStatCollector.setIgnoreFirst(500);
		e.addShopListener(batchStatCollector);

		MultipleReplicationExperiment mre = new MultipleReplicationExperiment(
				e, 7);
		mre.addKeepResultName("tardMean");
		mre.addKeepResultName("flowMean");

		FullFactorialExperiment ffe = new FullFactorialExperiment();
		ffe.setBaseExperiment(mre);
		ffe.addFactor("baseExperiment.sequencingRule",
				new SPT().setFinalTieBreaker(new TieBreakerFASFS()));
		ffe.addFactor("baseExperiment.sequencingRule",
				new FCFS().setFinalTieBreaker(new TieBreakerFASFS()));
		ffe.addFactors("baseExperiment.utilLevel", new Double[] { 0.8, 0.9 });
		ffe.addKeepResultName("tardMean");
		ffe.addKeepResultName("flowMean");

		ffe.runExperiment();
		ffe.printResults();

		checkResults(ffe.getResults(), new File(
				"testInstances/holthausResMultFFE.xml"));
	}

	@Test
	public void mimac4rResultsShouldBeReproducibleBestOfFamilyBatching() {
		PR pr = new WMOD().setFinalTieBreaker(new TieBreakerFASFS());
		BatchForming batchForming = new BestOfFamilyBatching();
		Map<String, Object> res = runMimac4rAndCheck(pr, batchForming,
				new File("testInstances/mimac4rResBestOfFamilyBatching.xml"),
				false);
	}

	@Test
	public void mimac4rResultsShouldBeReproducibleBestOfFamilyBatchingLAThreshold0() {
		PR pr = new AdaptiveLAThreshold(0.0).setFinalTieBreaker(new WMOD()
				.setFinalTieBreaker(new TieBreakerFASFS()));
		BatchForming batchForming = new BestOfFamilyBatching();
		Map<String, Object> res = runMimac4rAndCheck(pr, batchForming,
				new File("testInstances/mimac4rResBestOfFamilyBatching.xml"),
				true);
	}

	@Test
	public void mimac4rResultsShouldBeReproducibleBestOfFamilyBatchingLAThreshold50() {
		PR pr = new AdaptiveLAThreshold(0.5).setFinalTieBreaker(new WMOD()
				.setFinalTieBreaker(new TieBreakerFASFS()));
		BatchForming batchForming = new BestOfFamilyBatching();
		Map<String, Object> res = runMimac4rAndCheck(pr, batchForming,
				new File("testInstances/mimac4rResBestOfFamilyBatching50.xml"),
				true);
	}

	@Test
	public void mimac4rResultsShouldBeReproducibleBestOfFamilyBatchingLAThreshold100() {
		PR pr = new AdaptiveLAThreshold(1.0).setFinalTieBreaker(new WMOD()
				.setFinalTieBreaker(new TieBreakerFASFS()));
		BatchForming batchForming = new BestOfFamilyBatching();
		Map<String, Object> res = runMimac4rAndCheck(
				pr,
				batchForming,
				new File("testInstances/mimac4rResBestOfFamilyBatching100.xml"),
				true);
	}

	@Test
	public void mimac4rResultsShouldBeReproducibleMostCompleteBatch() {
		PR pr = new WMOD().setFinalTieBreaker(new TieBreakerFASFS());
		BatchForming batchForming = new MostCompleteBatch();
		Map<String, Object> res = runMimac4rAndCheck(pr, batchForming,
				new File("testInstances/mimac4rResMostCompleteBatch.xml"),
				false);
	}

	@Test
	public void mimac4rResultsShouldBeReproducibleMostCompleteBatchLAThreshold0() {
		PR pr = new AdaptiveLAThreshold(0.0).setFinalTieBreaker(new WMOD()
				.setFinalTieBreaker(new TieBreakerFASFS()));
		BatchForming batchForming = new MostCompleteBatch();
		Map<String, Object> res = runMimac4rAndCheck(pr, batchForming,
				new File("testInstances/mimac4rResMostCompleteBatch.xml"), true);
	}

	@Test
	public void mimac4rResultsShouldBeReproducibleMostCompleteBatchLAThreshold50() {
		PR pr = new AdaptiveLAThreshold(0.5).setFinalTieBreaker(new WMOD()
				.setFinalTieBreaker(new TieBreakerFASFS()));
		BatchForming batchForming = new MostCompleteBatch();
		Map<String, Object> res = runMimac4rAndCheck(pr, batchForming,
				new File("testInstances/mimac4rResMostCompleteBatch50.xml"),
				true);
	}

	@Test
	public void mimac4rResultsShouldBeReproducibleMostCompleteBatchLAThreshold100() {
		PR pr = new AdaptiveLAThreshold(1.0).setFinalTieBreaker(new WMOD()
				.setFinalTieBreaker(new TieBreakerFASFS()));
		BatchForming batchForming = new MostCompleteBatch();
		Map<String, Object> res = runMimac4rAndCheck(pr, batchForming,
				new File("testInstances/mimac4rResMostCompleteBatch100.xml"),
				true);
	}

	@Test
	public void mimac4rResultsShouldBeReproducibleHighestJobBatchingMBS() {
		PR pr = new WMOD().setFinalTieBreaker(new TieBreakerFASFS());
		HighestJobBatchingMBS batchForming = new HighestJobBatchingMBS(
				5.0 / 8.0);
		Map<String, Object> res = runMimac4rAndCheck(pr, batchForming,
				new File("testInstances/mimac4rResHighestJobBatchingMBS.xml"),
				false);
	}

	@Test
	public void mimac4rResultsShouldBeReproducibleHighestJobBatchingMBSLAThreshold0() {
		PR pr = new AdaptiveLAThreshold(0.0).setFinalTieBreaker(new WMOD()
				.setFinalTieBreaker(new TieBreakerFASFS()));
		HighestJobBatchingMBS batchForming = new HighestJobBatchingMBS(
				5.0 / 8.0);
		Map<String, Object> res = runMimac4rAndCheck(pr, batchForming,
				new File("testInstances/mimac4rResHighestJobBatchingMBS.xml"),
				true);
	}

	@Test
	public void mimac4rResultsShouldBeReproducibleHighestJobBatchingMBSLAThreshold50() {
		PR pr = new AdaptiveLAThreshold(0.5).setFinalTieBreaker(new WMOD()
				.setFinalTieBreaker(new TieBreakerFASFS()));
		HighestJobBatchingMBS batchForming = new HighestJobBatchingMBS(
				5.0 / 8.0);
		Map<String, Object> res = runMimac4rAndCheck(
				pr,
				batchForming,
				new File("testInstances/mimac4rResHighestJobBatchingMBS50.xml"),
				true);
	}

	@Test
	public void mimac4rResultsShouldBeReproducibleHighestJobBatchingMBSLAThreshold100() {
		PR pr = new AdaptiveLAThreshold(1.0).setFinalTieBreaker(new WMOD()
				.setFinalTieBreaker(new TieBreakerFASFS()));
		HighestJobBatchingMBS batchForming = new HighestJobBatchingMBS(
				5.0 / 8.0);
		Map<String, Object> res = runMimac4rAndCheck(
				pr,
				batchForming,
				new File("testInstances/mimac4rResHighestJobBatchingMBS100.xml"),
				true);
	}

	protected void checkResults(Map<String, Object> actual, File f) {
		if (SAVE_ACTUAL)
			XmlUtil.saveXML(actual, new File(f.getName()));

		Map<String, Object> expected = (Map<String, Object>) XmlUtil.loadXML(f);
		checkResults(actual, expected);
	}

	private Map<String, Object> runMimac4rAndCheck(PR pr,
			BatchForming batchForming, File f, boolean lookAhead) {
		MimacExperiment e = createMIMAC4r(pr, batchForming);
		e.setEnableLookAhead(lookAhead);
		e.runExperiment();
		e.printResults();

		Map<String, Object> res = new HashMap<String, Object>(e.getResults());
		if (SAVE_ACTUAL)
			XmlUtil.saveXML(res, new File(f.getName()));
		// res.remove("weightedCondTardMax");
		// res.remove("weightedCondTardVariance");
		// res.remove("weightedCondTardMean");
		// res.remove("noProcMean");
		// res.remove("noProcMax");
		// res.remove("noProcVariance");
		// res.remove("numTardy");
		// res.remove("weightedNumTardy");
		Map<String, Object> expected = (Map<String, Object>) XmlUtil.loadXML(f);

		checkKeySets(res, expected);
		checkResults(res, expected);

		return res;
	}

	private MimacExperiment createMIMAC4r(PR pr, BatchForming batchForming) {
		MimacExperiment e = new MimacExperiment();
		e.setInitialSeed(42);
		e.setScenario(DataSet.FAB4r);
		e.setSimulationLength(5 * 365 * 24 * 60);
		e.setSequencingRule(pr);
		e.setBatchForming(batchForming);
		e.addShopListener(new BasicJobStatCollector());
		e.addMachineListener(new MachineStatCollector());
		return e;
	}

	@Test
	public void mimac4r_MRE_FFE_ShouldBeReproducible() {
		PR pr1 = new SetupAvoidance().setFinalTieBreaker(new WMOD())
				.setFinalTieBreaker(new TieBreakerFASFS());
		PR pr2 = new SetupAvoidance().setFinalTieBreaker(new FCFS())
				.setFinalTieBreaker(new TieBreakerFASFS());

		BatchForming batchForming = new HighestJobBatchingMBS(5.0 / 8.0);
		BatchForming batchForming2 = new MostCompleteBatch();

		MimacExperiment e = new MimacExperiment();
		e.setInitialSeed(42);
		e.setScenario(DataSet.FAB4r);
		e.setSimulationLength(5 * 365 * 24 * 60);
		e.addShopListener(new BasicJobStatCollector());
		e.addMachineListener(new MachineStatCollector());
		e.addShopListener(new FlowtimePerProductCollector());

		MultipleReplicationExperiment mre = new MultipleReplicationExperiment(
				e, 7);
		mre.setBaseExperiment(e);
		mre.addKeepResultName("tardMean");

		FullFactorialExperiment ffe = new FullFactorialExperiment();
		ffe.setBaseExperiment(mre);
		ffe.addFactors("baseExperiment.sequencingRule", pr1, pr2);
		ffe.addFactors("baseExperiment.batchForming", batchForming,
				batchForming2);
		ffe.addKeepResultName("tardMean");

		ffe.runExperiment();
		ffe.printResults();

		checkResults(ffe.getResults(), new File(
				"testInstances/mimac4r_MRE_FFE.xml"));
	}
}
