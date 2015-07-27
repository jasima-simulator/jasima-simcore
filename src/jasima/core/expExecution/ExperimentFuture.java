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
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
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
