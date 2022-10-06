
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
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.models.staticShop.StaticShopExperiment;
import jasima.shopSim.prioRules.basic.SPT;

/**
 * 
 * @author Torsten Hildebrandt
 */
public class TestOrderIndependence {

	@Test
	public void test20x05() throws Exception {
		double[] res = test("testInstances/lookaheadTest.txt");

		assertEquals("reference value", res[0], 20.0, 0.000001d);
		assertEquals("without/with lookahead", res[0], res[1], 0.000001d);
	}

	@SuppressWarnings("serial")
	static PR[] rules = new PR[] { new SPT(), new PR() {

		@Override
		public double calcPrio(PrioRuleTarget j) {
			return -j.currProcTime();
		}
	} };

	public double[] test(String fn) throws Exception {
		File f = new File(fn);
		assertTrue("file not found: " + fn, f.exists());

		double[] res = new double[rules.length];
		for (int i = 0; i < rules.length; i++) {
			StaticShopExperiment m = TestStaticInsts.createTstModel(f, rules[i]);
			m.runExperiment();

			res[i] = (Double) m.getResults().get("simTime");
		}

		return res;
	}
}
