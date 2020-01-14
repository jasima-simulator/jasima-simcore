package jasima.core.simulation.processes;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.locks.LockSupport;

public class ThreadBasedCoroutine implements Continuation {

	private static ThreadGroup simThreads;

	private static Thread getThread(SimProcess<?> p) {
		if (simThreads == null || simThreads.isDestroyed()) {
			simThreads = new ThreadGroup("jasimaSimThreads");
			simThreads.setDaemon(true);
		}

		Thread t = new Thread(simThreads, p::internalRun);
		String suffix = t.getName().substring(t.getName().indexOf('-'));
		t.setName("SimProcess" + suffix);

		return t;
	}

	public static Continuation of(SimProcess<?> p) {
		return new ThreadBasedCoroutine(p);
	}

	private boolean wasSignaled;
	private Thread procExecutor;

	public ThreadBasedCoroutine(SimProcess<?> p) {
		super();

		this.procExecutor = getThread(requireNonNull(p));
		procExecutor.start();

		this.wasSignaled = false;
	}

	@Override
	public void activate() {
		assert !wasSignaled;

		wasSignaled = true;
		LockSupport.unpark(procExecutor);
	}

	@Override
	public void deactivate() {
		assert Thread.currentThread() == procExecutor;
		assert !wasSignaled;

		while (!wasSignaled) { // guard against spurious wake-ups
			LockSupport.park();
		}
		wasSignaled = false;
	}

}
