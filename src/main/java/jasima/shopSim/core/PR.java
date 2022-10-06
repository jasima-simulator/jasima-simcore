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
package jasima.shopSim.core;

import java.io.Serializable;

import jasima.shopSim.prioRules.meta.LookaheadThreshold;

/**
 * Abstract base class for a priority rule to be used to sequence items in a
 * {@code PriorityQueue}.
 * 
 * @author Torsten Hildebrandt
 * @see PriorityQueue
 */
public abstract class PR implements Cloneable, Serializable {

	private static final long serialVersionUID = -880043612686550471L;

	public static final double MIN_PRIO = PriorityQueue.MIN_PRIO;
	public static final double MAX_PRIO = PriorityQueue.MAX_PRIO;

	private WorkStation owner;

	private PR tieBreaker;
	private PR primaryRule;

	private LookaheadThreshold firstLookaheadRule = null;
	private boolean lookaheadRuleValid = false;

	public PR() {
		super();
	}

	/**
	 * This method is called upon start of a simulation to perform any
	 * initializations required.
	 */
	public void init() {
		if (getTieBreaker() != null)
			getTieBreaker().init();
	}

	/**
	 * This method is called by a queue before evaluating it's elements. Use it to
	 * do some initialization prior to calcPrio().
	 * 
	 * @param q The current queue.
	 */
	public void beforeCalc(PriorityQueue<? extends PrioRuleTarget> q) {
	}

	/**
	 * Returns the priority value of <code>entry</code>. This method has to be
	 * overwritten by a priority rule.
	 */
	public abstract double calcPrio(PrioRuleTarget entry);

	/**
	 * If this method returns true, the machine is kept idle. This method is called
	 * after beforeCalc(PriorityQueue) but before calcPrio(PrioRuleTarget).
	 */
	public boolean keepIdle() {
		return false;
	}

	@Override
	public final String toString() {
		String res = getName();
		if (getTieBreaker() != null)
			return res + "[" + getTieBreaker().toString() + "]";
		else
			return res;
	}

	public String getName() {
		String res = getClass().getSimpleName();
		if (res.isEmpty())
			res = getClass().getName();
		return res;
	}

	@Override
	public PR clone() {
		try {
			PR clone = (PR) super.clone();

			clone.firstLookaheadRule = null;
			clone.lookaheadRuleValid = false;

			if (getTieBreaker() != null) {
				clone.tieBreaker = null;
				clone.setTieBreaker(getTieBreaker().clone());
			}

			return clone;
		} catch (CloneNotSupportedException cantHappen) {
			throw new AssertionError(cantHappen);
		}
	}

	public WorkStation getOwner() {
		return owner;
	}

	public PR setOwner(WorkStation o) {
		owner = o;
		if (getTieBreaker() != null)
			getTieBreaker().setOwner(o);
		// returning "this" prevents "owner" from being recognized as a Bean
		// Property in the GUI
		return this;
	}

	/**
	 * Sets the tie breaker rule to use.
	 * 
	 * @param tieBreaker The tie-breaker to use.
	 */
	public void setTieBreaker(PR tieBreaker) {
		if (this.tieBreaker != null) {
			this.tieBreaker.primaryRule = null;
		}

		this.tieBreaker = tieBreaker;

		if (tieBreaker != null) {
			tieBreaker.primaryRule = this;
			tieBreaker.setOwner(getOwner());
		}
	}

	/**
	 * Convenience method to set the last tie breaker rule in a chain. The chain of
	 * rules is traversed until a rule without a tie breaker is found. The tie
	 * breaker of this rule is set to <code>tieBreaker</code>.
	 * 
	 * @param tieBreaker The tie-breaker to use.
	 * @return The main rule, i.e., <code>this</code>.
	 * @see #setTieBreaker(PR)
	 */
	public PR setFinalTieBreaker(PR tieBreaker) {
		PR pr = this;
		while (pr.getTieBreaker() != null)
			pr = pr.getTieBreaker();

		pr.setTieBreaker(tieBreaker);
		return this;
	}

	public PR getTieBreaker() {
		return tieBreaker;
	}

	/**
	 * If this rule is used as a tie-breaker for another rule, PrimaryRule points to
	 * the rule this rule is the tieBreaker for, i.e.
	 * <code>this.primaryRule().getTieBreaker()==this</code>.
	 */
	public PR primaryRule() {
		return primaryRule;
	}

	public boolean arrivesTooLate(PrioRuleTarget j) {
		if (!lookaheadRuleValid) {
			// find highest LookaheadThreshold
			firstLookaheadRule = null;
			PR own = primaryRule();
			while (own != null) {
				if (own instanceof LookaheadThreshold)
					firstLookaheadRule = (LookaheadThreshold) own;
				own = own.primaryRule();
			}
			lookaheadRuleValid = true;
		}

		if (firstLookaheadRule != null)
			return firstLookaheadRule.arrivesTooLate(j);
		else
			return false;
	}

}
