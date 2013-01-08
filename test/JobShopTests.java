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
import static org.junit.Assert.assertTrue;
import jasima.core.statistics.SummaryStat;
import jasima.shopSim.core.WorkStation;

import java.util.HashMap;

import org.junit.Test;

/**
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version $Id$
 */
public class JobShopTests {

	@Test
	public void checkAssertionStatus() {
		boolean assertState = false;
		assert assertState = true;
		assertTrue("please enable assertions", assertState);
	}

	@Test
	public void check1() throws Exception {
		JSExample js = new JSExample();

		js.addMachine(new WorkStation(3));
		js.addMachine(new WorkStation(2));
		js.addMachine(new WorkStation(4));
		js.addMachine(new WorkStation(3));
		js.addMachine(new WorkStation(1));

		js.route = new WorkStation[][] {
				{ js.machines[2], js.machines[0], js.machines[1],
						js.machines[4] },
				{ js.machines[3], js.machines[0], js.machines[2] },
				{ js.machines[1], js.machines[4], js.machines[0],
						js.machines[3], js.machines[2] } };

		// js.setLengthSimulation(30);
		js.setSimulationLength(8 * 365.0f);

		js.init();
		js.run();
		js.done();

		js.report();

		double oajtd = 0f, sumProbs = 0f;

		for (int i = 0; i < js.NUM_JOB_TYPES; i++) {
			/*
			 * average job total delay = average delay for job type for each
			 * task times the number of tasks
			 */
			double ajtd = js.jobTypeDelay[i].mean() * js.route[i].length;
			assertEquals("avDelay", new double[] { 25.274267d, 17.918234d,
					40.184708d }[i], ajtd, 0.0001d);
			/*
			 * oajtd is a weighted average of the total time a job waits in
			 * queue. Total waits (ojtd) are multiplied by the probability job
			 * being of a particular type. Oajtd would be the typical total wait
			 */
			oajtd += (js.probDistribJobType[i] - sumProbs) * ajtd;
			sumProbs = js.probDistribJobType[i];
		}
		assertEquals("Overall average job total delay: ", 24.578339d, oajtd,
				0.0001d);

		System.out.println(js.jobsFinished);
		assertEquals("numFinished", 11551, js.jobsFinished);

		HashMap<String, Object> res = new HashMap<String, Object>();
		js.produceResults(res);

		for (int i = 0; i < js.machines.length; i++) {
			WorkStation m = js.machines[i];

			SummaryStat aniq = (SummaryStat) res.get(m.getName() + ".qLen");
			SummaryStat aveMachinesBusy = (SummaryStat) res.get(m.getName()
					+ ".util");
			SummaryStat stationDelay = (SummaryStat) res
					.get(m.getName() + ".qWait");

			assertEquals("Average Number in Queue", new double[] {
					10.640711097463264, 43.506917495137166, 0.6860489627173679,
					41.67933338913038, 1.8671606451165894 }[i], aniq.mean(),
					0.0001d);
			assertEquals("Average Utilization", new double[] {
					0.9507663051379164, 0.9906905798380076, 0.7214107612790233,
					0.967256788472735, 0.803024879706631 }[i],
					aveMachinesBusy.mean() / js.machines[i].numInGroup, 0.0001d);
			assertEquals("Average Delay in Queue",
					new double[] { 2.6724790935697262, 21.627010141269572,
							0.17260580485393026, 14.956587844458907,
							0.938571153953861 }[i], stationDelay.mean(),
					0.0001d);
		}
	}

	@Test
	public void check2() throws Exception {
		JSExample js = new JSExample();

		js.addMachine(new WorkStation(3));
		js.addMachine(new WorkStation(2));
		js.addMachine(new WorkStation(4));
		js.addMachine(new WorkStation(3));
		js.addMachine(new WorkStation(1));

		js.route = new WorkStation[][] {
				{ js.machines[2], js.machines[0], js.machines[1],
						js.machines[4] },
				{ js.machines[3], js.machines[0], js.machines[2] },
				{ js.machines[1], js.machines[4], js.machines[0],
						js.machines[3], js.machines[2] } };

		js.setSimulationLength(8 * 100 * 365f);

		js.init();
		js.run();
		js.done();

		js.report();

		System.out.println(js.jobsFinished);
		assertEquals("numFinished", 1168292, js.jobsFinished);

		HashMap<String, Object> res = new HashMap<String, Object>();
		js.produceResults(res);

		for (int i = 0; i < js.machines.length; i++) {
			WorkStation m = js.machines[i];

			SummaryStat aniq = (SummaryStat) res.get(m.getName() + ".qLen");
			SummaryStat aveMachinesBusy = (SummaryStat) res.get(m.getName()
					+ ".util");
			SummaryStat stationDelay = (SummaryStat) res
					.get(m.getName() + ".qWait");

			assertEquals("Average Number in Queue", new double[] {
					13.216440696313796, 77.25100792930898, 0.7498628369283316,
					22.870835950650495, 1.9520833472252603 }[i], aniq.mean(),
					0.00001d);
			assertEquals("Average Utilization", new double[] {
					0.958749123958056, 0.9911394388506134, 0.7248176313853936,
					0.9726069395363314, 0.7998228342590076 }[i],
					aveMachinesBusy.mean() / js.machines[i].numInGroup,
					0.00001d);
			assertEquals("Average Delay in Queue", new double[] {
					3.30305880395206, 38.59318497492146, 0.18740648456724288,
					8.1645576457121, 0.9752793709934672 }[i],
					stationDelay.mean(), 0.00001d);
		}
	}

}
