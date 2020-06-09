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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import jasima.core.experiment.Experiment;

/**
 * <p>
 * Default implementation of an ExecutorFactory returning an Executor that uses
 * up to {@code Runtime.getRuntime().availableProcessors()} threads to execute
 * tasks concurrently. This number of threads can be overridden by setting the
 * system property "jasima.core.expExecution.ThreadPoolExecutor.numThreads".
 * </p>
 * <p>
 * In order to prevent starvation of worker threads waiting for sub-experiments
 * to complete, there is a thread pool for each nesting level of experiments.
 * </p>
 * 
 * @author Torsten Hildebrandt
 */
public class ThreadPoolExecutor extends ExperimentExecutor {

	public static final String POOL_SIZE_SETTING = ThreadPoolExecutor.class.getName() + ".numThreads";

	// an executor service for each nesting level
	private Map<Integer, ExecutorService> insts = new HashMap<Integer, ExecutorService>();

	// has to be public to be callable using reflection
	public ThreadPoolExecutor() {
		super();
	}

	@Override
	public ExecutorService experimentExecutor(Experiment e, Experiment parent) {
		return getExecutorInstance(e.nestingLevel());
	}

	@Override
	public synchronized void shutdownNow() {
		for (ExecutorService inst : insts.values()) {
			inst.shutdownNow();
		}
		insts.clear();
	}

	private synchronized ExecutorService getExecutorInstance(int nestingLevel) {
		ExecutorService inst = insts.get(nestingLevel);
		if (inst == null) {
			inst = createExecService(nestingLevel);
			insts.put(nestingLevel, inst);
		}

		return inst;
	}

	private ExecutorService createExecService(final int nestingLevel) {
		int numThreads = Runtime.getRuntime().availableProcessors();
		String sizeStr = System.getProperty(POOL_SIZE_SETTING);
		if (sizeStr != null)
			numThreads = Integer.parseInt(sizeStr.trim());

		ThreadFactory threadFactory = new ThreadFactory() {
			final ThreadFactory defFactory = Executors.defaultThreadFactory();
			final AtomicInteger numCreated = new AtomicInteger(0);

			@Override
			public Thread newThread(Runnable r) {
				Thread t = defFactory.newThread(r);
				t.setDaemon(true);
				t.setName("jasimaWorker-" + nestingLevel + "-" + numCreated.addAndGet(1));
				return t;
			}
		};

		return Executors.newFixedThreadPool(numThreads, threadFactory);
	}

}
