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
package jasima.shopSim.util;

import java.util.Map;

import jasima.core.simulation.SimComponent;
import jasima.core.statistics.SummaryStat;
import jasima.core.statistics.TimeWeightedSummaryStat;
import jasima.shopSim.core.IndividualMachine;
import jasima.shopSim.core.Job;
import jasima.shopSim.core.Operation;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.WorkStation;
import jasima.shopSim.core.WorkStationListener;

/**
 * Produces basic statistics for each workstation it is installed on (like
 * utilization, average queue length, average setup time per operation).
 * 
 * @author Torsten Hildebrandt
 */
public class MachineStatCollector implements WorkStationListener, Cloneable {

	/*
	 * Continuous statistic time average number of number of machines busy at each
	 * station and time average number in queue
	 */
	public TimeWeightedSummaryStat aveMachinesBusy;
	public TimeWeightedSummaryStat aniq;

	// Discrete statistics average delay at station
	public SummaryStat stationDelay;
	public SummaryStat capacityUtilized;
	public SummaryStat aveBatchSize;
	public SummaryStat setupTime;
	public SummaryStat procTime;

	public MachineStatCollector() {
		super();
	}

	@Override
	public void init(SimComponent c) {
		WorkStation m = (WorkStation) c;

		aveMachinesBusy = new TimeWeightedSummaryStat().value(m.numBusy(), m.shop().simTime());
		aniq = new TimeWeightedSummaryStat().value(m.numJobsWaiting(), m.shop().simTime());
		stationDelay = new SummaryStat();
		capacityUtilized = new SummaryStat();
		aveBatchSize = new SummaryStat();
		setupTime = new SummaryStat();
		procTime = new SummaryStat();
	}

	@Override
	public void produceResults(SimComponent c, Map<String, Object> res) {
		WorkStation m = (WorkStation) c;

		res.put(m.getName() + ".qLen", aniq);
		res.put(m.getName() + ".util", aveMachinesBusy);
		res.put(m.getName() + ".capUtil", capacityUtilized);
		res.put(m.getName() + ".bSize", aveBatchSize);
		res.put(m.getName() + ".setup", setupTime);
		res.put(m.getName() + ".qWait", stationDelay);
	}

	@Override
	public void done(SimComponent c) {
		WorkStation m = (WorkStation) c;

		// properly "close" time weighted stats
		aniq.value(Double.NaN, m.shop().simTime());
		aveMachinesBusy.value(Double.NaN, m.shop().simTime());
	}

	@Override
	public void arrival(WorkStation m, Job j) {
		if (!j.isFuture()) {
			aniq.value(m.numJobsWaiting(), m.shop().simTime());
		}
	}

	@Override
	public void activated(WorkStation m, IndividualMachine justActivated) {
		aveMachinesBusy.value(m.numBusy(), m.shop().simTime());
	}

	@Override
	public void operationStarted(WorkStation m, PrioRuleTarget jobOrBatch, int oldSetupState, int newSetupState,
			double setTime) {
		if (jobOrBatch == null)
			return;

		double simTime = m.shop().simTime();
		aveMachinesBusy.value(m.numBusy(), simTime);

		// Record delay for station
		for (int i = 0; i < jobOrBatch.numJobsInBatch(); i++) {
			stationDelay.value(simTime - jobOrBatch.job(i).getArriveTime());
		}

		// Schedule a service completion for this batch at this
		// station.
		Operation op = jobOrBatch.getCurrentOperation();
		capacityUtilized.value((double) jobOrBatch.numJobsInBatch() / op.getMaxBatchSize());
		aveBatchSize.value(jobOrBatch.numJobsInBatch());

		aniq.value(m.numJobsWaiting(), simTime);

		setupTime.value(setTime);
		procTime.value(jobOrBatch.currProcTime());
	}

	@Override
	public void operationCompleted(WorkStation m, PrioRuleTarget justCompleted) {
		aveMachinesBusy.value(m.numBusy(), m.shop().simTime());
	}

	@Override
	public MachineStatCollector clone() {
		try {
			return (MachineStatCollector) super.clone();
		} catch (CloneNotSupportedException cantHappen) {
			throw new AssertionError(cantHappen);
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

}
