/*******************************************************************************
 * Copyright (c) 2010-2013 Torsten Hildebrandt and jasima contributors
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
 *
 * $Id$
 *******************************************************************************/
package jasima.shopSim.core;

import jasima.core.simulation.Simulation.SimMsgCategory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

/**
 * An implementation of a priority queue. The two supported operations are add(T
 * t) and removeLeast(). Order of the elements is determined by a sequencing
 * rule PR.
 * 
 * @author Torsten Hildebrandt, 2009-08-01
 * 
 * @param <T>
 *            The element type contained in this PriorityQueue.
 * @version $Id$
 */
public class PriorityQueue<T extends PrioRuleTarget> implements Serializable {

	private static final long serialVersionUID = -4159482968254168459L;

	public static final double MAX_PRIO = Double.MAX_VALUE;
	public static final double MIN_PRIO = -MAX_PRIO;

	private final class ComparatorImpl<T extends PrioRuleTarget> implements
			Comparator<ListEntry<T>>, Serializable {
		private static final long serialVersionUID = -6907667564606558578L;

		@Override
		public int compare(ListEntry<T> i1, ListEntry<T> i2) {
			// i1 and i2 are the same
			if (i1 == i2)
				return 0;

			double[] p1 = i1.prios;
			double[] p2 = i2.prios;

			int res = comparePrioArrays(p1, p2);
			if (res == 0)
				getWorkStation().shop.print(SimMsgCategory.WARN,
						"equal priorities.");
			return res;
		}
	}

	public static int comparePrioArrays(final double[] p1, final double[] p2) {
		for (int i = 0; i < p1.length; i++) {
			int cmpRes = compareValues(p1[i], p2[i]);
			if (cmpRes != 0)
				return cmpRes;
		}

		return 0;
	}

	public static int compareValues(double v1, double v2) {
		if (v1 == 0.0 && v2 == 0.0) // +/-0.0 are the same
			return 0;
		else
			return -Double.compare(v1, v2);
	}

	private final Comparator<ListEntry<T>> comparator;

	public boolean strangePrioValuesFound = false;

	protected ListEntry<T>[] nodes_; // the tree nodes, packed into an array
	protected int count_ = 0; // number of used slots

	private ListEntry<T> reuse; // use old ListEntries again

	private PR sr;
	// all rules and their tie breakers unrolled in an array
	private PR[] rules = new PR[0];
	private double[] bestPrios;
	private final WorkStation workStation;

	private static class ListEntry<T> {
		public ListEntry(int numPrios) {
			super();

			prios = new double[numPrios];
		}

		public T elem;
		public final double[] prios;
		public ListEntry<T> next; // linked list to reuse ListEntries
	}

	public PriorityQueue(WorkStation workStation) {
		super();
		this.workStation = workStation;

		nodes_ = new ListEntry[11];

		comparator = new ComparatorImpl<T>();
	}

	/** Return number of elements **/
	public int size() {
		return count_;
	}

	public void add(T elem) {
		final ListEntry<T> e;
		if (reuse != null) {
			e = reuse;
			reuse = reuse.next;
		} else {
			e = new ListEntry<T>(rules.length);
		}
		e.elem = elem;
		// e.idx = num++;

		// ensure enough capacity
		if (count_ >= nodes_.length) {
			setCapacity(3 * nodes_.length / 2 + 1);
		}

		// simply store in nodes_, priority is computed on removeLargest
		nodes_[count_++] = e;
	}

	/**
	 * Removes the largest element (largest priority) from the q.
	 */
	public T removeLargest() {
		T res = null;
		bestPrios = null;

		int minIdx = updatePrios();
		if (minIdx >= 0) {
			final ListEntry<T> entry = nodes_[minIdx];
			nodes_[minIdx] = nodes_[--count_];
			nodes_[count_] = null;

			res = entry.elem;
			bestPrios = entry.prios;

			recycle(entry);
		}

		return res;
	}

	/**
	 * Returns the largest element (largest priority) from the q without
	 * removing it.
	 */
	public T peekLargest() {
		T res = null;
		bestPrios = null;

		int minIdx = updatePrios();
		if (minIdx >= 0) {
			ListEntry<T> minEntry = nodes_[minIdx];

			bestPrios = minEntry.prios;
			res = minEntry.elem;
		}

		return res;
	}

	/**
	 * Return all elements in this list ordered by their priority. Results are
	 * returned in 'resArray', the number of elements returned depends on the
	 * size of this array. This means, if, e.g., 'resArray' has a size of 3
	 * elements, the 3 elements with the largest priority are returned.
	 * 'resArray' can be larger than the number of elements currently contained
	 * in this queue. In this case, only the first size() elements are used.
	 */
	public T[] getAllElementsInOrder(T[] resArray) {
		if (resArray == null || resArray.length == 0)
			throw new IllegalArgumentException();

		int minIdx = updatePrios();
		ListEntry<T> min = nodes_[minIdx];

		// sort nodes_ by current priorities
		Arrays.sort(nodes_, 0, count_, comparator);
		assert comparator.compare(nodes_[0], min) == 0;

		// write result in 'resArray'
		for (int i = 0, n = Math.min(resArray.length, count_); i < n; i++) {
			resArray[i] = nodes_[i].elem;
		}

		return resArray;
	}

	/**
	 * Adds all elements of this queue to the collection c.
	 */
	public <C extends Collection<T>> C getAllElements(C c) {
		for (int i = 0, n = count_; i < n; i++) {
			c.add(nodes_[i].elem);
		}
		return c;
	}

	/**
	 * Returns the i'th element from this queue. Indexing is <b>not</b> based on
	 * priority, instead the order is arbitrary.
	 */
	public T get(int i) {
		return nodes_[i].elem;
	}

	/**
	 * Remove object o from this queue. Comparison is based on o's identity, not
	 * by calling equals().
	 */
	public boolean remove(T o) {
		for (int i = 0; i < count_; i++) {
			if (o == nodes_[i].elem) {
				final ListEntry<T> entry = nodes_[i];
				nodes_[i] = nodes_[--count_]; // swap with last element
				nodes_[count_] = null;
				recycle(entry);

				return true;
			}
		}
		return false;
	}

	/** Removes all elements. **/
	public void clear() {
		for (int i = 0; i < count_; ++i) {
			final ListEntry<T> entry = nodes_[i];
			nodes_[i] = null;
			recycle(entry);
		}
		count_ = 0;
	}

	/**
	 * Returns true if "o" is contained in this queue. Comparison is based on
	 * o's identity, not by calling equals().
	 */
	public boolean contains(T o) {
		for (int i = 0; i < count_; i++) {
			if (o == nodes_[i].elem) {
				return true;
			}
		}
		return false;
	}

	private void recycle(final ListEntry<T> entry) {
		entry.elem = null;
		entry.next = reuse;
		reuse = entry;
	}

	public void setSequencingRule(PR sr) {
		if (this.sr == sr)
			return;

		if (size() > 0)
			throw new IllegalStateException(
					"Can only change sequencing rule if queue is empty.");

		this.sr = sr;

		rules = new PR[dimCount(sr)];
		int i = 0;
		do {
			rules[i++] = sr;
		} while ((sr = sr.getTieBreaker()) != null);

		reuse = null;
	}

	public PR getSequencingRule() {
		return sr;
	}

	protected int capacity() {
		return nodes_.length;
	}

	protected void setCapacity(int newCap) {
		ListEntry<T>[] newnodes = new ListEntry[newCap];
		System.arraycopy(nodes_, 0, newnodes, 0, count_);
		nodes_ = newnodes;
	}

	private int updatePrios() {
		if (count_ == 0)
			return -1;

		if (rules.length == 0) {
			assert sr == null;
			return -1;
		}

		for (PR rule : rules) {
			rule.beforeCalc(this);
			if (rule.keepIdle())
				return -1;
		}

		updatePrio(nodes_[0]);
		ListEntry<T> minEntry = nodes_[0];
		int minIdx = 0;

		for (int i = 1, n = count_; i < n; i++) {
			final ListEntry<T> le = nodes_[i];
			updatePrio(le);
			if (comparator.compare(minEntry, le) > 0) {
				minEntry = le;
				minIdx = i;
			}
		}

		return minIdx;
	}

	private void updatePrio(ListEntry<T> le) {
		final double[] vs = le.prios;
		assert vs.length == rules.length;

		for (int j = 0; j < vs.length; j++) {
			double d = vs[j] = rules[j].calcPrio(le.elem);

			if (!strangePrioValuesFound)
				strangePrioValuesFound = Double.isInfinite(d)
						|| Double.isNaN(d);
		}
	}

	private int dimCount(PR sr) {
		int dims = 0;
		while (sr != null) {
			dims++;
			sr = sr.getTieBreaker();
		}
		return dims;
	}

	/**
	 * Returns priorities of the entries returned by the last call of
	 * peekLargest or removeLargest. Be careful: do not change the values
	 * contained in the returned array. The result array is only valid
	 * immediately after calling peekLargest/removeLargest (array is reused and
	 * can be overridden by subsequent calls to add()).
	 * <p/>
	 * This method returns null if null (keep machine idle) was returned by
	 * {@link #peekLargest()} or {@link #removeLargest()}.
	 */
	public double[] getBestPrios() {
		return bestPrios;
	}

	/**
	 * Returns the {@link WorkStation} this queue is responsible for.
	 * 
	 * @return The workstation (maybe null).
	 */
	public WorkStation getWorkStation() {
		return workStation;
	}

}
