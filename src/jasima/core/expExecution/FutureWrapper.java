package jasima.core.expExecution;

import jasima.core.experiment.Experiment;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Thin wrapper around a {@link Future}, implementing {@link ExperimentFuture}.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version "$Id$"
 */
public class FutureWrapper implements ExperimentFuture {

	private final Experiment experiment;
	private final Future<Map<String, Object>> future;

	public FutureWrapper(Experiment e, Future<Map<String, Object>> future) {
		super();
		this.experiment = e;
		this.future = future;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return future.cancel(mayInterruptIfRunning);
	}

	@Override
	public boolean isCancelled() {
		return future.isCancelled();
	}

	@Override
	public boolean isDone() {
		return future.isDone();
	}

	@Override
	public Map<String, Object> get() throws InterruptedException {
		try {
			return internalGet(-1l, null);
		} catch (TimeoutException e) {
			throw new AssertionError();
		}
	}

	@Override
	public Map<String, Object> get(long timeout, TimeUnit unit)
			throws InterruptedException, TimeoutException {
		return internalGet(timeout, unit);
	}

	protected Map<String, Object> internalGet(long timeout, TimeUnit unit)
			throws InterruptedException, TimeoutException {
		Map<String, Object> res;
		try {
			res = timeout < 0 ? future.get() : future.get(timeout, unit);
		} catch (ExecutionException ex) {
			Throwable cause = ex.getCause();

			// convert exception to string
			Writer sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			cause.printStackTrace(pw);
			String s = sw.toString();
			s = s.replace(System.getProperty("line.separator") + '\t', " \\\\ ");

			// create "artificial" results map
			res = new HashMap<String, Object>();
			res.put(Experiment.EXP_ABORTED, 1);
			res.put(Experiment.EXCEPTION_MESSAGE, cause.getMessage());
			res.put(Experiment.EXCEPTION, s);
		}

		return res;
	}

	@Override
	public Experiment getExperiment() {
		return experiment;
	}

}
