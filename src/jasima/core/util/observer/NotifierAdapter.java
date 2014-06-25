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
package jasima.core.util.observer;

import jasima.core.util.Util;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

/**
 * Implementation of a {@link Notifier} functionality. A NotifierAdapter handles
 * notifier functionality for some real Notifier.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version 
 *          "$Id$"
 */
public class NotifierAdapter<N extends Notifier<N, E>, E> implements
		Notifier<N, E>, Serializable, Cloneable {

	private static final long serialVersionUID = 72063783838585993L;

	private final N notifier;

	private ArrayList<NotifierListener<N, E>> listeners = new ArrayList<NotifierListener<N, E>>();
	private ArrayDeque<E> events = null;
	private Iterator<NotifierListener<N, E>> it;

	public NotifierAdapter(final N notifier) {
		if (notifier == null)
			throw new NullPointerException("notifier");
		this.notifier = notifier;
	}

	public void addNotifierListener(NotifierListener<N, E> listener) {
		if (listener == null)
			throw new NullPointerException("listener");
		if (it != null) // addNotifierListener() called while firing()
			throw new ConcurrentModificationException();
		listeners.add(listener);
	}

	public void removeNotifierListener(NotifierListener<N, E> listener) {
		if (listener == null)
			throw new NullPointerException("listener");
		if (it != null) {
			int oldPos = 0;
			assert (oldPos = listeners.indexOf(listener)) >= 0;
			it.remove();
			assert (oldPos != listeners.indexOf(listener));
		} else
			listeners.remove(listener);
	}

	public void fire(E event) {
		if (it != null) {
			// already firing, i.e., listener triggered another event
			if (events == null)
				events = new ArrayDeque<E>();
			events.addLast(event);
		} else {
			do {
				it = listeners.iterator();
				while (it.hasNext()) {
					it.next().update(this.notifier, event);
				}
				it = null;

				event = null;
				if (events != null && events.size() > 0)
					event = events.removeFirst();
			} while (event != null);
		}
	}

	public int numListener() {
		return listeners.size();
	}

	@Override
	public NotifierListener<N, E> getNotifierListener(int index) {
		return listeners.get(index);
	}

	/**
	 * Provides a deep clone of this {@code NotifierAdapter}, trying to clone
	 * each Listener contained using the method
	 * {@link Util#cloneIfPossible(Object)}
	 * 
	 * @see Util#cloneIfPossible(Object)
	 */
	@Override
	public NotifierAdapter<N, E> clone() throws CloneNotSupportedException {
		@SuppressWarnings("unchecked")
		NotifierAdapter<N, E> c = (NotifierAdapter<N, E>) super.clone();

		c.it = null;
		c.events = null;

		c.listeners = new ArrayList<NotifierListener<N, E>>(listeners.size());
		for (int i = 0, n = listeners.size(); i < n; i++) {
			NotifierListener<N, E> clone = Util.cloneIfPossible(listeners
					.get(i));
			c.listeners.add(clone);
		}

		return c;
	}

}