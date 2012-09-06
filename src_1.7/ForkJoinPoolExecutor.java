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

import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

/**
 * An implementation of {@link ExperimentExecutor} using a {@link ForkJoinPool}
 * (introduced in Java 1.7) to execute tasks. This pretty much simplifies code
 * compared to {@link ThreadPoolExecutor}, as we don't have to care about
 * possible starvation of worker threads waiting for sub-experiments to
 * complete.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>, 2012-09-05
 */
public class ForkJoinPoolExecutor extends ExperimentExecutor {

	private ForkJoinPool pool = new ForkJoinPool();

	protected ForkJoinPoolExecutor() {
		super();
	}

	@SuppressWarnings("serial")
	@Override
	public Future<Map<String, Object>> runExperiment(final Experiment e) {
		return pool.submit(new RecursiveTask<Map<String, Object>>() {
			@Override
			public Map<String, Object> compute() {
				e.runExperiment();
				return e.getResults();
			}
		});
	}

	@Override
	public void shutdownNow() {
		pool.shutdownNow();
	}

}
