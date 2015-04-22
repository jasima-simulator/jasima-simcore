/*******************************************************************************
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This file is part of jasima, v1.2.
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
import static org.junit.Assert.assertEquals;
import jasima.core.random.RandomFactory;
import jasima.core.random.RandomFactoryOld;
import jasima.core.simulation.Simulation;
import jasima.core.simulation.Simulation.SimEvent;
import jasima.core.statistics.SummaryStat;
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

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import util.ExtendedJobStatCollector;

/**
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version $Id: TestGECCOContinuity.java 550 2015-01-23 15:07:23Z
 *          thildebrandt@gmail.com $
 */
@SuppressWarnings("deprecation")
public class TestGECCOContinuity {

	@Before
	public void setUp() {
		System.out.println("setting up");
		System.setProperty(RandomFactory.RANDOM_FACTORY_PROP_KEY,
				RandomFactoryOld.class.getName());
	}

	private static final double GEN_SEED_FLOW_MEAN = 703.0405961677559d;
	private static final int GEN_SEED_NUM_JOBS = 10529;

	@Test
	public void Bremen_GECCO2010_genSeed_2reps() throws Exception {
		Map<String, Object> res = genSeed_2reps(false);

		assertEquals("numJobsFinished", GEN_SEED_NUM_JOBS,
				res.get("numJobsFinished"));
		assertEquals("numJobsStarted", GEN_SEED_NUM_JOBS,
				res.get("numJobsStarted"));

		SummaryStat flowMean = (SummaryStat) res.get("flowMean");
		assertEquals("numJobsStarted", GEN_SEED_FLOW_MEAN, flowMean.mean(),
				0.00001d);
	}

	@Test
	public void Bremen_GECCO2010_genSeed_2reps_lookahead() throws Exception {
		Map<String, Object> res = genSeed_2reps(true);

		assertEquals("numJobsFinished", GEN_SEED_NUM_JOBS,
				res.get("numJobsFinished"));
		assertEquals("numJobsStarted", GEN_SEED_NUM_JOBS,
				res.get("numJobsStarted"));

		SummaryStat flowMean = (SummaryStat) res.get("flowMean");
		assertEquals("numJobsStarted", GEN_SEED_FLOW_MEAN, flowMean.mean(),
				0.00001d);
	}

	@Test
	public void Bremen_GECCO2010_lookahead() throws Exception {
		Map<String, Object> res = GECCO2010_lookahead(false);

		assertEquals("numJobsFinished", 10529, res.get("numJobsFinished"));
		assertEquals("numJobsStarted", 10529, res.get("numJobsStarted"));

		SummaryStat flowMean = (SummaryStat) res.get("flowMean");
		assertEquals("numJobsStarted", 709.3618944257337d, flowMean.mean(),
				0.00001d);
	}

	@Test
	public void Bremen_GECCO2010_lookahead_lookahead() throws Exception {
		Map<String, Object> res = GECCO2010_lookahead(true);

		assertEquals("numJobsFinished", 10528, res.get("numJobsFinished"));
		assertEquals("numJobsStarted", 10528, res.get("numJobsStarted"));

		SummaryStat flowMean = (SummaryStat) res.get("flowMean");
		assertEquals("numJobsStarted", 698.7863489439536d, flowMean.mean(),
				0.00001d);
	}

	public static class TestRule extends GPRuleBase {

		private static final long serialVersionUID = 6302568915981723615L;

		@Override
		public double calcPrio(PrioRuleTarget j) {
			double p = j.getCurrentOperation().procTime;
			double winq2 = jasima.shopSim.prioRules.upDownStream.XWINQ.xwinq(j);
			double tiq = j.getShop().simTime() - j.getArriveTime();
			double npt = PTPlusWINQPlusNPT.npt(j);
			double ol = j.numOpsLeft();
			double rpt = j.remainingProcTime();
			double tis = j.getShop().simTime() - j.getRelDate();
			return mul(
					max(npt, p),
					sub(mul(p,
							sub(ifte(
									div(add(div(mul(add(winq2, tis), winq2), 0),
											mul(mul(npt, rpt), rpt)),
											div(ifte(
													div(add(winq2, tis),
															ifte(div(
																	add(winq2,
																			tis),
																	ifte(max(
																			sub(p,
																					npt),
																			sub(sub(0,
																					p),
																					winq2)),
																			ol,
																			tis)),
																	sub(winq2,
																			ol),
																	mul(tis,
																			ifte(add(
																					tiq,
																					ol),
																					mul(tis,
																							winq2),
																					tis)))),
													sub(mul(npt, rpt), ol),
													mul(tis,
															div(mul(add(winq2,
																	tis), winq2),
																	0))),
													mul(mul(npt, rpt), winq2))),
									sub(div(mul(add(winq2, tis), winq2),
											add(ifte(add(tiq, sub(p, npt)),
													tis, npt), mul(npt, rpt))),
											p),
									ifte(ifte(
											ifte(sub(
													div(tis,
															add(sub(p, npt),
																	mul(npt,
																			rpt))),
													p),
													sub(div(mul(
															add(winq2, tis),
															winq2),
															add(ifte(
																	add(tiq, ol),
																	tis, npt),
																	mul(npt,
																			rpt))),
															p), add(winq2, tis)),
											div(mul(tis, winq2),
													add(add(winq2, tis),
															mul(npt, rpt))),
											add(tiq, ol)),
											sub(div(sub(p, npt), sub(winq2, ol)),
													p),
											div(mul(tis, winq2),
													add(tis, mul(npt, rpt))))),
									sub(winq2, ol))), add(tiq, ol)));
		}
	}

	private Map<String, Object> GECCO2010_lookahead(boolean lookahead) {
		DynamicShopExperiment e = new DynamicShopExperiment();

		// remove default BasicJobStatCollector
		NotifierListener<Simulation, SimEvent>[] l = e.getShopListener();
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
		NotifierListener<Simulation, SimEvent>[] l = e.getShopListener();
		assert l.length == 1 && l[0] instanceof BasicJobStatCollector;
		e.setShopListener(null);

		e.addShopListener(new ExtendedJobStatCollector());

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
