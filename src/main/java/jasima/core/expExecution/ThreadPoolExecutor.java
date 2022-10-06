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
