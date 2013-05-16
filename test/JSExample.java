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
import jasima.core.statistics.SummaryStat;
import jasima.core.util.Util;
import jasima.shopSim.core.Job;
import jasima.shopSim.core.JobShop;
import jasima.shopSim.core.JobSource;
import jasima.shopSim.core.Operation;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.WorkStation;
import jasima.shopSim.util.MachineStatCollector;
import jasima.shopSim.util.WorkStationListenerBase;

import java.util.HashMap;
import java.util.Random;

/**
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version $Id$
 */
public class JSExample extends JobShop {
	final Random streamService = new Random(1234234535);
	final Random streamInterarrival = new Random(28437);
	final Random streamJobType = new Random(3424);

	float probDistribJobType[] = { 0.300f, 0.800f, 1.00f };

	float meanInterarrival = 0.25f;

	private static double LENGTH_SIM = 365.0;

	float meanService[][] = { { 0.50f, 0.60f, 0.85f, 0.50f },
			{ 1.10f, 0.80f, 0.75f }, { 1.20f, 0.25f, 0.70f, 0.90f, 1.00f } };

	// Number of tasks for each job type
	WorkStation route[][];// = { { 2, 0, 1, 4 }, { 3, 0, 2 }, { 1, 4, 0, 3, 2 }
							// };

	final int NUM_JOB_TYPES = meanService.length;

	SummaryStat[] jobTypeDelay;

	public static void main(String args[]) throws Exception {
		JSExample js = new JSExample();

		js.setSimulationLength(LENGTH_SIM);

		js.addMachine(new WorkStation(3));
		js.addMachine(new WorkStation(2));
		js.addMachine(new WorkStation(4));
		js.addMachine(new WorkStation(3));
		js.addMachine(new WorkStation(1));

		// int route[][] = { { 2, 0, 1, 4 }, { 3, 0, 2 }, { 1, 4, 0, 3, 2 } };
		js.route = new WorkStation[][] {
				{ js.machines[2], js.machines[0], js.machines[1],
						js.machines[4] },
				{ js.machines[3], js.machines[0], js.machines[2] },
				{ js.machines[1], js.machines[4], js.machines[0],
						js.machines[3], js.machines[2] } };

		js.init();
		js.run();
		js.done();
		js.report();
	} // End of main

	public JSExample() {
		super();
		setEnableLookAhead(false);
	}

	@Override
	public void init() {
		installMachineListener(new WorkStationListenerBase() {

			@Override
			protected void operationStarted(WorkStation m,
					PrioRuleTarget b, int oldSetupState,
					int newSetupState, double setupTime) {
				assert b.numJobsInBatch() == 1;
				Job job = b.job(0);

				jobTypeDelay[job.getJobType()].value(simTime() - job.getArriveTime());
			}}, false);
		installMachineListener(new MachineStatCollector(), true);
		
		addJobSource(new JobSource() {
			@Override
			public Job createNextJob() {
				Job job = new Job(JSExample.this);

				job.setRelDate(simTime()
						+ RandomHelper.expon(meanInterarrival,
								streamInterarrival));

				// Set JobType and TaskNumber of new job
				job.setJobType(RandomHelper.randomInteger(probDistribJobType,
						streamJobType));
				// job.setJobType(1);
				job.setTaskNumber(0);

				WorkStation[] ms = route[job.getJobType()];

				job.setOps(Util.initializedArray(ms.length, Operation.class));
				for (int i = 0; i < ms.length; i++) {
					job.getOps()[i].machine = ms[i];
				}

				for (int i = 0; i < ms.length; i++) {
					job.getOps()[i].procTime = RandomHelper.erlang(2,
							meanService[job.getJobType()][i], streamService);
				}

				return job;
			}
		});

		super.init();

		jobTypeDelay = new SummaryStat[NUM_JOB_TYPES];
		for (int i = 0; i < NUM_JOB_TYPES; i++) {
			jobTypeDelay[i] = new SummaryStat();
		}
	}

	public void addRecord(String s) {
		System.out.println(s);
	}

	public void report() {
		// Output begins here
		addRecord("Average total delay in queue");
		double oajtd = (double) 0.0; // Overall average job total delay
		double ajtd; // Average job total delay
		double sumProbs = (double) 0.0;
		for (int i = 0; i < NUM_JOB_TYPES; i++) {
			/*
			 * average job total delay = average delay for job type for each
			 * task times the number of tasks
			 */
			ajtd = (jobTypeDelay[i].mean() * route[i].length);
			addRecord(String.valueOf(ajtd));
			/*
			 * oajtd is a weighted average of the total time a job waits in
			 * queue. Total waits (ojtd) are multiplied by the probability job
			 * being of a particular type. Oajtd would be the typical total wait
			 */
			oajtd += (probDistribJobType[i] - sumProbs) * ajtd;
			sumProbs = probDistribJobType[i];
		}
		addRecord("Overall average job total delay: " + String.valueOf(oajtd));
		/*
		 * Compute the average number in queue, the average utilization, and the
		 * average delay in queue for each station
		 */
		addRecord("\n\nWork     Average Number     Average       Average Delay");
		addRecord("Station    in Queue        Utilization       in queue ");
		
		HashMap<String, Object> res = new HashMap<String,Object>();
		produceResults(res);
		for (int i = 0; i < machines.length; i++) {
			WorkStation m = machines[i];
			SummaryStat aniq = (SummaryStat) res.get( m.getName() + ".qLen" );
			SummaryStat aveMachinesBusy = (SummaryStat) res.get( m.getName() + ".util" );
			SummaryStat stationDelay = (SummaryStat) res.get( m.getName() + ".qWait" );
			addRecord(String.valueOf(i)
					+ "        "
					+ String.valueOf(aniq.mean())
					+ "        "
					+ String.valueOf(aveMachinesBusy.mean()
							/ machines[i].numInGroup()) + "        "
					+ String.valueOf(stationDelay.mean()));
		}
	}

}
