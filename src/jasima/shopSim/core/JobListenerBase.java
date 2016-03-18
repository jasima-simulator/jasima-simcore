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
package jasima.shopSim.core;

import static jasima.shopSim.core.Job.JOB_ARRIVED_IN_QUEUE;
import static jasima.shopSim.core.Job.JOB_END_OPERATION;
import static jasima.shopSim.core.Job.JOB_FINISHED;
import static jasima.shopSim.core.Job.JOB_RELEASED;
import static jasima.shopSim.core.Job.JOB_REMOVED_FROM_QUEUE;
import static jasima.shopSim.core.Job.JOB_START_OPERATION;

import jasima.core.util.observer.Subscriber;

/**
 * This class can be used as a base class for classes collecting results based
 * on job releases/job completions.
 * 
 * @author Torsten Hildebrandt
 */
public interface JobListenerBase extends Subscriber<Job, Object> {

	@Override
	default void inform(Job o, Object event) {
		final JobShop shop = o.getShop();
		if (event == JOB_RELEASED) {
			released(shop, o);
		} else if (event == JOB_FINISHED) {
			finished(shop, o);
		} else if (event == JOB_ARRIVED_IN_QUEUE) {
			arrivedInQueue(shop, o);
		} else if (event == JOB_REMOVED_FROM_QUEUE) {
			removedFromQueue(shop, o);
		} else if (event == JOB_START_OPERATION) {
			WorkStation m = o.getCurrMachine();
			operationStarted(shop, o, m.oldSetupState, m.newSetupState, m.setupTime);
		} else if (event == JOB_END_OPERATION) {
			endOperation(shop, o);
		} else {
			handleOther(shop, o, event);
		}
	}

	default void handleOther(JobShop shop, Job j, Object event) {
	}

	default void endOperation(JobShop shop, Job j) {
	}

	default void operationStarted(JobShop shop, Job j, int oldSetupState, int newSetupState, double setupTime) {
	}

	default void removedFromQueue(JobShop shop, Job j) {
	}

	default void arrivedInQueue(JobShop shop, Job j) {
	}

	default void finished(JobShop shop, Job j) {
	}

	default void released(JobShop shop, Job j) {
	}

}
