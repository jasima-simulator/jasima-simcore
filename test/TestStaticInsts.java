/*******************************************************************************
 * Copyright 2011, 2012 Torsten Hildebrandt and BIBA - Bremer Institut für Produktion und Logistik GmbH
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
 *******************************************************************************/
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import jasima.core.statistics.SummaryStat;
import jasima.core.util.Util;
import jasima.shopSim.core.PR;
import jasima.shopSim.models.staticShop.StaticShopExperiment;
import jasima.shopSim.prioRules.basic.ATC;
import jasima.shopSim.prioRules.basic.CR;
import jasima.shopSim.prioRules.basic.EDD;
import jasima.shopSim.prioRules.basic.FASFS;
import jasima.shopSim.prioRules.basic.FCFS;
import jasima.shopSim.prioRules.basic.SLK;
import jasima.shopSim.prioRules.basic.SPT;
import jasima.shopSim.prioRules.basic.TieBreakerFASFS;
import jasima.shopSim.prioRules.gp.Bremen1;
import jasima.shopSim.prioRules.gp.Bremen2;
import jasima.shopSim.prioRules.meta.IgnoreFutureJobs;
import jasima.shopSim.prioRules.meta.Inverse;
import jasima.shopSim.prioRules.upDownStream.IFTMinusUITPlusNPT;
import jasima.shopSim.prioRules.upDownStream.PTPlusWINQPlusNPT;
import jasima.shopSim.prioRules.upDownStream.WINQ;
import jasima.shopSim.prioRules.upDownStream.XWINQ;
import jasima.shopSim.prioRules.weighted.WSPT;
import jasima.shopSim.util.BasicJobStatCollector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;

/**
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version $Id$
 */
public class TestStaticInsts {

	@Test
	public void test_js06x06_2() throws Exception {
		testFromFile("testInstances/js06x06.txt", rules);
	}

	@Test
	public void test_js10x10_2() throws Exception {
		testFromFile("testInstances/js10x10.txt", rules);
	}

	@Test
	public void test_js20x05_2() throws Exception {
		testFromFile("testInstances/js20x05.txt", rules);
	}

	public static void testFromFile(String fileName, PR[] rules) throws Exception {
		String[] res = test(fileName, rules);
		System.out.println(Arrays.toString(res));
		String[] exp = Util.lines(new File(fileName + ".expected"));
		assertArrayEquals("simulation results", exp, res);
	}

	static PR[] rules = new PR[] { new IgnoreFutureJobs(new SPT()),
			new IgnoreFutureJobs(new FCFS()),
			new IgnoreFutureJobs(new PTPlusWINQPlusNPT()),
			new IgnoreFutureJobs(new WINQ()),
			new IgnoreFutureJobs(new Inverse(new FCFS())),
			new IgnoreFutureJobs(new Inverse(new SPT())),
			new IgnoreFutureJobs(new WSPT()),
			new IgnoreFutureJobs(new Inverse(new WSPT())),
			new IgnoreFutureJobs(new EDD()), new IgnoreFutureJobs(new FASFS()),
			new IgnoreFutureJobs(new SLK()),
			new IgnoreFutureJobs(new Inverse(new CR())),
			new IgnoreFutureJobs(new Inverse(new CR.Variant1())),
			new IgnoreFutureJobs(new Inverse(new CR.Variant2())),
			new IgnoreFutureJobs(new ATC(1.0)), new Bremen1(), new Bremen2(),
			new IgnoreFutureJobs(new IFTMinusUITPlusNPT()), new XWINQ() };

	public static String[] test(String fn, PR[] rules) throws Exception {
		ArrayList<String> res = new ArrayList<String>();
		File f = new File(fn);
		assertTrue("file not found: " + fn, f.exists());

		for (PR sr : rules) {
			StaticShopExperiment m = createTstModel(f, sr);
			m.runExperiment();
			getResults(m.getResults(), fn + " " + sr.toString(), res);
		}

		return res.toArray(new String[res.size()]);
	}

	public static StaticShopExperiment createTstModel(File f, PR sr)
			throws FileNotFoundException, IOException {
		StaticShopExperiment shopExperiment = new StaticShopExperiment();
		shopExperiment.setInstFileName(f.toString());
		shopExperiment.setEnableLookAhead(true);
		shopExperiment.setSequencingRule(sr.silentClone()
				.setFinalTieBreaker(new FCFS())
				.setFinalTieBreaker(new TieBreakerFASFS()));
		shopExperiment.addShopListener(new BasicJobStatCollector());

		return shopExperiment;
	}

	// get some selected results
	public static void getResults(Map<String, Object> expRes, String prefix,
			ArrayList<String> res) {
		res.add(prefix + "\tsimtime\t" + expRes.get("simTime"));
		res.add(prefix + "\tcMax\t" + expRes.get("cMax"));
		res.add(formatValueStat(prefix, (SummaryStat) expRes.get("flowMean")));
		res.add(formatValueStat(prefix, (SummaryStat) expRes.get("tardMean")));
		res.add(formatValueStat(prefix, (SummaryStat) expRes.get("lateMean")));
	}

	private static String formatValueStat(String prefix, SummaryStat stat) {
		return String.format(Locale.ENGLISH,
				"%s\t%s\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%d", prefix,
				stat.getName(), stat.mean(), stat.min(), stat.max(),
				stat.stdDev(), stat.sum(), stat.numObs());
	}

}
