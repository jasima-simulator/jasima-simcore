/*******************************************************************************
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This file is part of jasima, v1.2.
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
package jasima.core.experiment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import jasima.core.expExecution.ExperimentExecutor;
import jasima.core.expExecution.ExperimentFuture;
import jasima.core.statistics.SummaryStat;
import jasima.core.util.ConsolePrinter;

import java.util.Map;

import org.junit.Test;

public class TestExperimentExceptions {

	public static class ExceptionExperiment extends Experiment {

		private static final long serialVersionUID = -6611460335281191272L;

		private boolean fail = true;
		private int dummy = 23;

		@Override
		protected void performRun() {
			// do nothing useful
		}

		@Override
		protected void produceResults() {
			super.produceResults();

			if (isFail())
				throw new RuntimeException("Some strange error.");
			else
				resultMap.put("results", getDummy() * getDummy());
		}

		public boolean isFail() {
			return fail;
		}

		public void setFail(boolean fail) {
			this.fail = fail;
		}

		public int getDummy() {
			return dummy;
		}

		public void setDummy(int dummy) {
			this.dummy = dummy;
		}

	}

	@Test
	public void testPlain() {
		ExceptionExperiment e = new ExceptionExperiment();

		// execute in main thread
		try {
			e.runExperiment();
			e.printResults();
			fail("No exception.");
		} catch (RuntimeException expected) {
			// should raise exception
		}
	}

	@Test
	public void testPlainExecutor() throws InterruptedException {
		ExceptionExperiment e = new ExceptionExperiment();

		// execute with Executor
		ExperimentFuture ef = ExperimentExecutor.getExecutor().runExperiment(e,
				null);
		Map<String, Object> res = ef.get();

		// should produce error messages
		String msg = (String) res.get(Experiment.EXCEPTION_MESSAGE);
		assertNotNull(msg);
		String exc = (String) res.get(Experiment.EXCEPTION);
		assertNotNull(exc);
		Integer abort = (Integer) res.get(Experiment.EXP_ABORTED);
		assertEquals(1, abort.intValue());
		Double runtime = (Double) res.get(Experiment.RUNTIME);
		assertNotNull(runtime);
	}

	@Test
	public void test_FFE_Fail() throws InterruptedException {
		FullFactorialExperiment e = new FullFactorialExperiment();

		// execute with Executor
		ExperimentFuture ef = ExperimentExecutor.getExecutor().runExperiment(e,
				null);
		Map<String, Object> res = ef.get();

		// should produce error messages, no base experiment set
		String msg = (String) res.get(Experiment.EXCEPTION_MESSAGE);
		assertNotNull(msg);
		String exc = (String) res.get(Experiment.EXCEPTION);
		assertNotNull(exc);
		Integer abort = (Integer) res.get(Experiment.EXP_ABORTED);
		assertEquals(1, abort.intValue());
	}

	@Test
	public void test_FFE_WithFail() throws InterruptedException {
		FullFactorialExperiment e = new FullFactorialExperiment();
		e.setBaseExperiment(new ExceptionExperiment());
		e.addFactors("fail", true, false);
		e.setAbortUponBaseExperimentAbort(false);

		// execute with Executor
		ExperimentFuture ef = ExperimentExecutor.getExecutor().runExperiment(e,
				null);
		Map<String, Object> res = ef.get();
		ConsolePrinter.printResults(e, res);

		// FFE itself should not produce errors
		Integer abort = (Integer) res.get(Experiment.EXP_ABORTED);
		assertEquals(0, abort.intValue());
		String msg = (String) res.get(Experiment.EXCEPTION_MESSAGE);
		assertNull(msg);
		String exc = (String) res.get(Experiment.EXCEPTION);
		assertNull(exc);

		// but report errors from failing base experiments
		SummaryStat a = (SummaryStat) res.get("baseExperiment."
				+ Experiment.EXP_ABORTED);
		assertEquals(0.5, a.mean(), 1e-10);
		assertNotNull(res.get("baseExperiment." + Experiment.EXCEPTION_MESSAGE));
		assertNotNull(res.get("baseExperiment." + Experiment.EXCEPTION));
	}

	@Test
	public void test_FFE_WithFailAbort() throws InterruptedException {
		FullFactorialExperiment e = new FullFactorialExperiment();
		e.setBaseExperiment(new ExceptionExperiment());
		e.addFactors("fail", true, false);
		e.setAbortUponBaseExperimentAbort(true);

		// execute with Executor
		ExperimentFuture ef = ExperimentExecutor.getExecutor().runExperiment(e,
				null);
		Map<String, Object> res = ef.get();
		ConsolePrinter.printResults(e, res);

		// FFE itself should not produce errors
		Integer abort = (Integer) res.get(Experiment.EXP_ABORTED);
		assertEquals(1, abort.intValue());
		String msg = (String) res.get(Experiment.EXCEPTION_MESSAGE);
		assertNull(msg);
		String exc = (String) res.get(Experiment.EXCEPTION);
		assertNull(exc);

		// but report errors from failing base experiments
		SummaryStat a = (SummaryStat) res.get("baseExperiment."
				+ Experiment.EXP_ABORTED);
		assertEquals(1.0, a.mean(), 1e-10);
		assertNotNull(res.get("baseExperiment." + Experiment.EXCEPTION_MESSAGE));
		assertNotNull(res.get("baseExperiment." + Experiment.EXCEPTION));
	}

}
