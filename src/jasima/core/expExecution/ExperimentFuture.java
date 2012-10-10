package jasima.core.expExecution;

import jasima.core.experiment.Experiment;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This class is very similar to {@link java.util.concurrent.Future}, but has no
 * direct dependencies to it. The only difference is that get()-methods don't
 * throw an {@link ExecutionException}. Instead a result map is returned
 * containing the exception message and the exception in text format.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version "$Id$"
 * @see FutureWrapper
 */
public interface ExperimentFuture {

	public Experiment getExperiment();
	
	public boolean cancel(boolean mayInterruptIfRunning);

	public boolean isCancelled();

	public boolean isDone();

	public Map<String, Object> get() throws InterruptedException;

	public Map<String, Object> get(long timeout, TimeUnit unit)
			throws InterruptedException, TimeoutException;

}
