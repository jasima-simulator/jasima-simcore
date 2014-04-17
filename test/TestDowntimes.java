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
import jasima.core.random.continuous.DblConst;
import jasima.core.util.ExperimentTest;
import jasima.core.util.XmlUtil;
import jasima.shopSim.core.DowntimeSource;
import jasima.shopSim.core.IndividualMachine;
import jasima.shopSim.core.WorkStation;
import jasima.shopSim.models.staticShop.StaticShopExperiment;
import jasima.shopSim.prioRules.basic.FASFS;
import jasima.shopSim.prioRules.basic.TieBreakerFASFS;
import jasima.shopSim.util.BasicJobStatCollector;
import jasima.shopSim.util.MachineStatCollector;
import jasima.shopSim.util.TraceFileProducer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version "$Id$"
 */
public class TestDowntimes extends ExperimentTest {

	@Test
	public void test_js02x05() throws Exception {
		Map<String, Object> res = new HashMap<String, Object>(
				test("testInstances/js02x05.txt", 5));
		res.remove("weightedCondTardMax");
		res.remove("weightedCondTardVariance");
		res.remove("weightedCondTardMean");
		res.remove("numTardy");
		res.remove("weightedNumTardy");

		Map<String, Object> expected = (Map<String, Object>) XmlUtil
				.loadXML(new File("testInstances/js02x05.txt.results"));

		checkKeySets(res, expected);
		checkResults(res, expected);
		checkFiles("log_js02x05.txt", "testInstances/js02x05.txt.trace");
	}

	@Test
	public void test_js01x02() throws Exception {
		Map<String, Object> res = new HashMap<String, Object>(
				test("testInstances/js01x03.txt", 3));
		res.remove("weightedCondTardMax");
		res.remove("weightedCondTardVariance");
		res.remove("weightedCondTardMean");
		res.remove("numTardy");
		res.remove("weightedNumTardy");

		Map<String, Object> expected = (Map<String, Object>) XmlUtil
				.loadXML(new File("testInstances/js01x03.txt.results"));

		checkKeySets(res, expected);
		checkResults(res, expected);
		checkFiles("log_js01x03.txt", "testInstances/js01x03.txt.trace");
	}

	public static Map<String, Object> test(String fn, int n) throws Exception {
		StaticShopExperiment e = new StaticShopExperiment() {
			@Override
			protected void postConfigShop() {
				super.postConfigShop();
				for (WorkStation m : shop.machines) {
					for (IndividualMachine im : m.machDat()) {
						DowntimeSource dt = new DowntimeSource(im);
						dt.setTimeBetweenFailures(new DblConst(1.0));
						dt.setTimeToRepair(new DblConst(1.0));
						im.downsources = Arrays.asList(dt);
					}
				}
			}
		};
		e.setMaxJobsFinished(n);
		e.setInstFileName(fn);
		e.setSequencingRule(new FASFS()
				.setFinalTieBreaker(new TieBreakerFASFS()));
		e.addShopListener(new BasicJobStatCollector());
		e.addMachineListener(new MachineStatCollector());
		e.addShopListener(new TraceFileProducer("log_" + new File(fn).getName()));
		e.runExperiment();
		e.printResults();

		Map<String, Object> res = new HashMap<String, Object>(e.getResults());
		res.remove("condWeightedTardMax");
		res.remove("condWeightedTardVariance");
		res.remove("condWeightedTardMean");
		return res;
	}

	private static void checkFiles(String actual, String expected) {
		try {
			BufferedReader in1 = new BufferedReader(new FileReader(actual));
			BufferedReader in2 = new BufferedReader(new FileReader(expected));
			int line = 0;

			String s1 = in1.readLine();
			String s2 = in2.readLine();

			while (s1 != null && s2 != null) {
				assertEquals("line " + ++line, s2, s1);

				s1 = in1.readLine();
				s2 = in2.readLine();
			}

			assertEquals("file length differs.", s2, s1); // should both be null

			in1.close();
			in2.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
