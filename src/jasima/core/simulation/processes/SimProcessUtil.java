package jasima.core.simulation.processes;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

/**
 * Static helper methods and definitions used to implement {@link SimProcess}es.
 * 
 * @author Torsten.Hildebrandt
 */
public final class SimProcessUtil {

	/**
	 * Wraps the given {@link SimRunnable} to be used as a {@link Callable} always
	 * returning {@code null}. This method is the same as
	 * {@link Executors#callable(Runnable)}, just working with a
	 * {@code SimRunnable}.
	 * 
	 * @param <R> The produced callable will always return null, so R can be
	 *            anything.
	 * @param r   The object to wrap, mustn't be null.
	 * @return A {@code Callable} executing {@code r} when it's
	 *         {@code call()}-method is called.
	 */
	public static <R> Callable<R> callable(SimRunnable r) {
		return new RunnableWrapper<>(requireNonNull(r));
	}

	/**
	 * Same as Java's {@code Runnable}, except that it can throw the marker
	 * Exception {@code MightBlock}. Therefore any {@code Runnable} can also be used
	 * as a {@code SimRunnable}, but additionally the executed code could be
	 * declared to throw {@code MightBlock}, without having to handle it.
	 * 
	 * @author Torsten.Hildebrandt
	 */
	@FunctionalInterface
	public interface SimRunnable {
		void run() throws MightBlock;
	}

	// utility methods and classes for public API below

	/**
	 * A Callable that runs a given task ({@code SimRunnable}) and returns null.
	 */
	static final class RunnableWrapper<R> implements Callable<R> {
		final SimRunnable task;

		RunnableWrapper(SimRunnable task) {
			this.task = task;
		}

		@Override
		public R call() throws MightBlock {
			task.run();
			return null;
		}
	}

	/**
	 * Prevent instantiation and sub-classing.
	 */
	private SimProcessUtil() {
	}

}
