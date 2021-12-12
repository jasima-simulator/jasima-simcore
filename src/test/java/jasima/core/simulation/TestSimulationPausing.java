package jasima.core.simulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;

import jasima.core.simulation.Simulation.SimExecState;
import jasima.core.util.ConsolePrinter;
import jasima.core.util.MsgCategory;

public class TestSimulationPausing {

	private static final int REAL_TIME_ALLOWANCE = 100;

	static long tBefore;

	Simulation sim;

	public static void sleepOneSecond() {
		System.out.print((System.currentTimeMillis() - tBefore) + "\twaiting 1s ... ");
		sleep(1000);
		System.out.println("done.");
	}

	public static void sleep(long duration) {
		try {
			// imitate some computationally expensive action
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Before
	public void before() {
		// set up a simulation that runs about 3 seconds real time with simulation time
		// ranging from 0 to 23.
		sim = new Simulation();
		sim.setPrintLevel(MsgCategory.ALL);
		sim.addPrintListener(System.out::println);

		sim.scheduleAt("event1", 2.0, SimEvent.EVENT_PRIO_NORMAL, TestSimulationPausing::sleepOneSecond);
		sim.scheduleAt("event2", 4.0, SimEvent.EVENT_PRIO_NORMAL, TestSimulationPausing::sleepOneSecond);
		sim.scheduleAt("event3", 6.0, SimEvent.EVENT_PRIO_NORMAL, TestSimulationPausing::sleepOneSecond);

		sim.setSimulationLength(23.0);

		tBefore = System.currentTimeMillis();
	}

	@Test(timeout = 4000)
	public void testDefaultBehaviour() throws Exception {
		long t = System.currentTimeMillis();

		Future<Map<String, Object>> future = sim.performRunAsync();

		sleep(500);
		assertEquals("simTime", 2.0, sim.simTime(), 1e-6);
		assertEquals("simState", SimExecState.RUNNING, sim.state());

		Map<String, Object> res = future.get();

		t = System.currentTimeMillis() - t;

		ConsolePrinter.printResults(null, res);

		assertEquals("simTime", 23.0, sim.simTime(), 1e-6);
		assertEquals("simState", SimExecState.FINISHED, sim.state());

		long timeDiff = t - 3000;
		assertTrue("realTimeMin", timeDiff >= 0); // can't be smaller
		assertTrue("realTimeMax", timeDiff < REAL_TIME_ALLOWANCE); // at most 100ms more
	}

	@Test(timeout = 6000)
	public void testPausing() throws Exception {
		long t = System.currentTimeMillis();

		Future<Map<String, Object>> future = sim.performRunAsync();

		sleep(500);
		assertEquals("simState", SimExecState.RUNNING, sim.state());
		assertEquals("simTime for first event", 2.0, sim.simTime(), 1e-6);
		sim.pause(); // request simulation pause after current event was processed

		sleep(3000);
		assertEquals("simState", SimExecState.PAUSED, sim.state());
		sim.unpause(); // continue execution after 3 seconds

		Map<String, Object> res = future.get(); // wait for completion
		assertEquals("simTime", 23.0, sim.simTime(), 1e-6);

		t = System.currentTimeMillis() - t;

		ConsolePrinter.printResults(null, res);

		long timeDiff = t - (500 + 3000 + 2000);
		assertTrue("realTimeMin", timeDiff >= 0); // can't be smaller
		assertTrue("realTimeMax", timeDiff < REAL_TIME_ALLOWANCE); // at most 100ms more
	}

	@Test(timeout = 4000)
	public void testStartPaused() throws Exception {
		sim.pause();

		long t = System.currentTimeMillis();

		Future<Map<String, Object>> future = sim.performRunAsync();

		sleep(500);
		assertEquals("simState", SimExecState.PAUSED, sim.state());
		assertEquals("simTime should be initial", 0.0, sim.simTime(), 1e-6);
		sim.unpause(); // request simulation start

		Map<String, Object> res = future.get(); // wait for completion
		assertEquals("simTime", 23.0, sim.simTime(), 1e-6);

		t = System.currentTimeMillis() - t;

		ConsolePrinter.printResults(null, res);

		long timeDiff = t - (500 + 3000);
		assertTrue("realTimeMin", timeDiff >= 0); // can't be smaller
		assertTrue("realTimeMax", timeDiff < REAL_TIME_ALLOWANCE); // at most 100ms more
	}

	@Test(timeout = 7000)
	public void testStartPauseTwice() throws Exception {
		sim.pause();

		long t = System.currentTimeMillis();

		Future<Map<String, Object>> future = sim.performRunAsync();

		sleep(500);
		assertEquals("simState", SimExecState.PAUSED, sim.state());
		assertEquals("simTime should be initial", 0.0, sim.simTime(), 1e-6);
		sim.unpause(); // start execution

		sleep(500);
		assertEquals("simState", SimExecState.RUNNING, sim.state());
		assertEquals("simTime of first event", 2.0, sim.simTime(), 1e-6); // simulation should still be busy processing
																			// the first event
		sim.pause(); // request simulation pause after current event was processed

		sleep(3000);
		assertEquals("simState", SimExecState.PAUSED, sim.state());
		sim.unpause(); // continue execution after 3 seconds

		Map<String, Object> res = future.get(); // wait for completion
		assertEquals("simTime", 23.0, sim.simTime(), 1e-6);

		t = System.currentTimeMillis() - t;

		ConsolePrinter.printResults(null, res);

		long timeDiff = t - (500 + 500 + 3000 + 2000);
		assertTrue("realTimeMin, diff=" + timeDiff, timeDiff >= 0); // can't be smaller
		assertTrue("realTimeMax, diff=" + timeDiff, timeDiff < REAL_TIME_ALLOWANCE); // at most 100ms more
	}

	@Test(timeout = 5000)
	public void testStartPauseDouble() throws Exception {
		sim.pause();
		sim.pause();

		long t = System.currentTimeMillis();

		Future<Map<String, Object>> future = sim.performRunAsync();

		sleep(500);
		assertEquals("simState", SimExecState.PAUSED, sim.state());
		assertEquals("simTime should be initial", 0.0, sim.simTime(), 1e-6);
		sim.unpause(); // simulation should still be paused until the second unpause

		sleep(500);
		assertEquals("simState", SimExecState.PAUSED, sim.state());
		assertEquals("simTime should be initial", 0.0, sim.simTime(), 1e-6);
		sim.unpause();

		Map<String, Object> res = future.get(); // wait for completion
		assertEquals("simTime", 23.0, sim.simTime(), 1e-6);

		t = System.currentTimeMillis() - t;

		ConsolePrinter.printResults(null, res);

		long timeDiff = t - (500 + 500 + 3000);
		assertTrue("realTimeMin, diff=" + timeDiff, timeDiff >= 0); // can't be smaller
		assertTrue("realTimeMax, diff=" + timeDiff, timeDiff < REAL_TIME_ALLOWANCE); // at most 100ms more
	}

	@Test(timeout = 4000)
	public void testUnpauseWhileWaitingForPause1() throws Exception {
		long t = System.currentTimeMillis();

		Future<Map<String, Object>> future = sim.performRunAsync();

		sleep(300);
		assertEquals("simState", SimExecState.RUNNING, sim.state());
		assertEquals("simTime should be initial", 2.0, sim.simTime(), 1e-6);
		sim.pause();

		sleep(300);
		// simulation should still be busy with first event
		assertEquals("simState", SimExecState.RUNNING, sim.state());
		assertEquals("simTime should be initial", 2.0, sim.simTime(), 1e-6);

		sim.unpause(); // "cancel" pause

		Map<String, Object> res = future.get(); // wait for completion
		assertEquals("simState", SimExecState.FINISHED, sim.state());
		assertEquals("simTime", 23.0, sim.simTime(), 1e-6);

		t = System.currentTimeMillis() - t;

		ConsolePrinter.printResults(null, res);

		long timeDiff = t - (3000);
		assertTrue("realTimeMin, diff=" + timeDiff, timeDiff >= 0); // can't be smaller
		assertTrue("realTimeMax, diff=" + timeDiff, timeDiff < REAL_TIME_ALLOWANCE); // at most 100ms more
	}

	@Test(timeout = 4000)
	public void testUnpauseWhileWaitingForPause2() throws Exception {
		long t = System.currentTimeMillis();

		Future<Map<String, Object>> future = sim.performRunAsync();

		sleep(300);
		assertEquals("simState", SimExecState.RUNNING, sim.state());
		assertEquals("simTime should be initial", 2.0, sim.simTime(), 1e-6);
		sim.pause();

		sleep(300);
		// simulation should still be busy with first event
		assertEquals("simState", SimExecState.RUNNING, sim.state());
		assertEquals("simTime should be initial", 2.0, sim.simTime(), 1e-6);
		sim.unpause(); // "cancel" pause

		sleep(200);
		// pause again while still processing first event
		assertEquals("simState", SimExecState.RUNNING, sim.state());
		assertEquals("simTime should be initial", 2.0, sim.simTime(), 1e-6);
		sim.pause();

		sleep(500);
		// simulation should now be paused
		assertEquals("simState", SimExecState.PAUSED, sim.state());
		assertEquals("simTime should be initial", 2.0, sim.simTime(), 1e-6);
		sim.unpause();

		Map<String, Object> res = future.get(); // wait for completion
		assertEquals("simState", SimExecState.FINISHED, sim.state());
		assertEquals("simTime", 23.0, sim.simTime(), 1e-6);

		t = System.currentTimeMillis() - t;

		ConsolePrinter.printResults(null, res);

		long timeDiff = t - (300 + 3000);
		assertTrue("realTimeMin, diff=" + timeDiff, timeDiff >= 0); // can't be smaller
		assertTrue("realTimeMax, diff=" + timeDiff, timeDiff < REAL_TIME_ALLOWANCE); // at most 100ms more
	}

	@Test//(timeout = 2000)
	public void testPausedSimulationCanBeEnded() throws Exception {
		Future<Map<String, Object>> future = sim.performRunAsync();

		long t = System.currentTimeMillis();

		sleep(500);
		assertEquals("simState", SimExecState.RUNNING, sim.state());
		assertEquals("simTime for first event", 2.0, sim.simTime(), 1e-6);
		sim.pause(); // request simulation pause after current event was processed

		sleep(1000);
		assertEquals("simState", SimExecState.PAUSED, sim.state());
		assertEquals("simTime for first event", 2.0, sim.simTime(), 1e-6);

		sim.end();

		System.out.println("waiting for simulation to end...");
		Map<String, Object> res = future.get(); // wait for completion
		System.out.println("simulation ended.");
		
		assertEquals("simTime", 2.0, sim.simTime(), 1e-6);
		assertEquals("simState", SimExecState.FINISHED, sim.state());

		t = System.currentTimeMillis() - t;

		ConsolePrinter.printResults(null, res);

		long timeDiff = t - (500 + 1000);
		assertTrue("realTimeMin, diff=" + timeDiff, timeDiff >= 0); // can't be smaller
		assertTrue("realTimeMax, diff=" + timeDiff, timeDiff < REAL_TIME_ALLOWANCE); // at most 100ms more
	}

	@Test(timeout = 3000)
	public void testRunningSimulationCanBeEnded() throws Exception {
		long t = System.currentTimeMillis();

		Future<Map<String, Object>> future = sim.performRunAsync();

		sleep(1500);
		assertEquals("simState", SimExecState.RUNNING, sim.state());
		assertEquals("simTime for first event", 4.0, sim.simTime(), 1e-6);

		sim.end();

		Map<String, Object> res = future.get(); // wait for completion
		assertEquals("simTime", 4.0, sim.simTime(), 1e-6);
		assertEquals("simState", SimExecState.FINISHED, sim.state());

		t = System.currentTimeMillis() - t;

		ConsolePrinter.printResults(null, res);

		long timeDiff = t - (2000);
		assertTrue("realTimeMin, diff=" + timeDiff, timeDiff >= 0); // can't be smaller
		assertTrue("realTimeMax, diff=" + timeDiff, timeDiff < REAL_TIME_ALLOWANCE); // at most 100ms more
	}

//	private Map<String, Object> getFutureResult(Future<Map<String, Object>> future) {
//		try {
//			return future.get(); // blocks until result is available
//		} catch (InterruptedException | ExecutionException e) {
//			throw new RuntimeException(e);
//		}
//	}

}
