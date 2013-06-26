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
import static org.junit.Assert.assertTrue;
import jasima.shopSim.core.PR;
import jasima.shopSim.models.staticShop.StaticShopExperiment;
import jasima.shopSim.prioRules.basic.EDD;
import jasima.shopSim.prioRules.basic.FCFS;
import jasima.shopSim.prioRules.basic.ODD;
import jasima.shopSim.prioRules.basic.SPT;
import jasima.shopSim.prioRules.meta.IgnoreFutureJobs;
import jasima.shopSim.util.TraceFileProducer;

import java.io.File;

import org.junit.Test;

import util.FileChecker;

/**
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version $Id: TestStaticInsts.java 73 2013-01-08 17:16:19Z
 *          THildebrandt@gmail.com $
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

	public static void testFromFile(String fileName, PR[] rules)
			throws Exception {
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

	static PR[] rules = new PR[] { new IgnoreFutureJobs(new SPT()),
			new IgnoreFutureJobs(new FCFS()), new IgnoreFutureJobs(new EDD()),
			new IgnoreFutureJobs(new ODD()), };

}
