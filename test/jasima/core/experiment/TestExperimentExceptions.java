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
package jasima.core.experiment;

import static jasima.core.expExecution.ExperimentExecutor.runExperimentAsync;
import static jasima.core.experiment.Experiment.EXCEPTION;
import static jasima.core.experiment.Experiment.EXCEPTION_MESSAGE;
import static jasima.core.experiment.Experiment.EXP_ABORTED;
import static jasima.core.experiment.Experiment.RUNTIME;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Map;
import java.util.concurrent.CompletionException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import jasima.core.statistics.SummaryStat;
import jasima.core.util.ConsolePrinter;

public class TestExperimentExceptions {

	@Rule
	public Timeout globalTimeout = new Timeout(1000);

	@Test(expected = MyRuntimeException.class)
	public void directExectionShouldThrowErrorToCallingThread() {
		ExpTestControlFlow e = new ExpTestControlFlow();
		e.setFail(true);

		// execute directly in main thread
		e.runExperiment();
	}

	@Test
	public void directExectionWithoutErrorShouldNotAddErrorResults() {
		ExpTestControlFlow e = new ExpTestControlFlow();
		e.setFail(false);

		// execute directly in main thread
		Map<String, Object> res = e.runExperiment();

		assertThat(res, not(hasKey(EXCEPTION_MESSAGE)));
		assertThat(res, not(hasKey(EXCEPTION)));
	}

	@Test
	public void directExecutionShouldSetErrorResultsDespiteException() {
		ExpTestControlFlow exp = new ExpTestControlFlow();
		exp.setFail(true);

		// execute directly in main thread
		try {
			exp.runExperiment();
		} catch (MyRuntimeException ignore) {
		}

		Map<String, Object> expResults = exp.getResults();
		assertThat(expResults, hasKey(EXCEPTION_MESSAGE));
		assertThat(expResults, hasKey(EXCEPTION));
		assertThat(expResults, hasEntry(EXP_ABORTED, 1));
		assertThat(expResults, hasKey(RUNTIME));
	}

	@Test
	public void executionWithExperimentExecutorShouldThrowCompletionExceptionOnError() throws Exception {
		ExpTestControlFlow e = new ExpTestControlFlow();
		e.setFail(true);

		// execute with Executor
		ExperimentCompletableFuture ef = runExperimentAsync(e, null);

		try {
			ef.join();
			fail();
		} catch (CompletionException expected) {
			assertThat(expected.getCause(), is(instanceOf(MyRuntimeException.class)));
		}
	}

	@Test
	public void asyncExecutionShouldThrowCompletionExceptionOnError() throws Exception {
		ExpTestControlFlow e = new ExpTestControlFlow();
		e.setFail(true);

		// execute with Executor
		ExperimentCompletableFuture ef = e.runExperimentAsync();
		try {
			ef.join();
			fail();
		} catch (CompletionException expected) {
			assertThat(expected.getCause(), is(instanceOf(MyRuntimeException.class)));
		}
	}

	@Test
	public void testAsyncExecutionShouldAddErrorResultsWhenIgnoringExceptions() throws Exception {
		ExpTestControlFlow e = new ExpTestControlFlow();
		e.setFail(true);

		// execute with Executor
		ExperimentCompletableFuture ef = runExperimentAsync(e, null);

		// ignore execution error but check special results
		Map<String, Object> res = ef.joinIgnoreExceptions();

		// should produce error messages
		String msg = (String) res.get(EXCEPTION_MESSAGE);
		assertNotNull(msg);
		String exc = (String) res.get(EXCEPTION);
		assertNotNull(exc);
		Integer abort = (Integer) res.get(EXP_ABORTED);
		assertEquals(1, abort.intValue());
		Double runtime = (Double) res.get(RUNTIME);
		assertNotNull(runtime);
	}

	@Test(expected = IllegalArgumentException.class)
	public void misconfiguredFFEShouldThrowException() throws Exception {
		FullFactorialExperiment e = new FullFactorialExperiment();

		// execute with Executor
		e.runExperiment();
		fail();
	}

	@Test
	public void misconfiguredFFEShouldSetErrorValues() throws Exception {
		FullFactorialExperiment e = new FullFactorialExperiment();

		// execute with Executor
		ExperimentCompletableFuture ef = runExperimentAsync(e, null);
		Map<String, Object> res = ef.joinIgnoreExceptions();

		// should produce error messages, no base experiment set
		String msg = (String) res.get(EXCEPTION_MESSAGE);
		assertNotNull(msg);
		String exc = (String) res.get(EXCEPTION);
		assertNotNull(exc);
		Integer abort = (Integer) res.get(EXP_ABORTED);
		assertEquals(1, abort.intValue());
	}

	@Test
	public void testFFEShouldRunDespiteSubExperimentsFailing() throws Exception {
		FullFactorialExperiment e = new FullFactorialExperiment();
		e.setBaseExperiment(new ExpTestControlFlow());
		e.addFactor("fail", true, false);
		e.setAbortUponBaseExperimentAbort(false);

		// execute with Executor
		ExperimentCompletableFuture ef = runExperimentAsync(e, null);
		Map<String, Object> res = ef.get();
		ConsolePrinter.printResults(e, res);

		// FFE itself should not produce errors
		Integer abort = (Integer) res.get(EXP_ABORTED);
		assertEquals(0, abort.intValue());
		String msg = (String) res.get(EXCEPTION_MESSAGE);
		assertNull(msg);
		String exc = (String) res.get(EXCEPTION);
		assertNull(exc);

		// but report errors from failing base experiments
		SummaryStat a = (SummaryStat) res.get("baseExperiment." + EXP_ABORTED);
		assertEquals(0.5, a.mean(), 1e-10);
		assertNotNull(res.get("baseExperiment." + EXCEPTION_MESSAGE));
		assertNotNull(res.get("baseExperiment." + EXCEPTION));
	}

	@Test
	public void testFFEShouldFailOnFirstAbortingSubExperiment() throws Exception {
		FullFactorialExperiment e = new FullFactorialExperiment();
		e.setBaseExperiment(new ExpTestControlFlow());
		e.addFactor("fail", true, false);

		e.setAbortUponBaseExperimentAbort(true);

		// execute with Executor
		ExperimentCompletableFuture ef = runExperimentAsync(e, null);
		Map<String, Object> res = ef.get();
		ConsolePrinter.printResults(e, res);

		// FFE itself should not produce errors
		Integer abort = (Integer) res.get(EXP_ABORTED);
		assertEquals(1, abort.intValue());
		String msg = (String) res.get(EXCEPTION_MESSAGE);
		assertNull(msg);
		String exc = (String) res.get(EXCEPTION);
		assertNull(exc);

		// but report errors from failing base experiments
		SummaryStat a = (SummaryStat) res.get("baseExperiment." + EXP_ABORTED);
		assertEquals(1.0, a.mean(), 1e-10);
		assertNotNull(res.get("baseExperiment." + EXCEPTION_MESSAGE));
		assertNotNull(res.get("baseExperiment." + EXCEPTION));
	}

}
