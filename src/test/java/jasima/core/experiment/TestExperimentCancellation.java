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
package jasima.core.experiment;

import static jasima.core.experiment.Experiment.EXCEPTION;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinPool;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import jasima.core.experiment.ExperimentListener.StartingListener;
import jasima.core.run.ConsoleRunner;
import jasima.core.statistics.SummaryStat;

public class TestExperimentCancellation {

	@Rule
	public Timeout globalTimeout = new Timeout(1000);

	private void cancelAfterMillis(ExpTestControlFlow e, long time) {
		// cancel from external Thread after 50ms
		ForkJoinPool.commonPool().execute(() -> {
			try {
				Thread.sleep(time);
				e.cancel();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		});
	}

	private void cancelAfterMillis(ExperimentCompletableFuture e, long time) {
		// cancel from external Thread after 50ms
		ForkJoinPool.commonPool().execute(() -> {
			try {
				Thread.sleep(time);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			e.cancel();
		});
	}

	private void interruptAfterMillis(Thread executor, long time) {
		// cancel from external Thread after 50ms
		ForkJoinPool.commonPool().execute(() -> {
			try {
				Thread.sleep(time);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			executor.interrupt();
		});
	}

	@Test(expected = CancellationException.class)
	public void directExectionShouldThrowCancellationException() {
		ExpTestControlFlow e = new ExpTestControlFlow();
		e.setRuntimeMillis(100);
		e.setFail(false);

		cancelAfterMillis(e, 50);

		e.runExperiment();
		fail();
	}

	@Test(expected = CancellationException.class)
	public void asyncExectionShouldThrowCancellationException() {
		ExpTestControlFlow e = new ExpTestControlFlow();
		e.setRuntimeMillis(100);
		e.setFail(false);

		cancelAfterMillis(e, 50);

		e.runExperimentAsync().join();
		fail();
	}

	@Test(expected = CancellationException.class)
	public void asyncExectionShouldBeCancellableViaFuture() {
		ExpTestControlFlow e = new ExpTestControlFlow();
		e.setRuntimeMillis(100);
		e.setFail(false);

		ExperimentCompletableFuture cf = e.runExperimentAsync();
		cancelAfterMillis(cf, 50);

		cf.join();
		fail();
	}

	@Test
	public void asyncExectionShouldReportCancellationExceptionInResults() {
		ExpTestControlFlow e = new ExpTestControlFlow();
		e.setRuntimeMillis(100);
		e.setFail(false);

		cancelAfterMillis(e, 50);

		Map<String, Object> res = e.runExperimentAsync().joinIgnoreExceptions();
		assertThat((String) res.get(EXCEPTION), startsWith(CancellationException.class.getCanonicalName()));
	}

	@Test
	public void directExectionShouldBeInterruptable() {
		ExpTestControlFlow e = new ExpTestControlFlow();
		e.setRuntimeMillis(100);
		e.setFail(false);

		// interrupt from different Thread
		interruptAfterMillis(Thread.currentThread(), 50);

		try {
			e.runExperiment();
			fail();
		} catch (CancellationException expected) {
			assertThat("interrupt flag should be preserved", Thread.interrupted(), is(true));
		}
	}

	@Test
	public void asyncExectionShouldBeInterruptable() {
		ExpTestControlFlow e = new ExpTestControlFlow();
		e.setRuntimeMillis(100);
		e.setFail(false);

		// listener is executed in Experiment Thread
		e.addListener(StartingListener.class, exp -> interruptAfterMillis(Thread.currentThread(), 50));

		try {
			e.runExperimentAsync().join();
			fail();
		} catch (CancellationException expected) {
			assertThat(Thread.interrupted(), is(false));
		}
	}

	@Test
	public void mreWithInterruptedSubsShouldReportErrorsButRunEverything() {
		ExpTestControlFlow e = new ExpTestControlFlow();
		e.setRuntimeMillis(100);
		e.setFail(false);
		e.addListener(StartingListener.class, exp -> interruptAfterMillis(Thread.currentThread(), 50));
	
		MultipleReplicationExperiment mre = new MultipleReplicationExperiment(e, 10);
		mre.setAbortUponBaseExperimentAbort(false);
		
		Map<String, Object> res = ConsoleRunner.run(mre);
		assertThat((Integer) res.get("numTasks"), is(10));
		assertThat((Integer) res.get("expAborted"), is(0));
		
		SummaryStat subAborted = (SummaryStat) res.get("baseExperiment.expAborted");
		assertThat(subAborted.mean(), is(closeTo(1.0, 1e-6)));
		assertThat(subAborted.numObs(), is(10));
	}

	@Test
	public void mreWithInterruptedSubsShouldAbortIfAbortUponBaseExperiemntAbortIsTrue() {
		ExpTestControlFlow e = new ExpTestControlFlow();
		e.setRuntimeMillis(100);
		e.setFail(false);
		e.addListener(StartingListener.class, exp -> interruptAfterMillis(Thread.currentThread(), 50));
	
		MultipleReplicationExperiment mre = new MultipleReplicationExperiment(e, 10);
//		mre.setAbortUponBaseExperimentAbort(true); // this should be the default
		
		Map<String, Object> res = ConsoleRunner.run(mre);
		assertThat((Integer) res.get("numTasks"), is(1));
		assertThat((Integer) res.get("expAborted"), is(1));
		
		SummaryStat subAborted = (SummaryStat) res.get("baseExperiment.expAborted");
		assertThat(subAborted.mean(), is(closeTo(1.0, 1e-6)));
		assertThat(subAborted.numObs(), is(1));
	}

}