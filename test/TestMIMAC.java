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
import jasima.core.random.continuous.DblDistribution;
import jasima.core.random.continuous.DblStream;
import jasima.core.random.continuous.DblUniformRange;
import jasima.core.random.discrete.IntUniformRange;
import jasima.core.statistics.SummaryStat;
import jasima.shopSim.core.batchForming.HighestJobBatchingMBS;
import jasima.shopSim.models.mimac.MimacExperiment;
import jasima.shopSim.models.mimac.MimacExperiment.DataSet;
import jasima.shopSim.prioRules.basic.TieBreakerFASFS;
import jasima.shopSim.prioRules.setup.ATCS;
import jasima.shopSim.util.ExtendedJobStatCollector;

import java.util.Map;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version $Id$
 */
public class TestMIMAC {

	@BeforeClass
	public static void setUp() {
		System.out.println("setting up");
		System.setProperty(RandomFactory.RANDOM_FACTORY_PROP_KEY,
				RandomFactoryOld.class.getName());
	}

	public MimacExperiment createExperiment() {
		MimacExperiment e = new MimacExperiment();
		e.setInitialSeed(-6437543093816807328l);
		e.setScenario(DataSet.FAB4r);
		DblStream arrivals1 = new DblDistribution(new ExponentialDistribution(
				1440d / 4.5));
		DblStream arrivals2 = new DblDistribution(new ExponentialDistribution(
				1440d / 10.5));
		e.setInterArrivalTimes(new DblStream[] { arrivals1, arrivals2 });
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

	private void check(String name, double expected, double precision,
			Map<String, Object> res) {
		Object o = res.get(name);
		double d = o instanceof SummaryStat ? ((SummaryStat) o).mean()
				: ((Number) o).doubleValue();
		assertEquals(name, expected, d, precision);
	}

}
