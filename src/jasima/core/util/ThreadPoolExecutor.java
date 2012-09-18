/*******************************************************************************
 * Copyright 2011, 2012 Torsten Hildebrandt and BIBA - Bremer Institut für Produktion und Logistik GmbH
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
 *******************************************************************************/
package jasima.core.util;

import jasima.core.experiment.Experiment;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

/**
 * Default implementation of an ExecutorFactory returning an Executor that uses
 * up to {@code Runtime.getRuntime().availableProcessors()} threads to execute
 * tasks concurrently. This number of threads can be overridden by setting the
 * system property "jasima.core.ThreadPoolExecutor.numThreads".
 * <p />
 * In order to prevent starvation of worker threads waiting for sub-experiments
 * to complete, there is a thread pool for each nesting level of experiments.
 * 
 * @author Torsten Hildebrandt
 * @version "$Id$"
 */
public class ThreadPoolExecutor extends ExperimentExecutor {

	public static final String POOL_SIZE = "jasima.core.ThreadPoolExecutor.numThreads";

	// an executor service for each nesting level
	private Map<Integer, ExecutorService> insts = new HashMap<Integer, ExecutorService>();

	@Override
	public Future<Map<String, Object>> runExperiment(final Experiment e) {
		ExecutorService es = getExecutorInstance(e.isLeafExperiment(),
				e.nestingLevel());
		return es.submit(new Callable<Map<String, Object>>() {
			@Override
			public Map<String, Object> call() throws Exception {
				e.runExperiment();
				return e.getResults();
			}
		});
	}

	@Override
	public synchronized void shutdownNow() {
		for (ExecutorService inst : insts.values()) {
			inst.shutdownNow();
		}
		insts.clear();
	}

	private synchronized ExecutorService getExecutorInstance(boolean isLeaf,
			int nestingLevel) {
		if (isLeaf)
			nestingLevel = -1;

		ExecutorService inst = insts.get(nestingLevel);
		if (inst == null) {
			inst = createExecService(nestingLevel);

			insts.put(nestingLevel, inst);
		}

		return inst;
	}

	private ExecutorService createExecService(int nestingLevel) {
		int numThreads = Runtime.getRuntime().availableProcessors();

		String sizeStr = System.getProperty(POOL_SIZE);
		if (sizeStr != null)
			numThreads = Integer.parseInt(sizeStr.trim());

		ThreadFactory threadFactory = new ThreadFactory() {
			final ThreadFactory defFactory = Executors.defaultThreadFactory();

			@Override
			public Thread newThread(Runnable r) {
				Thread t = defFactory.newThread(r);
				t.setDaemon(true);
				return t;
			}
		};

		return Executors.newFixedThreadPool(numThreads, threadFactory);
	}

}
