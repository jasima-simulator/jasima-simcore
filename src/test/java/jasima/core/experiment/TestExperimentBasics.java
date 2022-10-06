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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.junit.Test;

/**
 */
public class TestExperimentBasics {

	private final static class FailingExperiment extends Experiment {

		private static final long serialVersionUID = -6683281039708815611L;

		@Override
		protected void performRun() {
			// some dummy action raising an exception at some point
			String s = "";
			for (int i = 0, n = 30000; i < n; i++) {
				if (i == 15000) {
					throw new IllegalStateException("some exception");
				}
				s += "a";
			}
			s.toString();
		}

		@Override
		protected void produceResults() {
			super.produceResults();
			resultMap.put("a", 1);
		}
	}

	private final static class TimeConsumingExperiment extends Experiment {
		private static final long serialVersionUID = -1l;

		private int a = -1;

		@Override
		protected void performRun() {
			try {
				System.out.println(Thread.currentThread() + "\ta");
				a = 0;
				Thread.sleep(500);
				System.out.println(Thread.currentThread() + "\tb");
				a = 1;
				Thread.sleep(500);
				System.out.println(Thread.currentThread() + "\tc");
				a = 2;
			} catch (InterruptedException e) {
				System.out.println(Thread.currentThread() + "\td");
				Thread.currentThread().interrupt();
				throw new RuntimeException(e);
			} catch (Throwable t) {
				System.out.println(Thread.currentThread() + "\tException " + t);
				throw t;
			}
		}

		@Override
		protected void produceResults() {
			System.out.println(Thread.currentThread() + "\tproduceResults()");
			super.produceResults();
			resultMap.put("a", a);
		}

	}

	@Test(expected = IllegalStateException.class)
	public void runDirectWithException() throws Exception {
		Experiment e = new FailingExperiment();

		e.runExperiment();
	}

	@Test
	public void runAsyncWithException1() throws Exception {
		Experiment e = new FailingExperiment();

		CompletableFuture<Map<String, Object>> expFuture = e.runExperimentAsync();

		try {
			expFuture.join();
			fail();
		} catch (CompletionException ex) {
			assertThat(ex, is(instanceOf(CompletionException.class)));
			assertThat(ex.getCause(), is(instanceOf(IllegalStateException.class)));
		}
	}

	@Test
	public void runAsyncWithException2() throws Exception {
		Experiment e = new FailingExperiment();

		CompletableFuture<Map<String, Object>> expFuture = e.runExperimentAsync();

		expFuture.handle((r, ex) -> {
			assertThat(r, is(nullValue()));
			assertThat(ex, is(notNullValue()));
			assertThat(ex, is(instanceOf(IllegalStateException.class)));
			return null;
		}).join();
	}

	@Test(expected = RuntimeException.class)
	public void runDirectTakingTime() throws Exception {
		Experiment e = new TimeConsumingExperiment();

		CompletableFuture<Map<String, Object>> f = e.runExperimentAsync();
		System.out.println(Thread.currentThread() + "\t1");

		Thread.sleep(500);
		System.out.println(Thread.currentThread() + "\t2");

		boolean canceled = f.cancel(true);

		System.out.println(Thread.currentThread() + "\tcancelled: " + canceled);
		Thread.sleep(2000);
		System.out.println(Thread.currentThread() + "\tafter wait");

		try {
			Map<String, Object> res = f.join();
			System.out.println(Thread.currentThread() + "\tafter join\t" + res.toString());
		} finally {
			Thread.sleep(2000);
		}
	}

	@Test(timeout = 5000)
	public void runAsyncFutureGet() throws Exception {
		Experiment e = new TimeConsumingExperiment();

		ExperimentCompletableFuture f = e.runExperimentAsync();

		Map<String, Object> res = f.get(); // wait for exp to finish
		assertThat("result", res.get("a"), is(2));

		Map<String, Object> res2 = f.get(); // second get() should return immediately
		assertThat("same results", res2, is(res));
	}

	@Test(timeout = 5000)
	public void runAsyncFutureJoin() throws Exception {
		Experiment e = new TimeConsumingExperiment();

		ExperimentCompletableFuture f = e.runExperimentAsync();

		Map<String, Object> res = f.join(); // wait for exp to finish
		assertThat("result", res.get("a"), is(2));

		Map<String, Object> res2 = f.join(); // second join() should return immediately
		assertThat("same results", res2, is(res));
	}

	@Test(timeout = 5000)
	public void runAsyncJoinIgnoreExceptions() throws Exception {
		Experiment e = new TimeConsumingExperiment();

		ExperimentCompletableFuture f = e.runExperimentAsync();

		Map<String, Object> res = f.joinIgnoreExceptions(); // wait for exp to finish
		assertThat("result", res.get("a"), is(2));

		Map<String, Object> res2 = f.joinIgnoreExceptions(); // second joinIgnoreExceptions() should return immediately
		assertThat("same results", res2, is(res));
	}

	@Test(expected = IllegalStateException.class)
	public void cantRunSameExperimentTwice() throws Exception {
		ExpTestControlFlow e = new ExpTestControlFlow();
		e.setFail(false);

		Map<String, Object> res = e.runExperiment();
		assertThat("result", res.get("results"), is(23 * 23));

		e.runExperiment(); // not allowed
	}

	@Test
	public void cantRunSameExperimentTwiceAsync() throws Exception {
		ExpTestControlFlow e = new ExpTestControlFlow();
		e.setFail(false);

		// start first execution
		ExperimentCompletableFuture exec1 = e.runExperimentAsync();
		
		// try second start
		try {
			e.runExperimentAsync();
			fail("second execution not prevented");
		} catch (IllegalStateException expected) {
		}
		
		// first execution should complete as expected
		Map<String, Object> res = exec1.join();
		assertThat("result", res.get("results"), is(23 * 23));
		assertThat("expAborted", res.get("expAborted"), is(0));
		assertThat("error", res.get("error"), is(nullValue()));
	}

	@Test
	public void cantRunSameExperimentTwiceAsyncSync() throws Exception {
		ExpTestControlFlow e = new ExpTestControlFlow();
		e.setFail(false);

		// start first execution
		ExperimentCompletableFuture exec1 = e.runExperimentAsync();
		
		// try second start synchronously
		try {
			e.runExperiment();
			fail("second execution not prevented");
		} catch (IllegalStateException expected) {
		}
		
		// first execution should complete as expected
		Map<String, Object> res = exec1.join();
		assertThat("result", res.get("results"), is(23 * 23));
		assertThat("expAborted", res.get("expAborted"), is(0));
		assertThat("error", res.get("error"), is(nullValue()));
	}

}
