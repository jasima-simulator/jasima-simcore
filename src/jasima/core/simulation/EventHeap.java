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
package jasima.core.simulation;

import jasima.core.simulation.Simulation.EventQueue;

import java.io.Serializable;

/**
 * An implementation of {@link jasima.core.simulation.Simulation.EventQueue}
 * using an array-based heap.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>, 2012-08-30
 * @version $Id$
 */
public final class EventHeap implements EventQueue, Serializable {
	private static final long serialVersionUID = -7578258752027946114L;

	private Event[] nodes; // the tree nodes, packed into an array
	private int count = 0; // number of used slots
	private boolean invalidRoot = false;

	/**
	 * Create a Heap with the given capacity, and relying on natural ordering.
	 * 
	 * @exception IllegalArgumentException
	 *                if capacity less or equal to zero
	 */
	public EventHeap(int capacity) throws IllegalArgumentException {
		if (capacity <= 0)
			throw new IllegalArgumentException();
		nodes = new Event[capacity];
	}

	// indexes of heap parents and children
	private final int parent(int k) {
		return (k - 1) >>> 1;
		// return (k - 1) / 2;
	}

	private final int left(int k) {
		return (k << 1) + 1;
		// return 2 * k + 1;
	}

	private final int right(int k) {
		return (k << 1) + 2;
		// return 2 * (k + 1);
	}

	/**
	 * insert an element, resize if necessary
	 */
	@Override
	public void insert(Event x) {
		final Event[] nodes = this.nodes;

		if (count >= nodes.length) {
			setCapacity(3 * nodes.length / 2 + 1);
		}

		if (invalidRoot) {
			// move new element to root
			nodes[0] = x;
			++count;

			sinkRoot();
		} else {
			// bubble up: normal heap insertion if more than one insert() in
			// succession
			int k = count;
			++count;
			while (k > 0) {
				int par = parent(k);
				if (x.compareTo(nodes[par]) < 0) {
					nodes[k] = nodes[par];
					k = par;
				} else
					break;
			}
			nodes[k] = x;
		}
	}

	/**
	 * Return and remove least element, or null if empty.
	 */
	@Override
	public Event extract() {
		final Event[] nodes = this.nodes;

		// extract() called more than once in succession?
		if (invalidRoot) {
			// move largest element to root
			nodes[0] = nodes[count];
			nodes[count] = null;

			sinkRoot();
		}

		Event least = nodes[0];
		nodes[0] = null;

		--count;

		invalidRoot = true;

		return least;
	}

	private void sinkRoot() {
		final int count = this.count;
		final Event[] nodes = this.nodes;
		invalidRoot = false;

		int k = 0;
		Event x = nodes[k];
		int l;
		while ((l = left(k)) < count) {
			int r = right(k);
			int child = (r >= count || nodes[l].compareTo(nodes[r]) < 0) ? l
					: r;
			if (x.compareTo(nodes[child]) > 0) {
				nodes[k] = nodes[child];
				k = child;
			} else
				break;
		}
		nodes[k] = x;
	}

	/** Return least element without removing it, or null if empty * */
	public Event peek() {
		if (count > 0)
			return nodes[0];
		else
			return null;
	}

	/** Return number of elements * */
	public int size() {
		return count;
	}

	/** remove all elements * */
	public void clear() {
		for (int i = 0; i < count; ++i)
			nodes[i] = null;
		count = 0;
	}

	public int capacity() {
		return nodes.length;
	}

	public void setCapacity(int newCap) {
		Event[] newnodes = new Event[newCap];
		System.arraycopy(nodes, 0, newnodes, 0, count);
		nodes = newnodes;
	}

}
