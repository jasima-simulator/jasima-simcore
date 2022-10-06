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
package examples.processes.servers;

import static jasima.core.simulation.SimContext.activate;
import static jasima.core.simulation.SimContext.trace;
import static jasima.core.simulation.SimContext.waitFor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import jasima.core.simulation.SimContext;
import jasima.core.simulation.SimEvent;
import jasima.core.simulation.SimProcess.MightBlock;
import jasima.core.simulation.generic.Q;
import jasima.core.simulation.generic.Q.QListener.ItemAdded;
import jasima.core.simulation.generic.Q.QListener.ItemRemoved;
import jasima.core.util.SimProcessUtil.SimAction;
import jasima.core.util.TypeHint;

public class TestQ {

	@Test
	public void testBasicFunctionalityNoBlocking() throws MightBlock {
		SimContext.simulationOf(sim -> {
			double startTime = sim.simTime();

			Q<String> q = new Q<String>();
			assertEquals("numItems", 0, q.numItems());
			q.put("item1");
			assertEquals("numItems", 1, q.numItems());
			q.put("item2");
			assertEquals("numItems", 2, q.numItems());
			String item = q.take();
			assertEquals("fifoOrder", "item1", item);
			assertEquals("numItems", 1, q.numItems());
			String item2 = q.take();
			assertEquals("fifoOrder", "item2", item2);
			assertEquals("numItems", 0, q.numItems());

			assertEquals("simTime not changed", startTime, sim.simTime(), 1e-6);
		});
	}

	@Test
	public void testBasicFunctionality() throws MightBlock {
		AtomicInteger numTaken = new AtomicInteger(0);
		SimContext.simulationOf(sim -> {
			sim.setSimulationLength(2.9);

			Q<String> q = new Q<String>();

			sim.schedulePeriodically(0.0, 1.0, SimEvent.EVENT_PRIO_NORMAL, () -> activate(() -> {
				waitFor(0.5);
				q.put("item");
			}));

			@SuppressWarnings("unused")
			String item;

			item = q.take();
			numTaken.incrementAndGet();
			assertEquals("first item available", 0.5, sim.simTime(), 1e-6);

			item = q.take();
			numTaken.incrementAndGet();
			assertEquals("second item available", 1.5, sim.simTime(), 1e-6);

			item = q.take();
			numTaken.incrementAndGet();
			assertEquals("third item available", 2.5, sim.simTime(), 1e-6);
		});
		assertEquals("take() returned 3 times", 3, numTaken.get());
	}

	@Test
	public void testMultipleTakersSequentialArrivals() throws MightBlock {
		double iat = 1.0;
		int numServer = 5;
		int jobsPerServer = 2;

		AtomicInteger numTaken = new AtomicInteger(0);

		SimContext.simulationOf(sim -> {
			sim.setSimulationLength(jobsPerServer * numServer * iat); // every server 2 items
			waitFor(1.0); // start simTime at 1.0

			Q<String> q = new Q<String>();

			activate("itemGen", s -> {
				int n = 0;
				while (true) {
					q.put("item" + ++n);
					waitFor(iat);
				}
			});

			SimAction serverProc = s -> {
				while (true) {
					String item = q.take();
					trace("processing item " + item);
					numTaken.incrementAndGet();
					waitFor(iat * numServer);
				}
			};
			for (int n = 0; n < numServer; n++) {
				activate("server" + (n + 1), serverProc);
			}
		});
		assertEquals("number of served items", numServer * jobsPerServer, numTaken.get());
	}

	@Test
	public void testMultipleTakersSimultaneousArrivals() throws MightBlock {
		double iat = 1.0;
		int numServer = 5;
		int jobsPerServer = 2;

		AtomicInteger numTaken = new AtomicInteger(0);

		SimContext.simulationOf(sim -> {
			sim.setSimulationLength(jobsPerServer * numServer * iat); // every server 2 items
			waitFor(1.0); // start simTime at 1.0

			Q<String> q = new Q<String>();

			SimAction serverProc = s -> {
				while (true) {
					String item = q.take();
					trace("processing item " + item);
					numTaken.incrementAndGet();
					waitFor(iat * numServer);
				}
			};
			for (int n = 0; n < numServer; n++) {
				activate("server" + (n + 1), serverProc);
			}
			assertEquals("numWaiting", 0, q.numWaitingTake());
			waitFor(0.001); // activate only scheduled activation event
			assertEquals("numWaiting", numServer, q.numWaitingTake());

			for (int n = 0; n < numServer * jobsPerServer; n++) {
				q.put("item" + n);
			}
			assertEquals("numJobs1", numServer * jobsPerServer, q.numItems());
			trace("before");
			waitFor(0.001); // give server a chance to start their work
			trace("after");
			assertEquals("numJobs2", numServer, q.numItems());
		});
		assertEquals("number of served items", numServer * jobsPerServer, numTaken.get());
	}

	@Test
	public void testEventParamsItemAdded() {
		AtomicInteger checksSuccessful = new AtomicInteger(0);
		SimContext.simulationOf(sim -> {
			Q<Integer> queue = new Q<>();
			queue.addListener(new TypeHint<ItemAdded<Integer>>(), (q, item) -> {
				if (q == queue)
					checksSuccessful.incrementAndGet();
				if (item == 23)
					checksSuccessful.incrementAndGet();
			});
			queue.put(23);
		});
		assertEquals("successful checks", 2, checksSuccessful.get());
	}

	@Test
	public void testEventParamsItemRemoved() {
		AtomicInteger checksSuccessful = new AtomicInteger(0);
		SimContext.simulationOf(sim -> {
			Q<Integer> queue = new Q<>();
			queue.addListener(new TypeHint<ItemRemoved<Integer>>(), (q, item) -> {
				if (q == queue)
					checksSuccessful.incrementAndGet();
				if (item == 23)
					checksSuccessful.incrementAndGet();
			});
			queue.put(23);
			queue.tryTake();
		});
		assertEquals("successful checks", 2, checksSuccessful.get());
	}

	@Test
	public void testRestrictedCapacityNoBlocking() {
		SimContext.simulationOf(sim -> {
			Q<String> q = new Q<String>();
			assertEquals("numAvailable", Integer.MAX_VALUE, q.numAvailable());
			q.setCapacity(1);
			assertEquals("numAvailable", 1, q.numAvailable());
			assertTrue("1st put", q.tryPut("item1"));
			assertFalse("2nd put", q.tryPut("item2"));
			assertEquals("numAvailable", 0, q.numAvailable());
			assertEquals("take", "item1", q.tryTake());
			assertEquals("numAvailable", 1, q.numAvailable());
			assertTrue("2nd put now succeeds", q.tryPut("item2"));
		});
	}

	@Test
	public void testRestrictedCapacityBlocking() {
		SimContext.simulationOf(sim -> {
			double startTime = sim.simTime();

			Q<String> q = new Q<String>();
			q.setCapacity(1);

			sim.scheduleAt(0.5, 0, () -> assertEquals("item1", q.tryTake()));

			q.put("item1");
			assertEquals("first put immediately", 0, sim.simTime() - startTime, 1e-6);

			q.put("item2");
			assertEquals("2nd put blocks until first item taken", 0.5, sim.simTime() - startTime, 1e-6);
		});
	}

	@Test
	public void testRestrictedCapacityBlocking__CapacityIncrease() {
		AtomicBoolean lifecycleFinished = new AtomicBoolean(false);
		SimContext.simulationOf(sim -> {
			double startTime = sim.simTime();

			Q<String> q = new Q<String>();
			q.setCapacity(1);

			sim.scheduleAt(0.5, 0, () -> q.setCapacity(-1));

			q.put("item1");
			assertEquals("first put immediately", 0, sim.simTime() - startTime, 1e-6);

			q.put("item2");
			assertEquals("2nd put blocks until capacity increased", 0.5, sim.simTime() - startTime, 1e-6);

			lifecycleFinished.set(true);
		});

		assertTrue("lifecycle finished", lifecycleFinished.get());
	}

}
