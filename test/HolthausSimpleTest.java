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
import jasima.core.random.RandomFactory;
import jasima.core.random.RandomFactoryOld;
import jasima.core.statistics.SummaryStat;
import jasima.shopSim.core.PR;
import jasima.shopSim.core.batchForming.HighestJobBatchingMBS;
import jasima.shopSim.models.holthaus.HolthausExperiment;
import jasima.shopSim.models.holthaus.HolthausExperiment.Scenario;
import jasima.shopSim.prioRules.basic.FASFS;
import jasima.shopSim.prioRules.basic.FCFS;
import jasima.shopSim.prioRules.meta.IgnoreFutureJobs;
import jasima.shopSim.util.BasicJobStatCollector;
import jasima.shopSim.util.BatchStatCollector;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version $Id$
 */
public class HolthausSimpleTest {

	@Before
	public void setUp() {
		System.out.println("setting up");
		System.setProperty(RandomFactory.RANDOM_FACTORY_PROP_KEY,
				RandomFactoryOld.class.getName());
	}

	@Test
	public void check1() throws Exception {
		HolthausExperiment e = new HolthausExperiment();
		e.setInitialSeed(8346);
		e.addShopListener(new BasicJobStatCollector());

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
		e.setDueDateFactor(4.0);
		e.setUtilLevel(0.95d);
		e.setStopAfterNumJobs(500+20*2000);
		e.setScenario(Scenario.JOB_SHOP);

		BatchStatCollector batchStatCollector = new BatchStatCollector();
		batchStatCollector.setNumBatches(20);
		batchStatCollector.setBatchSize(2000);
		batchStatCollector.setIgnoreFirst(500);
		e.addShopListener(batchStatCollector);

		e.runExperiment();
		e.printResults();

		Map<String, Object> res = e.getResults();

		assertEquals("numJobsFinished", 40553, res.get("numJobsFinished"));
		assertEquals("numJobsStarted", 40553, res.get("numJobsStarted"));

		SummaryStat flow = (SummaryStat) res.get("flowMean");
		assertEquals("flowMean", 2643.4588, flow.mean(), 0.0001);
		assertEquals("flowMeanVar", 1064.3671, flow.stdDev(), 0.0001);

		SummaryStat tard = (SummaryStat) res.get("tardMean");
		assertEquals("tardMean", 1645.5588, tard.mean(), 0.0001);
		assertEquals("tardMeanVar", 1067.7316, tard.stdDev(), 0.0001);
	}

}
