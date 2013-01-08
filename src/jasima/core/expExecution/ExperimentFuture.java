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

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This class is very similar to {@link java.util.concurrent.Future}, but has no
 * direct dependencies to it. The only difference is that get()-methods don't
 * throw an {@link ExecutionException}. Instead a result map is returned
 * containing the exception message and the exception in text format.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version "$Id$"
 * @see FutureWrapper
 */
public interface ExperimentFuture {

	public Experiment getExperiment();

	public boolean cancel(boolean mayInterruptIfRunning);

	public boolean isCancelled();

	public boolean isDone();

	public Map<String, Object> get() throws InterruptedException;

	public Map<String, Object> get(long timeout, TimeUnit unit)
			throws InterruptedException, TimeoutException;

}
