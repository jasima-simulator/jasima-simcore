package jasima.shopSim.util;

import static jasima.shopSim.core.Job.JOB_ARRIVED_IN_QUEUE;
import static jasima.shopSim.core.Job.JOB_END_OPERATION;
import static jasima.shopSim.core.Job.JOB_FINISHED;
import static jasima.shopSim.core.Job.JOB_RELEASED;
import static jasima.shopSim.core.Job.JOB_REMOVED_FROM_QUEUE;
import static jasima.shopSim.core.Job.JOB_START_OPERATION;
import jasima.core.util.observer.NotifierListener;
import jasima.shopSim.core.Job;
import jasima.shopSim.core.Job.JobEvent;
import jasima.shopSim.core.JobShop;
import jasima.shopSim.core.WorkStation;

/**
 * This class can be used as a base class for classes collecting results based
 * on job releases/job completions.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>, 2013-06-28
 * @version "$Id$"
 */
public class JobListenerBase implements Cloneable,
		NotifierListener<Job, JobEvent> {

	public JobListenerBase() {
		super();
	}

	@Override
	public final void update(Job j, JobEvent event) {
		final JobShop shop = j.getShop();
		if (event == JOB_RELEASED) {
			released(shop, j);
		} else if (event == JOB_FINISHED) {
			finished(shop, j);
		} else if (event == JOB_ARRIVED_IN_QUEUE) {
			arrivedInQueue(shop, j);
		} else if (event == JOB_REMOVED_FROM_QUEUE) {
			removedFromQueue(shop, j);
		} else if (event == JOB_START_OPERATION) {
			WorkStation m = j.getCurrMachine();
			operationStarted(shop, j, m.oldSetupState, m.newSetupState,
					m.setupTime);
		} else if (event == JOB_END_OPERATION) {
			endOperation(shop, j);
		} else {
			handleOther(shop, j, event);
		}
	}

	protected void handleOther(JobShop shop, Job j, JobEvent event) {
	}

	protected void endOperation(JobShop shop, Job j) {
	}

	protected void operationStarted(JobShop shop, Job j, int oldSetupState,
			int newSetupState, double setupTime) {
	}

	protected void removedFromQueue(JobShop shop, Job j) {
	}

	protected void arrivedInQueue(JobShop shop, Job j) {
	}

	protected void finished(JobShop shop, Job j) {
	}

	protected void released(JobShop shop, Job j) {
	}

	@Override
	public JobListenerBase clone() throws CloneNotSupportedException {
		return (JobListenerBase) super.clone();
	}

}
