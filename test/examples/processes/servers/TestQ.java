package examples.processes.servers;

import static jasima.core.simulation.SimContext.activate;
import static jasima.core.simulation.SimContext.trace;
import static jasima.core.simulation.SimContext.waitFor;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import jasima.core.simulation.SimContext;
import jasima.core.simulation.SimEvent;
import jasima.core.simulation.SimProcess.MightBlock;
import jasima.core.util.SimProcessUtil.SimAction;

public class TestQ {

	@Test
	public void testBasicFunctionalityNoBlocking() throws MightBlock {
		SimContext.of(sim -> {
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
		SimContext.of(sim -> {
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

		SimContext.of(sim -> {
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

		SimContext.of(sim -> {
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
			assertEquals("numWaiting", 0, q.numWaiting());
			waitFor(0.001); // activate only scheduled activation event
			assertEquals("numWaiting", numServer, q.numWaiting());

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

}
