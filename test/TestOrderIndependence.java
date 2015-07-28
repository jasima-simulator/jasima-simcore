/*******************************************************************************
 * This file is part of jasima, v1.3, the Java simulator for manufacturing and 
 * logistics.
 *  
 * Copyright (c) 2015 		jasima solutions UG
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.models.staticShop.StaticShopExperiment;
import jasima.shopSim.prioRules.basic.SPT;

import java.io.File;

import org.junit.Test;

/**
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version 
 *          "$Id$"
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
			StaticShopExperiment m = TestStaticInsts
					.createTstModel(f, rules[i]);
			m.runExperiment();

			res[i] = (Double) m.getResults().get("simTime");
		}

		return res;
	}
}
