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
 * @version 
 *          "$Id$"
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
