package jasima.core.simulation.generic;

import static jasima.core.simulation.SimContext.currentProcess;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import jasima.core.simulation.SimContext;
import jasima.core.simulation.SimProcess;
import jasima.core.simulation.SimProcess.MightBlock;
import jasima.core.simulation.SimProcess.ProcessState;
import jasima.core.simulation.generic.Q.QEvent;
import jasima.core.util.observer.Notifier;
import jasima.core.util.observer.NotifierImpl;
import jasima.core.util.observer.NotifierListener;

public class Q<T> implements Notifier<Q<T>, QEvent> {

	public static void enter(Q<SimProcess<?>> q) throws MightBlock {
		SimProcess<?> proc = SimContext.currentProcess();
		q.put(proc);
	}

	public static void leave(Q<SimProcess<?>> q) {
		SimProcess<?> proc = SimContext.currentProcess();
		boolean removed = q.remove(proc);
		if (!removed) {
			throw new IllegalStateException();
		}
	}

	public static interface QEvent {
	}

	public enum QEvents implements QEvent {
		ITEM_ADDED, ITEM_REMOVED,
	}

	public interface QListener<T> extends NotifierListener<Q<T>, QEvent> {
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
	private List<SimProcess<?>> awaitingTake = new ArrayList<>();
	private List<SimProcess<?>> awaitingPut = new ArrayList<>();

	// event notification

	private T lastAdded = null, lastRemoved = null;
	private NotifierImpl<Q<T>, QEvent> notifierImpl = new NotifierImpl<>(this);
	private int capacity = -1;

	public void put(T t) throws MightBlock {
		SimProcess<?> p = currentProcess();

		awaitingPut.add(p);
		while (numAvailable() <= 0) {
			p.suspend();
		}
		awaitingPut.remove(p);

		boolean putRes = tryPut(t);
		assert putRes;
	}

	public boolean tryPut(T t) {
		if (numAvailable() <= 0) {
			return false;
		}

		items.addLast(Objects.requireNonNull(t));
		itemAdded(t);

		return true;
	}

	public T take() throws MightBlock {
		SimProcess<?> p = currentProcess();

		awaitingTake.add(p);
		while (numItems() == 0) {
			p.suspend();
		}
		awaitingTake.remove(p);

		T res = tryTake();
		assert res != null;

		return res;
	}

	public @Nullable T tryTake() {
		while (numItems() == 0) {
			return null;
		}

		T res = items.removeFirst();
		itemRemoved(res);
		return res;
	}

	public boolean remove(T t) {
		boolean res = items.remove(t);
		if (res) {
			itemRemoved(t);
		}
		return res;
	}

	private void itemAdded(T t) {
		lastAdded = t;
		fire(QEvents.ITEM_ADDED);
		resumeTakeProcesses();
	}

	private void itemRemoved(T t) {
		lastRemoved = t;
		fire(QEvents.ITEM_REMOVED);
		resumePutProcesses();
	}

	private void resumeTakeProcesses() {
		awaitingTake.stream().filter(p -> p.processState() == ProcessState.PASSIVE).forEach(SimProcess::resume);
	}

	private void resumePutProcesses() {
		awaitingPut.stream().filter(p -> p.processState() == ProcessState.PASSIVE).forEach(SimProcess::resume);
	}

	public int numItems() {
		return items.size();
	}

	public int numAvailable() {
		return (capacity < 0) ? Integer.MAX_VALUE : Math.max(capacity - items.size(), 0);
	}

	public int numWaitingTake() {
		return awaitingTake.size();
	}

	public int numWaitingPut() {
		return awaitingPut.size();
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int newCapacity) {
		int oldAvailable = numAvailable();
		capacity = newCapacity;
		if (oldAvailable < numAvailable()) {
			resumePutProcesses();
		}
	}

	@Override
	public Notifier<Q<T>, QEvent> notifierImpl() {
		return notifierImpl;
	}

}
