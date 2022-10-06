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
package jasima.core.util.observer;

import static java.util.Objects.requireNonNull;

import java.util.ArrayDeque;
import java.util.ArrayList;

import jasima.core.experiment.Experiment;
import jasima.core.experiment.ExperimentListener;
import jasima.core.util.TypeHint;

/**
 * Example implementation of {@link Notifier} functionality. A
 * {@code NotifierImpl} handles notifier functionality for some real
 * {@code Notifier} (subject).
 * <p>
 * It is safe to fire new events while a current one is executing. In that case
 * the new message will be processed after processing the current one finished.
 * <p>
 * Listeners can also be removed while a message is send/processed.
 * <p>
 * As an example of how to use this class, have a look at
 * {@link ExperimentListener} used by {@link Experiment}s.
 * 
 * @see Notifier
 * @see NotifierListener
 * 
 * @author Torsten Hildebrandt
 */
public class NotifierImpl<SUBJECT extends Notifier<SUBJECT, MESSAGE>, MESSAGE> implements Notifier<SUBJECT, MESSAGE> {

	private final SUBJECT subject; // the class that is firing events

	private ArrayList<NotifierListener<SUBJECT, MESSAGE>> listeners; // currently registered listeners

	private ArrayDeque<MESSAGE> msgs; // used to temporarily save new messages send while iterating
	private int it; // index used while iterating / firing
	private NotifierListener<SUBJECT, MESSAGE> current; // the currently active listener while firing

	public NotifierImpl(SUBJECT subject) {
		super();
		this.subject = subject;
		this.listeners = new ArrayList<>();
		this.msgs = null;
		this.it = -1;
		this.current = null;
	}

	@Override
	public Notifier<SUBJECT, MESSAGE> notifierImpl() {
		return this;
	}

	/**
	 * Returns the number of currently registered listeners.
	 */
	@Override
	public synchronized int numListener() {
		return listeners.size();
	}

	/**
	 * Adds a new listener.
	 */
	@Override
	public synchronized <T extends NotifierListener<SUBJECT, MESSAGE>> T addListener(T l) {
		listeners.add(requireNonNull(l));
		return l;
	}

	/**
	 * Adds a new listener. In contrast to {@link #addListener(NotifierListener)}
	 * this method has an additional type parameter, usually representing a
	 * functional interface. Using this approach {@code eventHandler} can be a
	 * lambda expression or method reference.
	 */
	@Override
	public <T extends NotifierListener<SUBJECT, MESSAGE>> void addListener(Class<T> listenerType, T eventHandler) {
		addListener(eventHandler);
	}

	/**
	 * Adds a new listener. This is the same as
	 * {@link #addListener(Class, NotifierListener)}, only adding {@code TypeRef} as
	 * a super type token. This can be used when the listener is a parameterized
	 * type.
	 */
	@Override
	public <T extends NotifierListener<SUBJECT, MESSAGE>> void addListener(TypeHint<T> listenerType, T eventHandler) {
		addListener(eventHandler);
	}

	/**
	 * Removes the listener given as a parameter. This method returns {@code true}
	 * on success and {@code false} when the listener could not be found.
	 */
	@Override
	public synchronized boolean removeListener(NotifierListener<SUBJECT, MESSAGE> l) {
		int idx = listeners.indexOf(requireNonNull(l));
		if (idx < 0) {
			return false;
		}

		listeners.remove(idx);

		// do we have to adjust iteration position?
		if (it >= 0 && it < idx) {
			it--;
			assert it >= 0;
		}

		return true;
	}

	/**
	 * This method can be used to unregister the currently active listener while
	 * firing. This is particularly useful when using lambda's as listeners (see
	 * {@link #addListener(Class, NotifierListener)}), as their reference is
	 * otherwise hard to find (not accessible using {@code this}).
	 * <p>
	 * 
	 * @throws NullPointerException If called while not firing.
	 */
	@Override
	public synchronized void removeCurrentListener() {
		boolean removeRes = removeListener(requireNonNull(current));
		assert removeRes;
	}

	/**
	 * Returns the listener with the given index (0-based).
	 * 
	 * @see #numListener()
	 */
	@Override
	public synchronized NotifierListener<SUBJECT, MESSAGE> getListener(int idx) {
		return listeners.get(idx);
	}

	/**
	 * Send a message to all registered listeners.
	 */
	@Override
	public synchronized void fire(MESSAGE msg) {
		if (it >= 0) {
			// already firing, i.e., listener triggered another event
			if (msgs == null)
				msgs = new ArrayDeque<>();
			// enqueue msg for later processing
			msgs.addLast(msg);
		} else {
			// normal firing
			try {
				do {
					it = 0;
					while (it < listeners.size()) {
						NotifierListener<SUBJECT, MESSAGE> l = listeners.get(it);
						it++;

						current = l;
						l.inform(subject, msg);
					}
					it = -1;
					msg = null;

					if (msgs != null && msgs.size() > 0) {
						msg = msgs.removeFirst();
					}
				} while (msg != null);
			} finally {
				current = null;
			}
		}
	}

}
