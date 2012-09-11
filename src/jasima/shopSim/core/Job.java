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
package jasima.shopSim.core;

/**
 * Main work unit in a shop.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version $Id$
 */
public class Job extends PrioRuleTarget implements Cloneable {

	private final JobShop shop;
	private double arriveTime; // arrival time at current machine
	private WorkStation currMachine;
	// when will job finish processing on its current machine (if started)
	private double finishTime;
	private int taskNumber; // current operation
	private double relDate;
	private double dueDate;
	private int jobNum; // global number of job in system
	private JobSource source; // the job source releasing this job
	private int sourceNum; // number of job by a specific JobSource
	private double weight = 1.0d;
	private Operation[] ops;
	private int jobType;
	private double[] opDueDates;
	private boolean isFuture = false;
	private String name = null;
	// we cache the value returned by remainingProcTime()
	private double remProcTime = -1.0d;

	private Job future;

	public Job(JobShop shop) {
		super();
		this.shop = shop;
	}

	public void setArriveTime(double fl) {
		arriveTime = fl;
	}

	public double getArriveTime() {
		return arriveTime;
	}

	public void setJobType(int job) {
		jobType = job;
	}

	public int getJobType() {
		return jobType;
	}

	public void setTaskNumber(int tn) {
		remProcTime = -1.0d;
		taskNumber = tn;
	}

	public int getTaskNumber() {
		return taskNumber;
	}

	public void setCurrMachine(WorkStation currMachine) {
		this.currMachine = currMachine;
	}

	public WorkStation getCurrMachine() {
		return currMachine;
	}

	public Operation getCurrentOperation() {
		return ops[taskNumber];
	}

	public double currProcTime() {
		return ops[taskNumber].procTime;
	}

	public double procSum() {
		double res = 0d;
		for (Operation o : ops) {
			res += o.procTime;
		}
		return res;
	}

	public double remainingProcTime() {
		if (remProcTime < 0.0d) {
			remProcTime = 0f;
			Operation[] ops = this.ops;
			for (int i = taskNumber; i < ops.length; i++) {
				remProcTime += ops[i].procTime;
			}
		}
		return remProcTime;
	}

	public int numOps() {
		return getOps().length;
	}

	public int numOpsLeft() {
		return getOps().length - getTaskNumber();
	}

	/**
	 * If the this job has one or more tasks yet to be done, send the job to the
	 * next machine on its route
	 */
	@Override
	public void proceed() {
		if (!isLastOperation()) {
			setTaskNumber(getTaskNumber() + 1);

			WorkStation mNext = ops[taskNumber].machine;
			mNext.enqueueOrProcess(this);
		} else {
			shop.jobFinished(this);
		}
	}

	/**
	 * Notify next machine of future arrival. This mehod is called whenever an
	 * operation is started. This method assumes isFinished to be set to the
	 * correct value before this method is called.
	 */
	public void notifyNextMachine() {
		if (!isLastOperation() && shop.isEnableLookAhead()) {
			final Job f = getMyFuture();
			final WorkStation next = f.ops[f.taskNumber].machine;
			next.futureArrival(f, getFinishTime());
		}
	}

	/** returns a clone of this Job switched to the next operation. */
	public Job getMyFuture() {
		if (future == null) {
			future = silentClone();
			future.setFuture(true);
		}
		future.setTaskNumber(taskNumber + 1);
		return future;
	}

	@Override
	public Job clone() throws CloneNotSupportedException {
		return (Job) super.clone();
	}

	public Job silentClone() {
		try {
			return clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	void setFuture(boolean isFuture) {
		this.isFuture = isFuture;
	}

	public boolean isFuture() {
		return isFuture;
	}

	public boolean isLastOperation() {
		return taskNumber == ops.length - 1;
	}

	@Override
	public String toString() {
		String s = getName();
		if (s == null)
			s = getClass().getSimpleName() + "." + jobType + "." + getJobNum();
		return s + "#" + taskNumber;
	}

	public JobShop getShop() {
		return shop;
	}

	public double getDueDate() {
		return dueDate;
	}

	public void setDueDate(double dueDate) {
		this.dueDate = dueDate;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public Job getFuture() {
		return future;
	}

	public void setFuture(Job future) {
		this.future = future;
	}

	public void setRelDate(double relDate) {
		this.relDate = relDate;
	}

	public double getRelDate() {
		return relDate;
	}

	public void setJobNum(int jobNum) {
		this.jobNum = jobNum;
	}

	@Override
	public int getJobNum() {
		return jobNum;
	}

	public void setSource(JobSource source) {
		this.source = source;
	}

	public JobSource getSource() {
		return source;
	}

	public void setSourceNum(int sourceNum) {
		this.sourceNum = sourceNum;
	}

	public int getSourceNum() {
		return sourceNum;
	}

	public void setFinishTime(double finishTime) {
		this.finishTime = finishTime;
	}

	public double getFinishTime() {
		return finishTime;
	}

	public void setOpDueDates(double[] opDueDates) {
		this.opDueDates = opDueDates;
	}

	public double[] getOpDueDates() {
		return opDueDates;
	}

	@Override
	public double getCurrentOperationDueDate() {
		if (opDueDates == null) {
			setOpDueDates(computeDueDatesTWC(this, (dueDate - relDate)
					/ procSum()));
		}

		return opDueDates[taskNumber];
	}

	public void setOps(Operation[] ops) {
		this.ops = ops;
		this.opDueDates = null;
	}

	public Operation[] getOps() {
		return ops;
	}

	@Override
	public Job job(int i) {
		if (i != 0)
			throw new IllegalArgumentException("" + i);
		return this;
	}

	@Override
	public int numJobsInBatch() {
		return 1;
	}

	// compute operational due dates based on the total work content method,
	// i.e., proportional to an operation's processing time
	public static double[] computeDueDatesTWC(Job j, double ff) {
		Operation[] ops = j.ops;

		double[] res = new double[ops.length];

		double due = j.getRelDate();

		for (int i = 0; i < res.length; i++) {
			due += ff * ops[i].procTime;
			res[i] = due;
		}

		return res;
	}

	@Override
	public boolean isBatch() {
		return false;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
