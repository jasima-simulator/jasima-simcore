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

import jasima.core.expExecution.ExperimentCompletableFuture;
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
	public void mreWithInterruptedSubsShouldAbortIfAbourtUponBaseExperiemntAbortIsTrue() {
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