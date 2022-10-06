
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

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import jasima.core.random.RandomFactory;
import jasima.core.random.RandomFactoryOld;
import jasima.core.random.continuous.DblDistribution;
import jasima.core.random.continuous.DblSequence;
import jasima.core.random.continuous.DblUniformRange;
import jasima.core.random.discrete.IntUniformRange;
import jasima.core.statistics.SummaryStat;
import jasima.shopSim.core.batchForming.HighestJobBatchingMBS;
import jasima.shopSim.models.mimac.MimacExperiment;
import jasima.shopSim.models.mimac.MimacExperiment.DataSet;
import jasima.shopSim.prioRules.basic.TieBreakerFASFS;
import jasima.shopSim.prioRules.setup.ATCS;
import util.ExtendedJobStatCollector;

/**
 * 
 * @author Torsten Hildebrandt
 */
@SuppressWarnings("deprecation")
public class TestMIMAC {

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

	public MimacExperiment createExperiment() {
		MimacExperiment e = new MimacExperiment();
		e.setInitialSeed(-6437543093816807328l);
		e.setScenario(DataSet.FAB4r);
		DblSequence arrivals1 = new DblDistribution(new ExponentialDistribution(1440d / 4.5));
		DblSequence arrivals2 = new DblDistribution(new ExponentialDistribution(1440d / 10.5));
		e.setInterArrivalTimes(new DblSequence[] { arrivals1, arrivals2 });
		e.setDueDateFactors(new DblUniformRange(2.0, 5.0));
		e.setJobWeights(new IntUniformRange(1, 10));
		e.setSimulationLength(6 * 365 * 24 * 60);
		e.setMaxJobsInSystem(3 * 250);
		e.setEnableLookAhead(false);

		ExtendedJobStatCollector stats = new ExtendedJobStatCollector();
		stats.setInitialPeriod(365 * 24 * 60);
		e.addShopListener(stats);

		ATCS atcs = new ATCS(0.01, 0.5);
		atcs.setTieBreaker(new TieBreakerFASFS());
		e.setSequencingRule(atcs);

		e.setBatchForming(new HighestJobBatchingMBS(0.75));

		return e;
	}

	@Test
	public void testPrioQueueAssertion() throws Exception {
		MimacExperiment e = createExperiment();

		e.runExperiment();
		e.printResults();

		Map<String, Object> res = e.getResults();

		check("flowMean", 12499.1164, 0.0001, res);
		check("tardMean", 2192.6347, 0.0001, res);
		// check("tardPercentage", 0.6811, 0.0001, res);
		check("tardPercentage", 0.8148, 0.0001, res);
		check("weightedTardMean", 7031.1629, 0.0001, res);
		check("numJobsStarted", 32926, 0.0001, res);
		check("numJobsFinished", 32799, 0.0001, res);
		check("expAborted", 0.0, 0.0001, res);
	}

	private void check(String name, double expected, double precision, Map<String, Object> res) {
		Object o = res.get(name);
		double d = o instanceof SummaryStat ? ((SummaryStat) o).mean() : ((Number) o).doubleValue();
		assertEquals(name, expected, d, precision);
	}

}
