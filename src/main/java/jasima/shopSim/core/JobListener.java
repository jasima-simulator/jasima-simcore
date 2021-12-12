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

import static jasima.shopSim.core.Job.JobMessage.JOB_ARRIVED_IN_QUEUE;
import static jasima.shopSim.core.Job.JobMessage.JOB_END_OPERATION;
import static jasima.shopSim.core.Job.JobMessage.JOB_FINISHED;
import static jasima.shopSim.core.Job.JobMessage.JOB_RELEASED;
import static jasima.shopSim.core.Job.JobMessage.JOB_REMOVED_FROM_QUEUE;
import static jasima.shopSim.core.Job.JobMessage.JOB_START_OPERATION;

import jasima.core.util.observer.NotifierListener;
import jasima.shopSim.core.Job.JobEvent;

/**
 * This class can be used as a base class for classes collecting results based
 * on job releases/job completions.
 * 
 * @author Torsten Hildebrandt
 */
public interface JobListener extends NotifierListener<Job, JobEvent> {

	@Override
	default void inform(Job o, JobEvent event) {
		final Shop shop = o.getShop();
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

	default void handleOther(Shop shop, Job j, Object event) {
	}

	@FunctionalInterface
	public interface OtherListener extends JobListener {
		@Override
		void handleOther(Shop shop, Job j, Object event);
	}

	default void endOperation(Shop shop, Job j) {
	}

	@FunctionalInterface
	public interface OperationEndedListener extends JobListener {
		@Override
		void endOperation(Shop shop, Job j);
	}

	default void operationStarted(Shop shop, Job j, int oldSetupState, int newSetupState, double setupTime) {
	}

	@FunctionalInterface
	public interface OperationStartedListener extends JobListener {
		@Override
		void operationStarted(Shop shop, Job j, int oldSetupState, int newSetupState, double setupTime);
	}

	default void removedFromQueue(Shop shop, Job j) {
	}

	@FunctionalInterface
	public interface RemovedListener extends JobListener {
		@Override
		void removedFromQueue(Shop shop, Job j);
	}

	default void arrivedInQueue(Shop shop, Job j) {
	}

	@FunctionalInterface
	public interface ArrivedListener extends JobListener {
		@Override
		void arrivedInQueue(Shop shop, Job j);
	}

	default void finished(Shop shop, Job j) {
	}

	@FunctionalInterface
	public interface FinishedListener extends JobListener {
		@Override
		void finished(Shop shop, Job j);
	}

	default void released(Shop shop, Job j) {
	}

	@FunctionalInterface
	public interface ReleasedListener extends JobListener {
		@Override
		void released(Shop shop, Job j);
	}

}
