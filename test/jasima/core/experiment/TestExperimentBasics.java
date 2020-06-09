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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
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
			System.out.println(Thread.currentThread() + "\tafter join.");
		} finally {
			Thread.sleep(2000);
		}
	}

}
