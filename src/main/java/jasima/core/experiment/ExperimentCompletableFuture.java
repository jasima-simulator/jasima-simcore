/*
This file is part of jasima, the Java simulator for manufacturing and logistics.
 
Copyright 2010-2022 jasima contributors (see license.txt)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package jasima.core.experiment;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
		e.aboutToStart();
		addFinishedListener();
		this.future = es.submit(e::runExperimentInternal);
	}

	private void addFinishedListener() {
		ExperimentListener finallyListener = new ExperimentListener() {
			@Override
			public void finalAction(Experiment e) {
				if (e.getError() != null) {
					completeExceptionally(e.getError());
				} else {
					complete(e.getResults());
				}
				e.removeCurrentListener();
			}
		};
		experiment.addListener(finallyListener);
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