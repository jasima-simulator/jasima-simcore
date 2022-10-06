
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
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;

import jasima.core.statistics.SummaryStat;
import jasima.core.util.IoUtil;
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
import jasima.shopSim.prioRules.meta.IgnoreFutureJobs;
import jasima.shopSim.prioRules.meta.Inverse;
import jasima.shopSim.prioRules.upDownStream.IFTMinusUITPlusNPT;
import jasima.shopSim.prioRules.upDownStream.PTPlusWINQPlusNPT;
import jasima.shopSim.prioRules.upDownStream.WINQ;
import jasima.shopSim.prioRules.upDownStream.XWINQ;
import jasima.shopSim.prioRules.weighted.WSPT;
import util.Bremen1;
import util.Bremen2;
import util.ExtendedJobStatCollector;

/**
 * 
 * @author Torsten Hildebrandt
 */
@SuppressWarnings("deprecation")
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
		String[] exp = IoUtil.lines(fileName + ".expected");
		assertArrayEquals("simulation results", exp, res);
	}

	static PR[] rules = new PR[] { new IgnoreFutureJobs(new SPT()), new IgnoreFutureJobs(new FCFS()),
			new IgnoreFutureJobs(new PTPlusWINQPlusNPT()), new IgnoreFutureJobs(new WINQ()),
			new IgnoreFutureJobs(new Inverse(new FCFS())), new IgnoreFutureJobs(new Inverse(new SPT())),
			new IgnoreFutureJobs(new WSPT()), new IgnoreFutureJobs(new Inverse(new WSPT())),
			new IgnoreFutureJobs(new EDD()), new IgnoreFutureJobs(new FASFS()), new IgnoreFutureJobs(new SLK()),
			new IgnoreFutureJobs(new Inverse(new CR())), new IgnoreFutureJobs(new Inverse(new CR.Variant1())),
			new IgnoreFutureJobs(new Inverse(new CR.Variant2())), new IgnoreFutureJobs(new ATC(1.0)), new Bremen1(),
			new Bremen2(), new IgnoreFutureJobs(new IFTMinusUITPlusNPT()), new XWINQ() };

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

	public static StaticShopExperiment createTstModel(File f, PR sr) throws FileNotFoundException, IOException {
		StaticShopExperiment shopExperiment = new StaticShopExperiment();
		shopExperiment.setInstFileName(f.toString());
		shopExperiment.setEnableLookAhead(true);
		shopExperiment
				.setSequencingRule(sr.clone().setFinalTieBreaker(new FCFS()).setFinalTieBreaker(new TieBreakerFASFS()));
		shopExperiment.addShopListener(new ExtendedJobStatCollector());

		return shopExperiment;
	}

	// get some selected results
	public static void getResults(Map<String, Object> expRes, String prefix, ArrayList<String> res) {
		res.add(prefix + "\tsimtime\t" + expRes.get("simTime"));
		res.add(prefix + "\tcMax\t" + expRes.get("cMax"));
		res.add(formatValueStat(prefix, "flowtimes", (SummaryStat) expRes.get("flowMean")));
		res.add(formatValueStat(prefix, "tardiness", (SummaryStat) expRes.get("tardMean")));
		res.add(formatValueStat(prefix, "lateness", (SummaryStat) expRes.get("lateMean")));
	}

	private static String formatValueStat(String prefix, String name, SummaryStat stat) {
		return String.format(Locale.ENGLISH, "%s\t%s\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%d", prefix, name, stat.mean(),
				stat.min(), stat.max(), stat.stdDev(), stat.sum(), stat.numObs());
	}

}
