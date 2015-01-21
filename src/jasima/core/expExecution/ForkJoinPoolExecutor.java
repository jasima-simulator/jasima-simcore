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
package jasima.core.expExecution;

import jasima.core.experiment.Experiment;

import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An implementation of {@link ExperimentExecutor} using a {@link ForkJoinPool}
 * (introduced in Java 1.7) to execute tasks. This pretty much simplifies code
 * compared to {@link ThreadPoolExecutor}, as we don't have to care about
 * possible starvation of worker threads waiting for sub-experiments to
 * complete.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>, 2012-09-05
 * @version 
 *          "$Id$"
 */
public class ForkJoinPoolExecutor extends ExperimentExecutor {

	public static final String POOL_SIZE_SETTING = ForkJoinPoolExecutor.class
			.getName() + ".numThreads";

	private ForkJoinPool pool;

	protected ForkJoinPoolExecutor() {
		super();
		pool = createPool();
	}

	private ForkJoinPool createPool() {
		int numThreads = Runtime.getRuntime().availableProcessors();
		String sizeStr = System.getProperty(POOL_SIZE_SETTING);
		if (sizeStr != null)
			numThreads = Integer.parseInt(sizeStr.trim());

		ForkJoinWorkerThreadFactory threadFactory = new ForkJoinWorkerThreadFactory() {
			private AtomicInteger n = new AtomicInteger(-1);

			@Override
			public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
				ForkJoinWorkerThread t = ForkJoinPool.defaultForkJoinWorkerThreadFactory
						.newThread(pool);
				t.setName("jasimaWorker" + n.addAndGet(1));
				t.setDaemon(true);
				return t;
			}
		};

		return new ForkJoinPool(numThreads, threadFactory, null, false);
	}

	@SuppressWarnings("serial")
	@Override
	public ExperimentFuture runExperiment(final Experiment e) {
		ForkJoinTask<Map<String, Object>> task;
		synchronized (pool) {
			task = pool.submit(new RecursiveTask<Map<String, Object>>() {
				@Override
				public Map<String, Object> compute() {
					e.runExperiment();
					return e.getResults();
				}
			});
		}
		return new FutureWrapper(e, task);
	}

	@Override
	public void shutdownNow() {
		pool.shutdownNow();
	}

}
