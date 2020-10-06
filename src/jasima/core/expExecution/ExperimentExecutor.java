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

import static jasima.core.util.TypeUtil.createInstance;
import static jasima.core.util.TypeUtil.getClassFromSystemProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import javax.annotation.Nullable;

import jasima.core.experiment.Experiment;
import jasima.core.experiment.ExperimentCompletableFuture;

/**
 * Base class for classes executing experiments. This class implements the
 * Abstract Factory pattern, therefore ExperimentExecutor.getExecutor() has to
 * be called to create executor instances. This call is delegated to a
 * non-abstract implementation of ExperimentExecutor. Which ExperimentExecutor
 * to use is determined by a system property "
 * {@code jasima.core.expExecution.ExperimentExecutor}". As a default, a
 * {@link ThreadPoolExecutor} is used (with a maximum number of threads equal to
 * the number of available processors).
 * 
 * @author Torsten Hildebrandt
 * @see ThreadPoolExecutor
 * @see ForkJoinPoolExecutor
 */
public abstract class ExperimentExecutor {

	public static final String EXECUTOR_FACTORY = ExperimentExecutor.class.getName();
	public static final Class<? extends ExperimentExecutor> execFactoryImpl = getClassFromSystemProperty(
			EXECUTOR_FACTORY, ExperimentExecutor.class, ThreadPoolExecutor.class);

	// thread-safe lazy initialization holder class idiom for static fields
	private static class ExecHolder {
		static final ExperimentExecutor instance = createExecutor();
	}

	public static ExperimentExecutor getExecutor() {
		return ExecHolder.instance;
	}

	private static ExperimentExecutor createExecutor() {
		ExperimentExecutor result = createInstance(execFactoryImpl);

		// cleanup
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				result.shutdownNow();
			}
		});

		return result;
	}

	/**
	 * Don't call directly, use {@link #getExecutor()} instead. Has to be public to
	 * be callable by reflection.
	 */
	public ExperimentExecutor() {
		super();
	}

	/**
	 * Shuts down this {@link ExperimentExecutor}.
	 */
	public abstract void shutdownNow();

	/**
	 * Returns the {@code ExecutorService} that should be used to execute the
	 * {@link Experiment}.
	 * 
	 * @param e      The experiment to be executed.
	 * @param parent A parent experiment that is executing {@code e} (can be null).
	 * @return The {@code ExecutorService} to execute it with.
	 */
	public abstract ExecutorService experimentExecutor(Experiment e, @Nullable Experiment parent);

	/**
	 * Execute many experiments at once. The implementation here simply calls
	 * {@link #runExperimentAsync(Experiment,Experiment)} for all experiments in
	 * {@code es}.
	 * 
	 * @param es     A list of {@link Experiment}s to run.
	 * @param parent The parent experiment of "es". This might be null.
	 * 
	 * @return A {@link Collection} of {@link ExperimentCompletableFuture}s, one for
	 *         each submitted experiment.
	 */
	public static Collection<ExperimentCompletableFuture> runAllExperiments(Collection<? extends Experiment> es,
			Experiment parent) {
		ArrayList<ExperimentCompletableFuture> res = new ArrayList<ExperimentCompletableFuture>(es.size());

		for (Experiment e : es) {
			res.add(runExperimentAsync(e, parent));
		}

		return res;
	}

	/**
	 * Runs an experiment (usually in an asynchronous way). Therefore an
	 * {@link ExperimentCompletableFuture} is returned to access results once they
	 * become available.
	 * 
	 * @param e      The experiment to execute.
	 * @param parent The parent experiment of "e". This might be null.
	 * 
	 * @return An {@link ExperimentCompletableFuture} to access experiment results.
	 */
	public static ExperimentCompletableFuture runExperimentAsync(Experiment e, Experiment parent) {
		return runExperimentAsync(e, parent, getExecutor().experimentExecutor(e, parent));
	}

	/**
	 * Runs an experiment (usually in an asynchronous way). Therefore an
	 * {@link ExperimentCompletableFuture} is returned to access results once they
	 * become available.
	 * 
	 * @param e      The experiment to execute.
	 * @param parent The parent experiment of "e". This might be null.
	 * 
	 * @return An {@link ExperimentCompletableFuture} to access experiment results.
	 */
	public static ExperimentCompletableFuture runExperimentAsync(Experiment e, Experiment parent, ExecutorService es) {
		return new ExperimentCompletableFuture(e, es);
	}

}
