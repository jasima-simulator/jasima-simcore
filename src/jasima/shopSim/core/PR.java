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

import jasima.shopSim.prioRules.meta.LookaheadThreshold;

import java.io.Serializable;

/**
 * Abstract base class for a priority rule to be used to sequence items in a
 * PriorityQueue.
 * 
 * @author Torsten Hildebrandt
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

	/**
	 * This method is called upon start of a simulation to perform any
	 * initializations required.
	 */
	public void init() {
	}

	/**
	 * This method is called by a queue before evaluating it's elements. Use it
	 * to do some initialization prior to calcPrio(). This method is only called
	 * if this is not a static rule.
	 * 
	 * @param q
	 */
	public void beforeCalc(PriorityQueue<?> q) {
	}

	/**
	 * Returns the priority value of <code>entry</code>. This method has to be
	 * overwritten by a priority rule.
	 */
	public abstract double calcPrio(PrioRuleTarget entry);

	/**
	 * If this method returns true, the machine is kept idle. This method is
	 * called after beforeCalc(PriorityQueue) but before
	 * calcPrio(PrioRuleTarget).
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
		return getClass().getSimpleName();
	}

	@Override
	public PR clone() throws CloneNotSupportedException {
		PR clone = (PR) super.clone();

		clone.firstLookaheadRule = null;
		clone.lookaheadRuleValid = false;

		if (getTieBreaker() != null) {
			clone.tieBreaker = null;
			clone.setTieBreaker(getTieBreaker().clone());
		}

		return clone;
	}

	/**
	 * This method simply calls {@link #clone()}, but hides the checked
	 * exception {@link CloneNotSupportedException}.
	 */
	public PR silentClone() {
		try {
			return clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public WorkStation getOwner() {
		return owner;
	}

	public void setOwner(WorkStation o) {
		owner = o;
		if (getTieBreaker() != null)
			getTieBreaker().setOwner(o);
	}

	/**
	 * Calls {@link #setTieBreaker(PR)} and returns the main rule (
	 * <code>this</code>) to allow easier chaining of multiple rules.
	 * 
	 * @param tieBreaker
	 *            The tie-breaker to use.
	 * @return The main rule, i.e., <code>this</code>.
	 */
	public PR tieBreaker(PR tieBreaker) {
		tieBreaker(tieBreaker);

		return this;
	}

	/**
	 * Sets the tie breaker rule to use.
	 * 
	 * @param tieBreaker
	 *            The tie-breaker to use.
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
	 * Convenience method to set the last tie breaker rule in a chain. The chain
	 * of rules is traversed until a rule without a tie breaker is found. The
	 * tie breaker of this rule is set to <code>tieBreaker</code>.
	 * 
	 * @param tieBreaker
	 *            The tie-breaker to use.
	 * @return The main rule, i.e., <code>this</code>.
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
	 * If this rule is used as a tie-breaker for another rule, PrimaryRule
	 * points to the rule this rule is the tieBreaker for, i.e.
	 * <code>this.getPrimaryRule().getTieBreaker()==this</code>.
	 */
	public PR getPrimaryRule() {
		return primaryRule;
	}

	public boolean arrivesTooLate(PrioRuleTarget j) {
		if (!lookaheadRuleValid) {
			// find highest LookaheadThreshold
			firstLookaheadRule = null;
			PR own = getPrimaryRule();
			while (own != null) {
				if (own instanceof LookaheadThreshold)
					firstLookaheadRule = (LookaheadThreshold) own;
				own = own.getPrimaryRule();
			}
			lookaheadRuleValid = true;
		}

		if (firstLookaheadRule != null)
			return firstLookaheadRule.arrivesTooLate(j);
		else
			return false;
	}

}
