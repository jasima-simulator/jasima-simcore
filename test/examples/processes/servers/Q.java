package examples.processes.servers;

import static jasima.core.simulation.SimContext.currentProcess;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import examples.processes.servers.Q.QEvent;
import jasima.core.simulation.SimProcess;
import jasima.core.simulation.SimProcess.MightBlock;
import jasima.core.simulation.SimProcess.ProcessState;
import jasima.core.util.observer.Notifier;
import jasima.core.util.observer.NotifierImpl;
import jasima.core.util.observer.NotifierListener;

public class Q<T> implements Notifier<Q<T>, QEvent> {

	public static interface QEvent {
	}

	public enum QEvents implements QEvent {
		ITEM_ADDED, ITEM_REMOVED,
	}

	interface QListener<T> extends NotifierListener<Q<T>, QEvent> {
		@Override
		default void inform(Q<T> q, QEvent event) {
			if (event == QEvents.ITEM_ADDED) {
				itemAdded(q, q.lastAdded);
			} else if (event == QEvents.ITEM_REMOVED) {
				itemRemoved(q, q.lastRemoved);
			} else {
				handleOther(q, event);
			}
		}

		default void itemAdded(Q<T> q, T item) {
		}

		@FunctionalInterface
		interface ItemAdded<T> extends QListener<T> {
			@Override
			void itemAdded(Q<T> q, T item);
		}

		default void itemRemoved(Q<T> q, T item) {
		}

		@FunctionalInterface
		interface ItemRemoved<T> extends QListener<T> {
			@Override
			void itemRemoved(Q<T> q, T item);
		}

		default void handleOther(Q<T> q, QEvent event) {
		}
	}

	private Deque<T> items = new ArrayDeque<>();
	private List<SimProcess<?>> waiting = new ArrayList<>();

	// event notification

	private T lastAdded = null, lastRemoved = null;
	private NotifierImpl<Q<T>, QEvent> notifierImpl = new NotifierImpl<>(this);

	public void put(T t) throws MightBlock {
		tryPut(t);
	}

	public boolean tryPut(T t) {
		items.addLast(Objects.requireNonNull(t));
		fire(QEvents.ITEM_ADDED);

		waiting.stream().filter(p -> p.processState() == ProcessState.PASSIVE).forEach(SimProcess::resume);

		return true;
	}

	public T take() throws MightBlock {
		SimProcess<?> p = currentProcess();

		waiting.add(p);
		while (items.size() == 0) {
			p.suspend();
		}
		waiting.remove(p);

		T res = tryTake();
		assert res!=null;

		return res;
	}

	public @Nullable T tryTake() {
		while (items.size() == 0) {
			return null;
		}

		T res = items.removeFirst();

		lastRemoved = res;
		fire(QEvents.ITEM_REMOVED);

		return res;
	}

	public int numItems() {
		return items.size();
	}

	public int numWaiting() {
		return waiting.size();
	}

	@Override
	public Notifier<Q<T>, QEvent> notifierImpl() {
		return notifierImpl;
	}

}
