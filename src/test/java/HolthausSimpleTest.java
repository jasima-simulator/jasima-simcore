
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
import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jasima.core.random.RandomFactory;
import jasima.core.random.RandomFactoryOld;
import jasima.core.random.continuous.DblConst;
import jasima.core.statistics.SummaryStat;
import jasima.core.util.observer.NotifierListener;
import jasima.shopSim.core.PR;
import jasima.shopSim.core.batchForming.HighestJobBatchingMBS;
import jasima.shopSim.models.dynamicShop.DynamicShopExperiment;
import jasima.shopSim.models.dynamicShop.DynamicShopExperiment.Scenario;
import jasima.shopSim.prioRules.basic.FASFS;
import jasima.shopSim.prioRules.basic.FCFS;
import jasima.shopSim.prioRules.meta.IgnoreFutureJobs;
import jasima.shopSim.util.BasicJobStatCollector;
import jasima.shopSim.util.BatchStatCollector;
import util.ExtendedJobStatCollector;

/**
 * 
 * @author Torsten Hildebrandt
 */
@SuppressWarnings("deprecation")
public class HolthausSimpleTest {

	@Before
	public void setUp() {
		System.setProperty(RandomFactory.RANDOM_FACTORY_PROP_KEY, RandomFactoryOld.class.getName());
		RandomFactory.reloadSysProps();
	}

	@After
	public void tearDown() {
		System.setProperty(RandomFactory.RANDOM_FACTORY_PROP_KEY, RandomFactory.class.getName());
		RandomFactory.reloadSysProps();
	}

	@Test
	public void check1() throws Exception {
		DynamicShopExperiment e = new DynamicShopExperiment();
		e.setInitialSeed(8346);

		// remove default BasicJobStatCollector
		NotifierListener[] l = e.getShopListener();
		assert l.length == 1 && l[0] instanceof BasicJobStatCollector;
		e.setShopListener(null);

		e.addShopListener(new ExtendedJobStatCollector());

		PR sr = new FCFS();
		PR sr2 = new IgnoreFutureJobs(sr);
		// PR<Object, Job> sr2 = new InversRule(new InversRule(sr));

		PR sr3 = new FASFS();
		// sr.setTieBreaker(sr2);
		sr2.setTieBreaker(sr3);
		e.setSequencingRule(sr2);
		e.setBatchForming(new HighestJobBatchingMBS(0.0));

		e.setNumMachines(10);
		e.setNumOps(10, 10);
		e.setDueDateFactor(new DblConst(4.0));
		e.setUtilLevel(0.95d);
		e.setStopArrivalsAfterNumJobs(500 + 20 * 2000);
		e.setScenario(Scenario.JOB_SHOP);

		BatchStatCollector batchStatCollector = new BatchStatCollector();
		batchStatCollector.setNumBatches(20);
		batchStatCollector.setBatchSize(2000);
		batchStatCollector.setIgnoreFirst(500);
		e.addShopListener(batchStatCollector);

		e.runExperiment();
		e.printResults();

		Map<String, Object> res = e.getResults();

		assertEquals("numJobsStarted", 40553, res.get("numJobsStarted"));
		assertEquals("numJobsFinished", 40553, res.get("numJobsFinished"));

		SummaryStat flow = (SummaryStat) res.get("flowMean");
		assertEquals("flowMean", 2643.4588, flow.mean(), 0.0001);
		assertEquals("flowMeanVar", 1064.3671, flow.stdDev(), 0.0001);

		SummaryStat tard = (SummaryStat) res.get("tardMean");
		assertEquals("tardMean", 1645.5588, tard.mean(), 0.0001);
		assertEquals("tardMeanVar", 1067.7316, tard.stdDev(), 0.0001);
	}

}
