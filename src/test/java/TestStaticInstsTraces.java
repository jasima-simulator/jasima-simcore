
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
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import jasima.core.util.TraceFileProducer;
import jasima.shopSim.core.PR;
import jasima.shopSim.models.staticShop.StaticShopExperiment;
import jasima.shopSim.prioRules.basic.EDD;
import jasima.shopSim.prioRules.basic.FCFS;
import jasima.shopSim.prioRules.basic.ODD;
import jasima.shopSim.prioRules.basic.SPT;
import jasima.shopSim.prioRules.meta.IgnoreFutureJobs;
import util.FileChecker;

/**
 * 
 * @author Torsten Hildebrandt
 */
public class TestStaticInstsTraces {

	@Test
	public void test_js06x06_2() throws Exception {
		testFromFile("js06x06.txt", rules);
	}

	@Test
	public void test_js10x10_2() throws Exception {
		testFromFile("js10x10.txt", rules);
	}

	@Test
	public void test_js20x05_2() throws Exception {
		testFromFile("js20x05.txt", rules);
	}

	public static void testFromFile(String fileName, PR[] rules) throws Exception {
		File f = new File("testInstances/" + fileName);
		assertTrue("file not found: " + fileName, f.exists());

		for (PR sr : rules) {
			String traceName = "log_" + sr.toString() + "_" + fileName;
			StaticShopExperiment m = TestStaticInsts.createTstModel(f, sr);
			m.addShopListener(new TraceFileProducer(traceName));

			m.runExperiment();
			m.printResults();

			FileChecker.checkFiles(traceName, "testInstances/" + traceName);
			new File(traceName).delete();
		}
	}

	static PR[] rules = new PR[] { new IgnoreFutureJobs(new SPT()), new IgnoreFutureJobs(new FCFS()),
			new IgnoreFutureJobs(new EDD()), new IgnoreFutureJobs(new ODD()), };

}
