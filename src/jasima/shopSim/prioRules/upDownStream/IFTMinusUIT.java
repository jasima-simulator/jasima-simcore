/*******************************************************************************
 * This file is part of jasima, v1.3, the Java simulator for manufacturing and 
 * logistics.
 *  
 * Copyright (c) 2015 		jasima solutions UG
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package jasima.shopSim.prioRules.upDownStream;

import java.util.ArrayList;
import java.util.List;

import jasima.shopSim.core.Job;
import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.PriorityQueue;
import jasima.shopSim.core.WorkStation;

/**
 * This class implements a rule developed by Branke and Pickardt (2011) for job
 * shop problems, which attempts to measure the usable idle time on other work
 * centers in the prioritization of jobs.
 * <p>
 * The lookahead needs to be enabled in the simulation for this rule to work
 * properly.
 * 
 * @author Christoph Pickardt, 2011-11-15
 */
public class IFTMinusUIT extends PR {

	private static final long serialVersionUID = -8999022613610310632L;

	@Override
	public double calcPrio(PrioRuleTarget j) {
		double additionalFT = ift(j);
		return -(additionalFT - utilisedIdleTime(j));
	}

	public static double ift(PrioRuleTarget j) {
		WorkStation machine = j.getCurrMachine();
		double PT = j.currProcTime();

		double additionalFT = 0.0;
		for (int i = 0; i < machine.queue.size(); i++) {
			PrioRuleTarget j2 = machine.queue.get(i);
			if (!j2.isFuture()) {
				double PTDifferenz = PT - j2.currProcTime();
				if (PTDifferenz > 0)
					additionalFT += PTDifferenz;
			}
		}
		return additionalFT;
	}

	public static double utilisedIdleTime(PrioRuleTarget job) {
		double currPT = job.currProcTime();

		int nextTask = job.getTaskNumber() + 1;
		if (nextTask >= job.numOps())
			return 0;
		double nextPT = job.getOps()[nextTask].getProcTime();

		double winq = getExtendedWINQ(job);

		double earliestArrival = getEarliestArrivalAfter(job);

		if (winq < currPT) {
			if (currPT + nextPT <= earliestArrival)
				return nextPT;
			else
				return earliestArrival - currPT;
		} else if (winq < currPT + nextPT) {
			if (currPT + nextPT <= earliestArrival)
				return nextPT + (currPT - winq);
			else
				return earliestArrival - winq;
		} else {
			return 0;
		}
	}

	public static double getExtendedWINQ(PrioRuleTarget job) {
		int nextTask = job.getTaskNumber() + 1;
		assert (nextTask < job.numOps());
		WorkStation mNext = job.getOps()[nextTask].getMachine();

		double winq = mNext.workContent(false);

		List<PrioRuleTarget> additionalJobs = findLookAheadJobs(mNext, job.currProcTime() + 1);
		while (additionalJobs.size() > 0) {
			int index = getIndexOfNextJob(additionalJobs);
			PrioRuleTarget nextJob = additionalJobs.remove(index);
			double jobArrivingIn = nextJob.getCurrMachine().againIdleIn();
			if (jobArrivingIn <= winq)
				winq += nextJob.currProcTime();
			else
				winq = jobArrivingIn + nextJob.currProcTime();
		}
		return winq;
	}

	private static List<PrioRuleTarget> findLookAheadJobs(WorkStation m, double threshold) {
		threshold += m.shop().simTime();

		ArrayList<PrioRuleTarget> res = new ArrayList<PrioRuleTarget>();
		PriorityQueue<Job> q = m.queue;
		for (int i = 0, n = q.size(); i < n; i++) {
			Job j = q.get(i);
			if (j.isFuture() && j.getArriveTime() < threshold)
				res.add(j);
		}
		return res;
	}

	public static double getEarliestArrivalAfter(PrioRuleTarget job) {
		int nextTask = job.getTaskNumber() + 1;
		assert (nextTask < job.numOps());
		WorkStation mNext = job.getOps()[nextTask].getMachine();

		List<PrioRuleTarget> additionalJobs = findLookAheadJobs(mNext, Double.MAX_VALUE - job.getShop().simTime());
		double earliestArrival = Double.MAX_VALUE;
		for (int i = 0; i < additionalJobs.size(); i++) {
			PrioRuleTarget lookaheadJob = additionalJobs.get(i);
			double arrivingIn = lookaheadJob.getCurrMachine().againIdleIn();
			if (arrivingIn > job.currProcTime() && arrivingIn < earliestArrival)
				earliestArrival = arrivingIn;
		}
		return earliestArrival;
	}

	// earliest job?
	public static int getIndexOfNextJob(List<PrioRuleTarget> additionalJobs) {
		PrioRuleTarget nextJob = additionalJobs.get(0);
		int nextJobIndex = 0;
		for (int i = 1; i < additionalJobs.size(); i++) {
			PrioRuleTarget job = additionalJobs.get(i);
			if (job.getCurrMachine().againIdle() < nextJob.getCurrMachine().againIdle()) {
				nextJobIndex = i;
				nextJob = job;
			}
		}
		return nextJobIndex;
	}

}
