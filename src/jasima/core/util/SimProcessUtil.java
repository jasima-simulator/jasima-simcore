package jasima.core.util;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import jasima.core.simulation.SimProcess;
import jasima.core.simulation.SimProcess.MightBlock;
import jasima.core.simulation.Simulation;

/**
 * Static helper methods and definitions used to implement {@link SimProcess}es.
 * 
 * @author Torsten Hildebrandt
 */
public final class SimProcessUtil {

	private static final int DEF_THREAD_STACK_SIZE = 64 * 1024;

	private static final ExecutorService exec = Executors.newCachedThreadPool(SimProcessUtil::newWorkerThread);
	private static volatile ThreadGroup simThreads = null;
	private static final int threadStackSize = Integer
			.parseInt(System.getProperty(SimProcess.class.getName() + ".threadStackSize", "" + DEF_THREAD_STACK_SIZE));
	private static AtomicInteger threadNumber = new AtomicInteger(0);

	private static Thread newWorkerThread(Runnable r) {
		// lazy init of ThreadGroup simThreads using double check idiom
		ThreadGroup tg = simThreads;
		if (tg == null || tg.isDestroyed()) {
			synchronized (SimProcessUtil.class) {
				tg = simThreads;
				if (tg == null || tg.isDestroyed()) {
					tg = new ThreadGroup("jasimaSimThreads");
					tg.setDaemon(true);
					simThreads = tg;
				}
			}
		}
		// create a new thread
		Thread t = new Thread(simThreads, r, "jasimaSimThread-" + threadNumber.incrementAndGet(), threadStackSize);
		t.setDaemon(true);
		return t;
	}

	public static void pauseExecuting(Thread executor) {
		assert executor == currentExecutor();
		LockSupport.park();
	}

	public static void continueWith(Thread executor) {
		LockSupport.unpark((Thread) requireNonNull(executor));
	}

	public static void startExecuting(Runnable r) {
		exec.submit(r);
	}

	public static Thread currentExecutor() {
		return Thread.currentThread();
	}

//	/**
//	 * Wraps the given {@link SimRunnable} to be used as a {@link Callable} always
//	 * returning {@code null}. This method is the same as
//	 * {@link Executors#callable(Runnable)}, just working with a
//	 * {@link SimRunnable}.
//	 * 
//	 * @param <R> The produced callable will always return null, so R can be
//	 *            anything.
//	 * @param r   The object to wrap, mustn't be null.
//	 * @return A {@link Callable} executing {@code r} when it's
//	 *         {@link call()}-method is called.
//	 */
//	public static <R> Callable<R> callable(SimRunnable r) {
//		return r == null ? null : new RunnableWrapper<>(r);
//	}

	public static <R> SimCallable<R> simCallable(SimRunnable r) {
		return r == null ? null : new SimRunnableWrapper<>(r);
	}

	public static <R> SimCallable<R> simCallable(SimAction r) {
		return r == null ? null : new SimActionWrapper<>(r);
	}

	public static <R> SimCallable<R> simCallable(Callable<R> c) {
		return c == null ? null : new CallableWrapper<>(c);
	}

	/**
	 * Same as Java's {@link Runnable}, except that it can throw the marker
	 * Exception {@link MightBlock}. Therefore any {@link Runnable} can also be used
	 * as a {@link SimRunnable}, but additionally the executed code could be
	 * declared to throw {@link MightBlock}, without having to handle it.
	 */
	@FunctionalInterface
	public interface SimRunnable {
		void run() throws MightBlock;
	}

	@FunctionalInterface
	public interface SimAction {
		void run(Simulation sim) throws MightBlock;
	}

	@FunctionalInterface
	public interface SimCallable<R> {
		R call(Simulation sim) throws MightBlock, Exception;
	}

	// utility methods and classes for public API below

	/**
	 * A SimAction that runs a given task ({@link SimRunnable}) and always returns
	 * {@code null}.
	 */
	static final class SimRunnableWrapper<R> implements SimCallable<R> {
		final SimRunnable task;

		SimRunnableWrapper(SimRunnable task) {
			this.task = task;
		}

		@Override
		public R call(Simulation sim) throws MightBlock {
			task.run();
			return null;
		}
	}

	static final class SimActionWrapper<R> implements SimCallable<R> {
		final SimAction task;

		SimActionWrapper(SimAction task) {
			this.task = task;
		}

		@Override
		public R call(Simulation sim) throws MightBlock, Exception {
			task.run(sim);
			return null;
		}
	}

	static final class CallableWrapper<R> implements SimCallable<R> {
		final Callable<R> c;

		CallableWrapper(Callable<R> task) {
			this.c = task;
		}

		@Override
		public R call(Simulation sim) throws MightBlock, Exception {
			return c.call();
		}
	}

	/**
	 * Prevent instantiation and sub-classing.
	 */
	private SimProcessUtil() {
	}

}
