package jasima.core.experiment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import jasima.core.expExecution.ExperimentExecutor;
import jasima.core.expExecution.ExperimentFuture;
import jasima.core.statistics.SummaryStat;

import java.util.Map;

import org.junit.Test;

public class TestExperimentExceptions {

	public static class ExceptionExperiment extends Experiment {

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
		ExperimentFuture ef = ExperimentExecutor.getExecutor().runExperiment(e);
		Map<String, Object> res = ef.get();

		// should produce error messages
		String msg = (String) res.get(Experiment.EXCEPTION_MESSAGE);
		assertNotNull(msg);
		String exc = (String) res.get(Experiment.EXCEPTION);
		assertNotNull(exc);
		Integer abort = (Integer) res.get(Experiment.EXP_ABORTED);
		assertEquals(1, abort.intValue());
	}

	@Test
	public void test_FFE_Fail() throws InterruptedException {
		FullFactorialExperiment e = new FullFactorialExperiment();

		// execute with Executor
		ExperimentFuture ef = ExperimentExecutor.getExecutor().runExperiment(e);
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
		ExperimentFuture ef = ExperimentExecutor.getExecutor().runExperiment(e);
		Map<String, Object> res = ef.get();
		e.printResults(res);

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
		ExperimentFuture ef = ExperimentExecutor.getExecutor().runExperiment(e);
		Map<String, Object> res = ef.get();
		e.printResults(res);

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
