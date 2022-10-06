
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

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jasima.core.random.RandomFactory;
import jasima.core.random.RandomFactoryOld;
import jasima.core.statistics.SummaryStat;
import jasima.core.util.ConsolePrinter;
import jasima.core.util.MsgCategory;
import jasima.core.util.observer.NotifierListener;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.batchForming.HighestJobBatchingMBS;
import jasima.shopSim.models.dynamicShop.DynamicShopExperiment;
import jasima.shopSim.prioRules.basic.TieBreakerFASFS;
import jasima.shopSim.prioRules.gp.GECCO2010_genSeed_2reps;
import jasima.shopSim.prioRules.gp.GECCO2010_lookahead;
import jasima.shopSim.prioRules.gp.GPRuleBase;
import jasima.shopSim.prioRules.meta.IgnoreFutureJobs;
import jasima.shopSim.prioRules.upDownStream.PTPlusWINQPlusNPT;
import jasima.shopSim.util.BasicJobStatCollector;
import util.ExtendedJobStatCollector;

/**
 * 
 * @author Torsten Hildebrandt
 */
@SuppressWarnings("deprecation")
public class TestGECCOContinuity {

	@Before
	public void setUp() {
		System.setProperty(RandomFactory.RANDOM_FACTORY_PROP_KEY, RandomFactoryOld.class.getName());
		RandomFactory.reloadSysProps();
	}

	@After
	public void tearDown() {
		System.setProperty(RandomFactory.RANDOM_FACTORY_PROP_KEY, RandomFactory.class.getName());
		RandomFactory.reloadSysProps();
	}

	private static final double GEN_SEED_FLOW_MEAN = 703.0405961677559d;
	private static final int GEN_SEED_NUM_JOBS = 10529;

	@Test
	public void Bremen_GECCO2010_genSeed_2reps() throws Exception {
		Map<String, Object> res = genSeed_2reps(false);

		assertEquals("numJobsFinished", GEN_SEED_NUM_JOBS, res.get("numJobsFinished"));
		assertEquals("numJobsStarted", GEN_SEED_NUM_JOBS, res.get("numJobsStarted"));

		SummaryStat flowMean = (SummaryStat) res.get("flowMean");
		assertEquals("numJobsStarted", GEN_SEED_FLOW_MEAN, flowMean.mean(), 0.00001d);
	}

	@Test
	public void Bremen_GECCO2010_genSeed_2reps_lookahead() throws Exception {
		Map<String, Object> res = genSeed_2reps(true);

		assertEquals("numJobsFinished", GEN_SEED_NUM_JOBS, res.get("numJobsFinished"));
		assertEquals("numJobsStarted", GEN_SEED_NUM_JOBS, res.get("numJobsStarted"));

		SummaryStat flowMean = (SummaryStat) res.get("flowMean");
		assertEquals("numJobsStarted", GEN_SEED_FLOW_MEAN, flowMean.mean(), 0.00001d);
	}

	@Test
	public void Bremen_GECCO2010_lookahead() throws Exception {
		Map<String, Object> res = GECCO2010_lookahead(false);

		assertEquals("numJobsFinished", 10529, res.get("numJobsFinished"));
		assertEquals("numJobsStarted", 10529, res.get("numJobsStarted"));

		SummaryStat flowMean = (SummaryStat) res.get("flowMean");
		assertEquals("numJobsStarted", 709.3618944257337d, flowMean.mean(), 0.00001d);
	}

	@Test
	public void Bremen_GECCO2010_lookahead_lookahead() throws Exception {
		Map<String, Object> res = GECCO2010_lookahead(true);

		assertEquals("numJobsFinished", 10528, res.get("numJobsFinished"));
		assertEquals("numJobsStarted", 10528, res.get("numJobsStarted"));

		SummaryStat flowMean = (SummaryStat) res.get("flowMean");
		assertEquals("numJobsStarted", 698.7863489439536d, flowMean.mean(), 0.00001d);
	}

	public static class TestRule extends GPRuleBase {

		private static final long serialVersionUID = 6302568915981723615L;

		@Override
		public double calcPrio(PrioRuleTarget j) {
			double p = j.currProcTime();
			double winq2 = jasima.shopSim.prioRules.upDownStream.XWINQ.xwinq(j);
			double tiq = j.getShop().simTime() - j.getArriveTime();
			double npt = PTPlusWINQPlusNPT.npt(j);
			double ol = j.numOpsLeft();
			double rpt = j.remainingProcTime();
			double tis = j.getShop().simTime() - j.getRelDate();
			return mul(
					max(npt, p), sub(
							mul(p, sub(
									ifte(div(
											add(div(mul(add(winq2, tis),
													winq2), 0), mul(mul(npt, rpt),
															rpt)),
											div(ifte(
													div(add(winq2, tis), ifte(
															div(add(winq2, tis),
																	ifte(max(sub(p, npt), sub(sub(0, p), winq2)), ol,
																			tis)),
															sub(winq2, ol),
															mul(tis, ifte(add(tiq, ol), mul(tis, winq2), tis)))),
													sub(mul(npt, rpt), ol),
													mul(tis, div(mul(add(winq2, tis), winq2), 0))),
													mul(mul(npt, rpt), winq2))),
											sub(div(mul(
													add(winq2, tis), winq2),
													add(ifte(add(tiq, sub(p, npt)), tis, npt), mul(npt, rpt))), p),
											ifte(ifte(
													ifte(sub(div(tis, add(sub(p, npt), mul(npt, rpt))), p),
															sub(div(mul(add(winq2, tis), winq2),
																	add(ifte(add(tiq, ol), tis, npt), mul(npt, rpt))),
																	p),
															add(winq2, tis)),
													div(mul(tis, winq2), add(add(winq2, tis), mul(npt, rpt))),
													add(tiq, ol)), sub(div(sub(p, npt), sub(winq2, ol)), p),
													div(mul(tis, winq2), add(tis, mul(npt, rpt))))),
									sub(winq2, ol))),
							add(tiq, ol)));
		}
	}

	private Map<String, Object> GECCO2010_lookahead(boolean lookahead) {
		DynamicShopExperiment e = new DynamicShopExperiment();

		// remove default BasicJobStatCollector
		NotifierListener[] l = e.getShopListener();
		assert l.length == 1 && l[0] instanceof BasicJobStatCollector;
		e.setShopListener(null);

		e.addShopListener(new ExtendedJobStatCollector());

		GECCO2010_lookahead pr = new GECCO2010_lookahead();
		pr.setTieBreaker(new TieBreakerFASFS());
		e.setSequencingRule(new IgnoreFutureJobs(pr));
		e.setBatchForming(new HighestJobBatchingMBS(0.0));
		e.setEnableLookAhead(lookahead);
		e.setInitialSeed(-1688276341376791082L);
		e.setStopArrivalsAfterNumJobs(500 + 5 * 2000);

		e.runExperiment();
		e.printResults();

		Map<String, Object> res = e.getResults();
		return res;
	}

	private Map<String, Object> genSeed_2reps(boolean lookahead) {
		DynamicShopExperiment e = new DynamicShopExperiment();

		// remove default BasicJobStatCollector
		NotifierListener[] l = e.getShopListener();
		assert l.length == 1 && l[0] instanceof BasicJobStatCollector;
		e.setShopListener(null);

		e.addShopListener(new ExtendedJobStatCollector());
//		e.addShopListener(new TraceFileProducer("traceNew.txt"));

		e.addListener(new ConsolePrinter(MsgCategory.DEBUG));

		GECCO2010_genSeed_2reps pr = new GECCO2010_genSeed_2reps();
		pr.setTieBreaker(new TieBreakerFASFS());

		e.setSequencingRule(new IgnoreFutureJobs(pr));
		e.setBatchForming(new HighestJobBatchingMBS(0.0));

		e.setEnableLookAhead(lookahead);
		e.setInitialSeed(-1688276341376791082L);
		e.setStopArrivalsAfterNumJobs(500 + 5 * 2000);

		e.runExperiment();
		e.printResults();

		Map<String, Object> res = e.getResults();
		return res;
	}

}
