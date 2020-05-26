package jasima.core.util.observer;

import static java.util.Objects.requireNonNull;

import java.util.ArrayDeque;
import java.util.ArrayList;

import jasima.core.experiment.Experiment;
import jasima.core.experiment.ExperimentListener;

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

	/**
	 * A listener that is removing itself after executing once.
	 * 
	 * @author Torsten.Hildebrandt
	 */
	protected class SingleExecListener implements NotifierListener<SUBJECT, MESSAGE> {

		private final NotifierListener<SUBJECT, MESSAGE> listener;

		protected SingleExecListener(NotifierListener<SUBJECT, MESSAGE> l) {
			this.listener = l;
		}

		@Override
		public void inform(SUBJECT publisher, MESSAGE event) {
			try {
				listener.inform(publisher, event);
			} finally {
				assert current == this;
				removeCurrentListener();
			}
		}

	}

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
	public int numListener() {
		return listeners.size();
	}

	/**
	 * Adds a new listener.
	 */
	@Override
	public void addListener(NotifierListener<SUBJECT, MESSAGE> l) {
		listeners.add(requireNonNull(l));
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
	 * Adds a new listener that removes itself after being executed exactly once.
	 * Otherwise this method behaves like
	 * {@link #addListener(Class, NotifierListener)}.
	 */
	@Override
	public <T extends NotifierListener<SUBJECT, MESSAGE>> void addListenerOnce(Class<T> eventType, T eventHandler) {
		addListener(new SingleExecListener(eventHandler));
	}

	/**
	 * Removes the listener given as a parameter. This method returns {@code true}
	 * on success and {@code false} when the listener could not be found.
	 */
	@Override
	public boolean removeListener(NotifierListener<SUBJECT, MESSAGE> l) {
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
	public void removeCurrentListener() {
		boolean removeRes = removeListener(requireNonNull(current));
		assert removeRes;
	}

	/**
	 * Returns the listener with the given index (0-based).
	 * 
	 * @see #numListener()
	 */
	@Override
	public NotifierListener<SUBJECT, MESSAGE> getListener(int idx) {
		return listeners.get(idx);
	}

	/**
	 * Send a message to all registered listeners.
	 */
	@Override
	public void fire(MESSAGE msg) {
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
