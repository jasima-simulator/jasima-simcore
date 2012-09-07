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
package jasima.shopSim.util;

import jasima.core.statistics.TimeWeightedSummaryStat;
import jasima.core.statistics.SummaryStat;
import jasima.shopSim.core.IndividualMachine;
import jasima.shopSim.core.Job;
import jasima.shopSim.core.Operation;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.WorkStation;

import java.util.Map;

/**
 * Produces basic statistics for each workstation it is installed on (like
 * utilization, average queue length, average setup time per operation).
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version $Id$
 */
public class MachineStatCollector extends WorkStationListenerBase {

	/*
	 * Continuous statistic time average number of number of machines busy at
	 * each station and time average number in queue
	 */
	private TimeWeightedSummaryStat aveMachinesBusy;
	private TimeWeightedSummaryStat aniq;

	// Discrete statistics average delay at station
	private SummaryStat stationDelay;
	private SummaryStat capacityUtilized;
	private SummaryStat aveBatchSize;
	private SummaryStat setupTime;

	public MachineStatCollector() {
		super();
	}

	@Override
	protected void init(WorkStation m) {
		aveMachinesBusy = new TimeWeightedSummaryStat();
		aveMachinesBusy.value(m.numBusy, m.shop.simTime());
		aniq = new TimeWeightedSummaryStat();
		stationDelay = new SummaryStat();
		capacityUtilized = new SummaryStat();
		aveBatchSize = new SummaryStat();
		setupTime = new SummaryStat();
		aniq.clear();
		stationDelay.clear();
		capacityUtilized.clear();
		aveBatchSize.clear();
		setupTime.clear();
	}

	@Override
	protected void produceResults(WorkStation m, Map<String, Object> res) {
		res.put(m.getName() + ".qLen", aniq);
		res.put(m.getName() + ".util", aveMachinesBusy);
		res.put(m.getName() + ".capUtil", capacityUtilized);
		res.put(m.getName() + ".bSize", aveBatchSize);
		res.put(m.getName() + ".setup", setupTime);
		res.put(m.getName() + ".qWait", stationDelay);
	}

	@Override
	protected void done(WorkStation m) {
		// properly "close" time weighted stats
		aniq.value(Double.NaN, m.shop.simTime());
		aveMachinesBusy.value(Double.NaN, m.shop.simTime());
	}

	@Override
	protected void arrival(WorkStation m, Job j) {
		if (!j.isFuture()) {
			aniq.value(m.numJobsWaiting(), m.shop.simTime());
		}
	}

	@Override
	protected void activated(WorkStation m, IndividualMachine justActivated) {
		aveMachinesBusy.value(m.numBusy, m.shop.simTime());
	}

	@Override
	protected void operationStarted(WorkStation m, PrioRuleTarget jobOrBatch,
			int oldSetupState, int newSetupState, double setTime) {
		if (jobOrBatch == null)
			return;

		double simTime = m.shop.simTime();
		aveMachinesBusy.value(m.numBusy, simTime);

		// Record delay for station
		for (int i = 0; i < jobOrBatch.numJobsInBatch(); i++) {
			stationDelay.value(simTime - jobOrBatch.job(i).getArriveTime());
		}

		// Schedule a service completion for this batch at this
		// station.
		Operation op = jobOrBatch.getCurrentOperation();
		capacityUtilized.value((double) jobOrBatch.numJobsInBatch()
				/ op.maxBatchSize);
		aveBatchSize.value(jobOrBatch.numJobsInBatch());

		aniq.value(m.numJobsWaiting(), simTime);

		setupTime.value(setTime);
	}

	@Override
	protected void operationCompleted(WorkStation m,
			PrioRuleTarget justCompleted) {
		aveMachinesBusy.value(m.numBusy, m.shop.simTime());
	}

}
