
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
import java.util.HashMap;
import java.util.Random;

import org.junit.Ignore;

import jasima.core.simulation.Simulation;
import jasima.core.simulation.Simulation.ProduceResultsMessage;
import jasima.core.statistics.SummaryStat;
import jasima.core.util.ConsolePrinter;
import jasima.core.util.Util;
import jasima.shopSim.core.Job;
import jasima.shopSim.core.JobSource;
import jasima.shopSim.core.Operation;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.Shop;
import jasima.shopSim.core.WorkStation;
import jasima.shopSim.core.WorkStationListener;
import jasima.shopSim.util.MachineStatCollector;

/**
 * 
 * @author Torsten Hildebrandt
 */
@Ignore
public class JSExample extends Shop {
	final Random streamService = new Random(1234234535);
	final Random streamInterarrival = new Random(28437);
	final Random streamJobType = new Random(3424);

	float probDistribJobType[] = { 0.300f, 0.800f, 1.00f };

	float meanInterarrival = 0.25f;

	private static double LENGTH_SIM = 365.0;

	float meanService[][] = { { 0.50f, 0.60f, 0.85f, 0.50f }, { 1.10f, 0.80f, 0.75f },
			{ 1.20f, 0.25f, 0.70f, 0.90f, 1.00f } };

	// Number of tasks for each job type
	WorkStation route[][];// = { { 2, 0, 1, 4 }, { 3, 0, 2 }, { 1, 4, 0, 3, 2 }
							// };

	final int NUM_JOB_TYPES = meanService.length;

	SummaryStat[] jobTypeDelay;

	public static void main(String args[]) throws Exception {
		Simulation s = new Simulation();

		JSExample js = new JSExample();
		s.addComponent(js);

		s.setSimulationLength(LENGTH_SIM);

		js.addMachine(new WorkStation(3));
		js.addMachine(new WorkStation(2));
		js.addMachine(new WorkStation(4));
		js.addMachine(new WorkStation(3));
		js.addMachine(new WorkStation(1));

		// int route[][] = { { 2, 0, 1, 4 }, { 3, 0, 2 }, { 1, 4, 0, 3, 2 } };
		WorkStation[] ws = js.getMachines();
		js.route = new WorkStation[][] { { ws[2], ws[0], ws[1], ws[4] }, { ws[3], ws[0], ws[2] },
				{ ws[1], ws[4], ws[0], ws[3], ws[2] } };

		s.performRun();

		js.report();
	} // End of main

	public JSExample() {
		super();
		setEnableLookAhead(false);
	}

	@Override
	public void init() {
		installMachineListener(new WorkStationListener() {
			@Override
			public void operationStarted(WorkStation m, PrioRuleTarget b, int oldSetupState, int newSetupState,
					double setupTime) {
				assert b.numJobsInBatch() == 1;
				Job job = b.job(0);

				jobTypeDelay[job.getJobType()].value(simTime() - job.getArriveTime());
			}
		}, false);
		installMachineListener(new MachineStatCollector(), true);

		addJobSource(new JobSource() {
			@Override
			public Job createNextJob() {
				Job job = new Job(JSExample.this);

				job.setRelDate(simTime() + RandomHelper.expon(meanInterarrival, streamInterarrival));

				// Set JobType and TaskNumber of new job
				job.setJobType(RandomHelper.randomInteger(probDistribJobType, streamJobType));
				// job.setJobType(1);
				job.setTaskNumber(0);

				WorkStation[] ms = route[job.getJobType()];

				job.setOps(Util.initializedArray(ms.length, Operation.class));
				for (int i = 0; i < ms.length; i++) {
					job.getOps()[i].setMachine(ms[i]);
				}

				for (int i = 0; i < ms.length; i++) {
					job.getOps()[i]
							.setProcTime(RandomHelper.erlang(2, meanService[job.getJobType()][i], streamService));
				}

				return job;
			}
		});

		jobTypeDelay = Util.initializedArray(NUM_JOB_TYPES, SummaryStat.class);
	}

	@Override
	public void done() {
		super.done();
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
			 * average job total delay = average delay for job type for each task times the
			 * number of tasks
			 */
			ajtd = (jobTypeDelay[i].mean() * route[i].length);
			addRecord(String.valueOf(ajtd));
			/*
			 * oajtd is a weighted average of the total time a job waits in queue. Total
			 * waits (ojtd) are multiplied by the probability job being of a particular
			 * type. Oajtd would be the typical total wait
			 */
			oajtd += (probDistribJobType[i] - sumProbs) * ajtd;
			sumProbs = probDistribJobType[i];
		}
		addRecord("Overall average job total delay: " + String.valueOf(oajtd));
		/*
		 * Compute the average number in queue, the average utilization, and the average
		 * delay in queue for each station
		 */
		addRecord("\n\nWork     Average Number     Average       Average Delay");
		addRecord("Station    in Queue        Utilization       in queue ");

		HashMap<String, Object> res = new HashMap<String, Object>();
		produceResults(res);
		ConsolePrinter.printResults(res);
		getSim().fire(new ProduceResultsMessage(res));
		ConsolePrinter.printResults(res);

		int i = 0;
		for (WorkStation m : getMachines()) {
			SummaryStat aniq = (SummaryStat) res.get(m.getName() + ".qLen");
			SummaryStat aveMachinesBusy = (SummaryStat) res.get(m.getName() + ".util");
			SummaryStat stationDelay = (SummaryStat) res.get(m.getName() + ".qWait");

			addRecord(String.valueOf(i) + "        " + String.valueOf(aniq.mean()) + "        "
					+ String.valueOf(aveMachinesBusy.mean() / m.numInGroup()) + "        "
					+ String.valueOf(stationDelay.mean()));
			i++;
		}
	}

}
