
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import jasima.core.random.continuous.DblConst;
import jasima.core.util.ExperimentTest;
import jasima.core.util.FileFormat;
import jasima.core.util.TraceFileProducer;
import jasima.core.util.XmlUtil;
import jasima.shopSim.core.DowntimeSource;
import jasima.shopSim.core.IndividualMachine;
import jasima.shopSim.core.WorkStation;
import jasima.shopSim.models.staticShop.StaticShopExperiment;
import jasima.shopSim.prioRules.basic.FASFS;
import jasima.shopSim.prioRules.basic.TieBreakerFASFS;
import jasima.shopSim.util.MachineStatCollector;
import util.ExtendedJobStatCollector;

/**
 * 
 * @author Torsten Hildebrandt
 */
@SuppressWarnings("deprecation")
public class TestDowntimes extends ExperimentTest {

	@Test
	@Ignore
	public void test_js02x05() throws Exception {
		Map<String, Object> res = new HashMap<String, Object>(test("testInstances/js02x05.txt", 5));
		res.remove("weightedCondTardMax");
		res.remove("weightedCondTardVariance");
		res.remove("weightedCondTardMean");
		res.remove("numTardy");
		res.remove("weightedNumTardy");

		@SuppressWarnings("unchecked")
		Map<String, Object> expected = (Map<String, Object>) XmlUtil.loadXML(FileFormat.XSTREAM,
				new File("testInstances/js02x05.txt.results"));

		checkKeySets(res, expected);
		checkResults(res, expected);
		checkFiles("log_js02x05.txt", "testInstances/js02x05.txt.trace");
	}

	@Test
	@Ignore
	public void test_js01x02() throws Exception {
		Map<String, Object> res = new HashMap<String, Object>(test("testInstances/js01x03.txt", 3));
		res.remove("weightedCondTardMax");
		res.remove("weightedCondTardVariance");
		res.remove("weightedCondTardMean");
		res.remove("numTardy");
		res.remove("weightedNumTardy");

		@SuppressWarnings("unchecked")
		Map<String, Object> expected = (Map<String, Object>) XmlUtil.loadXML(FileFormat.XSTREAM,
				new File("testInstances/js01x03.txt.results"));

		checkKeySets(res, expected);
		checkResults(res, expected);
		checkFiles("log_js01x03.txt", "testInstances/js01x03.txt.trace");
	}

	public static Map<String, Object> test(String fn, int n) throws Exception {
		@SuppressWarnings("serial")
		StaticShopExperiment e = new StaticShopExperiment() {
			@Override
			protected void configureShop() {
				super.configureShop();

				for (WorkStation m : shop.getMachines()) {
					for (IndividualMachine im : m.machDat()) {
						DowntimeSource dt = new DowntimeSource(im);
						dt.setTimeBetweenFailures(new DblConst(1.0));
						dt.setTimeToRepair(new DblConst(1.0));
						im.downsources = Arrays.asList(dt);
					}
				}
			}
		};
		e.setStopAfterNumJobs(n);
		e.setInstFileName(fn);
		e.setSequencingRule(new FASFS().setFinalTieBreaker(new TieBreakerFASFS()));
		e.addShopListener(new ExtendedJobStatCollector());
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
