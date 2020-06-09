/*******************************************************************************
 * This file is part of jasima, v1.3, the Java simulator for manufacturing and 
 * logistics.
 *  
 * Copyright (c) 2015 		jasima solutions UG
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package jasima.core.expExecution;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jasima.core.experiment.Experiment;
import jasima.core.experiment.ExperimentListener;

/**
 * Thin wrapper around a {@link Future}, making it cancellable/interruptable and
 * allowing it to be used as a
 * {@link CompletionStage}/{@link CompletableFuture}. To achieve this, an
 * experiment is submitted to an {@link ExecutorService}, immediately starting
 * execution.
 * 
 * @author Torsten Hildebrandt
 */
public class ExperimentCompletableFuture extends CompletableFuture<Map<String, Object>> {

	private final Experiment experiment;
	private final Future<Map<String, Object>> future;

	public ExperimentCompletableFuture(Experiment e, ExecutorService es) {
		super();

		this.experiment = requireNonNull(e);
		addFinishedListener();

		this.future = es.submit(e::runExperiment);
	}

	private void addFinishedListener() {
		ExperimentListener finishedListener = new ExperimentListener() {
			@Override
			public void finished(Experiment e, Map<String, Object> results) {
				complete(results);
				e.removeCurrentListener();
			}

			@Override
			public void error(Experiment e, Throwable t) {
				completeExceptionally(t);
				e.removeCurrentListener();
			}
		};
		experiment.addListener(finishedListener);
	}

	public Experiment getExperiment() {
		return experiment;
	}

	public Map<String, Object> joinIgnoreExceptions() {
		CompletableFuture<Map<String, Object>> whenFinished = handle((r, ex) -> experiment.getResults());
		return whenFinished.join();
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		experiment.cancel();
		boolean res = future.cancel(mayInterruptIfRunning);
		super.cancel(mayInterruptIfRunning);
		return res;
	}

	public boolean cancel() {
		return cancel(true);
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
	public Map<String, Object> get() throws ExecutionException, InterruptedException {
		return future.get();
	}

	@Override
	public Map<String, Object> get(long timeout, TimeUnit unit)
			throws ExecutionException, InterruptedException, TimeoutException {
		return future.get(timeout, unit);
	}

}