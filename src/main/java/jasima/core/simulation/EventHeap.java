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
package jasima.core.simulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * An implementation of {@link jasima.core.simulation.EventQueue} using an
 * array-based heap.
 * 
 * @author Torsten Hildebrandt
 */
public final class EventHeap implements EventQueue {

	private SimEvent[] nodes; // the tree nodes, packed into an array
	private int count = 0; // number of used slots
	private boolean invalidRoot = false;

	/**
	 * Create an event heap with an initial capacity of 103.
	 */
	public EventHeap() {
		this(103);
	}

	/**
	 * Create an event heap with the given capacity.
	 * 
	 * @exception IllegalArgumentException if capacity less or equal to zero
	 */
	public EventHeap(int capacity) throws IllegalArgumentException {
		if (capacity <= 0)
			throw new IllegalArgumentException();
		nodes = new SimEvent[capacity];
	}

	/**
	 * Copy constructor.
	 */
	protected EventHeap(EventHeap copyFrom) {
		this(copyFrom.size() + 2);
		invalidRoot = copyFrom.invalidRoot;
		count = copyFrom.count;
		System.arraycopy(copyFrom.nodes, 0, nodes, 0, Math.min(count + 2, copyFrom.nodes.length));
	}

	/**
	 * insert an element, resize if necessary
	 */
	@Override
	public void insert(SimEvent x) {
		if (count >= nodes.length) {
			setCapacity(3 * nodes.length / 2 + 1);
		}

		if (invalidRoot) {
			// move new element to root
			nodes[0] = x;
			++count;
			invalidRoot = false;

			sink(nodes[0], 0);
		} else {
			// bubble up: normal heap insertion if more than one insert() in
			// succession
			bubbleUp(x, count);
			++count;
		}
	}

	/**
	 * Return and remove least element, or null if empty.
	 */
	@Override
	public SimEvent extract() {
		final SimEvent[] nodes = this.nodes;

		// extract() called more than once in succession?
		if (invalidRoot) {
			fixRootNode();
		}

		SimEvent least = nodes[0];
		nodes[0] = null;

		--count;

		invalidRoot = true;

		return least;
	}

	private void fixRootNode() {
		// move last element to root
		nodes[0] = nodes[count];
		nodes[count] = null;
		invalidRoot = false;

		sink(nodes[0], 0);
	}

	/**
	 * Scans the underlying array if it contains the given element. This methods
	 * requires O(n) time and compares object references for equality, not using
	 * {@code equals()}.
	 * 
	 * @param element the element to look for.
	 * @return the index of the element in the underlying array
	 */
	public int indexOf(SimEvent element) {
		Objects.requireNonNull(element);
		if (invalidRoot) {
			fixRootNode();
		}

		for (int i = 0; i < count; i++) {
			if (nodes[i] == element) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Removes an element from this heap. Required O(n) time to find the position of
	 * {@code element} in the underlying array.
	 * 
	 * @param element the element to remove
	 * @return {@code true} if the element was contained in the heap and
	 *         successfully removed, {@code false} otherwise
	 */
	public boolean remove(SimEvent element) {
		int idx = indexOf(element);
		if (idx < 0) {
			return false;
		}

		nodes[idx] = null;

		SimEvent e = nodes[count - 1];
		nodes[count - 1] = null;

		count--;

		// restore heap condition
		if (e != null) {
			if (bubbleUp(e, idx) == idx) {
				sink(e, idx);
			}
		}

		return true;
	}

	/** remove all elements * */
	public void clear() {
		Arrays.fill(nodes, 0, count, null);
		count = 0;
		invalidRoot = false;
	}

	/** Return number of elements * */
	@Override
	public int size() {
		return count;
	}

	/**
	 * Returns all events as an ordered list.
	 * 
	 * @return All events.
	 */
	@Override
	public ArrayList<SimEvent> allEvents() {
		ArrayList<SimEvent> res = new ArrayList<>();

		EventHeap copy = new EventHeap(this);
		for (int i = 0; i < count; i++)
			res.add(copy.extract());

		return res;
	}

	private void setCapacity(int newCap) {
		if (newCap < count) {
			throw new IllegalArgumentException("Capacity has to be larger than count.");
		}
		SimEvent[] newnodes = new SimEvent[newCap];
		System.arraycopy(nodes, 0, newnodes, 0, count);
		nodes = newnodes;
	}

	private int bubbleUp(SimEvent x, int k) {
		final SimEvent[] nodes = this.nodes;

		while (k > 0) {
			int par = parent(k);
			if (x.compareTo(nodes[par]) < 0) {
				nodes[k] = nodes[par];
				k = par;
			} else
				break;
		}
		nodes[k] = x;
		return k;
	}

	private int sink(SimEvent x, int k) {
		final SimEvent[] nodes = this.nodes;
		final int count = this.count;

		int l;
		while ((l = left(k)) < count) {
			int r = right(k);
			int child = (r >= count || nodes[l].compareTo(nodes[r]) < 0) ? l : r;
			if (x.compareTo(nodes[child]) > 0) {
				nodes[k] = nodes[child];
				k = child;
			} else
				break;
		}
		nodes[k] = x;
		return k;
	}

	// indexes of heap parents and children
	private static final int parent(int k) {
		return (k - 1) >>> 1;
		// return (k - 1) / 2;
	}

	private static final int left(int k) {
		return (k << 1) + 1;
		// return 2 * k + 1;
	}

	private static final int right(int k) {
		return (k << 1) + 2;
		// return 2 * (k + 1);
	}

}
