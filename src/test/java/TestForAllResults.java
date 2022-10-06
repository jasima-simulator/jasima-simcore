
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
import java.io.File;
import java.util.Map;

import org.junit.Test;

import jasima.core.experiment.FullFactorialExperiment;
import jasima.core.experiment.MultipleReplicationExperiment;
import jasima.core.util.ExperimentTest;
import jasima.core.util.FileFormat;
import jasima.core.util.XmlUtil;
import jasima.core.util.observer.NotifierListener;
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
import util.ExtendedJobStatCollector;

/**
 * 
 * @author Torsten Hildebrandt
 */
@SuppressWarnings({ "unused", "deprecation" })
public class TestForAllResults extends ExperimentTest {

	private static final boolean SAVE_ACTUAL = false;

	@Test
	public void holthausResultsShouldBeReproducible() {
		DynamicShopExperiment e = new DynamicShopExperiment();
		e.setInitialSeed(42);
		e.setSequencingRule(new SPT().setFinalTieBreaker(new TieBreakerFASFS()));

		// remove default BasicJobStatCollector
		NotifierListener<?, ?>[] l = e.getShopListener();
		assert l.length == 1 && l[0] instanceof BasicJobStatCollector;
		e.setShopListener(null);
		e.addShopListener(new ExtendedJobStatCollector());
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
		e.setSequencingRule(
				new AdaptiveLAThreshold(0.0).setFinalTieBreaker(new SPT().setFinalTieBreaker(new TieBreakerFASFS())));

		// remove default BasicJobStatCollector
		NotifierListener<?, ?>[] l = e.getShopListener();
		assert l.length == 1 && l[0] instanceof BasicJobStatCollector;
		e.setShopListener(null);

		e.addShopListener(new ExtendedJobStatCollector());
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
		e.setSequencingRule(
				new AdaptiveLAThreshold(0.5).setFinalTieBreaker(new SPT().setFinalTieBreaker(new TieBreakerFASFS())));

		// remove default BasicJobStatCollector
		NotifierListener<?, ?>[] l = e.getShopListener();
		assert l.length == 1 && l[0] instanceof BasicJobStatCollector;
		e.setShopListener(null);

		e.addShopListener(new ExtendedJobStatCollector());
		e.addMachineListener(new MachineStatCollector());

		BatchStatCollector batchStatCollector = new BatchStatCollector();
		batchStatCollector.setNumBatches(1);
		batchStatCollector.setBatchSize(2000);
		batchStatCollector.setIgnoreFirst(500);
		e.addShopListener(batchStatCollector);

		e.runExperiment();
		e.printResults();

		checkResults(e.getResults(), new File("testInstances/holthausResLA50.xml"));
	}

	@Test
	public void holthausResultsShouldBeReproducibleWithLAThreshold100() {
		DynamicShopExperiment e = new DynamicShopExperiment();
		e.setInitialSeed(42);
		e.setEnableLookAhead(true);
		e.setSequencingRule(
				new AdaptiveLAThreshold(1.0).setFinalTieBreaker(new SPT().setFinalTieBreaker(new TieBreakerFASFS())));

		// remove default BasicJobStatCollector
		NotifierListener<?, ?>[] l = e.getShopListener();
		assert l.length == 1 && l[0] instanceof BasicJobStatCollector;
		e.setShopListener(null);
		e.addShopListener(new ExtendedJobStatCollector());
		e.addMachineListener(new MachineStatCollector());

		BatchStatCollector batchStatCollector = new BatchStatCollector();
		batchStatCollector.setNumBatches(1);
		batchStatCollector.setBatchSize(2000);
		batchStatCollector.setIgnoreFirst(500);
		e.addShopListener(batchStatCollector);

		e.runExperiment();
		e.printResults();

		checkResults(e.getResults(), new File("testInstances/holthausResLA100.xml"));
	}

	@Test
	public void holthausMREResultsShouldBeReproducible() {
		DynamicShopExperiment e = new DynamicShopExperiment();
		e.setInitialSeed(42);
		e.setSequencingRule(new SPT().setFinalTieBreaker(new TieBreakerFASFS()));
		// remove default BasicJobStatCollector
		NotifierListener<?, ?>[] l = e.getShopListener();
		assert l.length == 1 && l[0] instanceof BasicJobStatCollector;
		e.setShopListener(null);
		e.addShopListener(new ExtendedJobStatCollector());
		e.addMachineListener(new MachineStatCollector());

		BatchStatCollector batchStatCollector = new BatchStatCollector();
		batchStatCollector.setNumBatches(1);
		batchStatCollector.setBatchSize(2000);
		batchStatCollector.setIgnoreFirst(500);
		e.addShopListener(batchStatCollector);

		MultipleReplicationExperiment mre = new MultipleReplicationExperiment(e, 7);
		mre.addKeepResultName("tardMean");
		mre.addKeepResultName("flowMean");
		mre.runExperiment();
		mre.printResults();

		checkResults(mre.getResults(), new File("testInstances/holthausResMult.xml"));
	}

	@Test
	public void holthaus_MRE_FFE_ResultsShouldBeReproducible() {
		DynamicShopExperiment e = new DynamicShopExperiment();
		e.setInitialSeed(42);
		e.setSequencingRule(new SPT().setFinalTieBreaker(new TieBreakerFASFS()));
		// remove default BasicJobStatCollector
		NotifierListener<?, ?>[] l = e.getShopListener();
		assert l.length == 1 && l[0] instanceof BasicJobStatCollector;
		e.setShopListener(null);
		e.addShopListener(new ExtendedJobStatCollector());
		e.addMachineListener(new MachineStatCollector());

		BatchStatCollector batchStatCollector = new BatchStatCollector();
		batchStatCollector.setNumBatches(1);
		batchStatCollector.setBatchSize(2000);
		batchStatCollector.setIgnoreFirst(500);
		e.addShopListener(batchStatCollector);

		MultipleReplicationExperiment mre = new MultipleReplicationExperiment(e, 7);
		mre.addKeepResultName("tardMean");
		mre.addKeepResultName("flowMean");

		FullFactorialExperiment ffe = new FullFactorialExperiment();
		ffe.setBaseExperiment(mre);
		ffe.addFactorValue("baseExperiment.sequencingRule", new SPT().setFinalTieBreaker(new TieBreakerFASFS()));
		ffe.addFactorValue("baseExperiment.sequencingRule", new FCFS().setFinalTieBreaker(new TieBreakerFASFS()));
		ffe.addFactor("baseExperiment.utilLevel", 0.8, 0.9);
		ffe.addKeepResultName("tardMean");
		ffe.addKeepResultName("flowMean");

		ffe.runExperiment();
		ffe.printResults();

		checkResults(ffe.getResults(), new File("testInstances/holthausResMultFFE.xml"));
	}

	@Test
	public void mimac4rResultsShouldBeReproducibleBestOfFamilyBatching() {
		PR pr = new WMOD().setFinalTieBreaker(new TieBreakerFASFS());
		BatchForming batchForming = new BestOfFamilyBatching();
		Map<String, Object> res = runMimac4rAndCheck(pr, batchForming,
				new File("testInstances/mimac4rResBestOfFamilyBatching.xml"), false);
	}

	@Test
	public void mimac4rResultsShouldBeReproducibleBestOfFamilyBatchingLAThreshold0() {
		PR pr = new AdaptiveLAThreshold(0.0).setFinalTieBreaker(new WMOD()).setFinalTieBreaker(new TieBreakerFASFS());
		BatchForming batchForming = new BestOfFamilyBatching();
		Map<String, Object> res = runMimac4rAndCheck(pr, batchForming,
				new File("testInstances/mimac4rResBestOfFamilyBatching.xml"), true);
	}

	@Test
	public void mimac4rResultsShouldBeReproducibleBestOfFamilyBatchingLAThreshold50() {
		PR pr = new AdaptiveLAThreshold(0.5).setFinalTieBreaker(new WMOD().setFinalTieBreaker(new TieBreakerFASFS()));
		BatchForming batchForming = new BestOfFamilyBatching();
		Map<String, Object> res = runMimac4rAndCheck(pr, batchForming,
				new File("testInstances/mimac4rResBestOfFamilyBatching50.xml"), true);
	}

	@Test
	public void mimac4rResultsShouldBeReproducibleBestOfFamilyBatchingLAThreshold100() {
		PR pr = new AdaptiveLAThreshold(1.0).setFinalTieBreaker(new WMOD().setFinalTieBreaker(new TieBreakerFASFS()));
		BatchForming batchForming = new BestOfFamilyBatching();
		Map<String, Object> res = runMimac4rAndCheck(pr, batchForming,
				new File("testInstances/mimac4rResBestOfFamilyBatching100.xml"), true);
	}

	@Test
	public void mimac4rResultsShouldBeReproducibleMostCompleteBatch() {
		PR pr = new WMOD().setFinalTieBreaker(new TieBreakerFASFS());
		BatchForming batchForming = new MostCompleteBatch();
		Map<String, Object> res = runMimac4rAndCheck(pr, batchForming,
				new File("testInstances/mimac4rResMostCompleteBatch.xml"), false);
	}

	@Test
	public void mimac4rResultsShouldBeReproducibleMostCompleteBatchLAThreshold0() {
		PR pr = new AdaptiveLAThreshold(0.0).setFinalTieBreaker(new WMOD().setFinalTieBreaker(new TieBreakerFASFS()));
		BatchForming batchForming = new MostCompleteBatch();
		Map<String, Object> res = runMimac4rAndCheck(pr, batchForming,
				new File("testInstances/mimac4rResMostCompleteBatch.xml"), true);
	}

	@Test
	public void mimac4rResultsShouldBeReproducibleMostCompleteBatchLAThreshold50() {
		PR pr = new AdaptiveLAThreshold(0.5).setFinalTieBreaker(new WMOD().setFinalTieBreaker(new TieBreakerFASFS()));
		BatchForming batchForming = new MostCompleteBatch();
		Map<String, Object> res = runMimac4rAndCheck(pr, batchForming,
				new File("testInstances/mimac4rResMostCompleteBatch50.xml"), true);
	}

	@Test
	public void mimac4rResultsShouldBeReproducibleMostCompleteBatchLAThreshold100() {
		PR pr = new AdaptiveLAThreshold(1.0).setFinalTieBreaker(new WMOD().setFinalTieBreaker(new TieBreakerFASFS()));
		BatchForming batchForming = new MostCompleteBatch();
		Map<String, Object> res = runMimac4rAndCheck(pr, batchForming,
				new File("testInstances/mimac4rResMostCompleteBatch100.xml"), true);
	}

	@Test
	public void mimac4rResultsShouldBeReproducibleHighestJobBatchingMBS() {
		PR pr = new WMOD().setFinalTieBreaker(new TieBreakerFASFS());
		HighestJobBatchingMBS batchForming = new HighestJobBatchingMBS(5.0 / 8.0);
		Map<String, Object> res = runMimac4rAndCheck(pr, batchForming,
				new File("testInstances/mimac4rResHighestJobBatchingMBS.xml"), false);
	}

	@Test
	public void mimac4rResultsShouldBeReproducibleHighestJobBatchingMBSLAThreshold0() {
		PR pr = new AdaptiveLAThreshold(0.0).setFinalTieBreaker(new WMOD().setFinalTieBreaker(new TieBreakerFASFS()));
		HighestJobBatchingMBS batchForming = new HighestJobBatchingMBS(5.0 / 8.0);
		Map<String, Object> res = runMimac4rAndCheck(pr, batchForming,
				new File("testInstances/mimac4rResHighestJobBatchingMBS.xml"), true);
	}

	@Test
	public void mimac4rResultsShouldBeReproducibleHighestJobBatchingMBSLAThreshold50() {
		PR pr = new AdaptiveLAThreshold(0.5).setFinalTieBreaker(new WMOD().setFinalTieBreaker(new TieBreakerFASFS()));
		HighestJobBatchingMBS batchForming = new HighestJobBatchingMBS(5.0 / 8.0);
		Map<String, Object> res = runMimac4rAndCheck(pr, batchForming,
				new File("testInstances/mimac4rResHighestJobBatchingMBS50.xml"), true);
	}

	@Test
	public void mimac4rResultsShouldBeReproducibleHighestJobBatchingMBSLAThreshold100() {
		PR pr = new AdaptiveLAThreshold(1.0).setFinalTieBreaker(new WMOD().setFinalTieBreaker(new TieBreakerFASFS()));
		HighestJobBatchingMBS batchForming = new HighestJobBatchingMBS(5.0 / 8.0);
		Map<String, Object> res = runMimac4rAndCheck(pr, batchForming,
				new File("testInstances/mimac4rResHighestJobBatchingMBS100.xml"), true);
	}

	@SuppressWarnings("unchecked")
	protected void checkResults(Map<String, Object> actual, File f) {
		File fTmp = new File(f.getName());
		XmlUtil.saveXML(FileFormat.XSTREAM, actual, fTmp);
		Map<String, Object> res = (Map<String, Object>) XmlUtil.loadXML(FileFormat.XSTREAM, fTmp);
		if (!SAVE_ACTUAL)
			fTmp.delete();

		Map<String, Object> expected = (Map<String, Object>) XmlUtil.loadXML(FileFormat.XSTREAM, f);
		checkKeySets(actual, expected);
		checkResults(actual, expected);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> runMimac4rAndCheck(PR pr, BatchForming batchForming, File f, boolean lookAhead) {
		MimacExperiment e = createMIMAC4r(pr, batchForming);
		e.setEnableLookAhead(lookAhead);
		e.runExperiment();
		e.printResults();

		// test saving/loading xml in the process
		File fTmp = new File(f.getName());
		XmlUtil.saveXML(FileFormat.XSTREAM, e.getResults(), fTmp);
		Map<String, Object> res = (Map<String, Object>) XmlUtil.loadXML(FileFormat.XSTREAM, fTmp);
		if (!SAVE_ACTUAL)
			fTmp.delete();

		Map<String, Object> expected = (Map<String, Object>) XmlUtil.loadXML(FileFormat.XSTREAM, f);

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
		e.addShopListener(new ExtendedJobStatCollector());
		e.addMachineListener(new MachineStatCollector());
		return e;
	}

	@Test
	public void mimac4r_MRE_FFE_ShouldBeReproducible() {
		PR pr1 = new SetupAvoidance().setFinalTieBreaker(new WMOD()).setFinalTieBreaker(new TieBreakerFASFS());
		PR pr2 = new SetupAvoidance().setFinalTieBreaker(new FCFS()).setFinalTieBreaker(new TieBreakerFASFS());

		BatchForming batchForming = new HighestJobBatchingMBS(5.0 / 8.0);
		BatchForming batchForming2 = new MostCompleteBatch();

		MimacExperiment e = new MimacExperiment();
		e.setInitialSeed(42);
		e.setScenario(DataSet.FAB4r);
		e.setSimulationLength(5 * 365 * 24 * 60);
		e.addShopListener(new ExtendedJobStatCollector());
		e.addMachineListener(new MachineStatCollector());
		e.addShopListener(new FlowtimePerProductCollector());

		MultipleReplicationExperiment mre = new MultipleReplicationExperiment(e, 7);
		mre.setBaseExperiment(e);
		mre.addKeepResultName("tardMean");

		FullFactorialExperiment ffe = new FullFactorialExperiment();
		ffe.setBaseExperiment(mre);
		ffe.addFactor("baseExperiment.sequencingRule", pr1, pr2);
		ffe.addFactor("baseExperiment.batchForming", batchForming, batchForming2);
		ffe.addKeepResultName("tardMean");

		ffe.runExperiment();
		ffe.printResults();

		checkResults(ffe.getResults(), new File("testInstances/mimac4r_MRE_FFE.xml"));
	}
}
