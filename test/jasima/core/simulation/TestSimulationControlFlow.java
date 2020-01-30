package jasima.core.simulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import jasima.core.simulation.Simulation.SimExecState;
import jasima.core.simulation.Simulation.SimulationFailed;
import junit.framework.Assert;

public class TestSimulationControlFlow {
	@Rule
	public Timeout globalTimeout = new Timeout(2000);

	Simulation sim;
	int count;

	public void incCounter() {
		System.out.println("incCounter() triggered");
		count++;
	}

	public void throwException() {
		System.out.println("throwException() triggered");
		throw new IllegalStateException("Something strange happened.");
	}

	@Before
	public void before() {
		sim = new Simulation();
		sim.addPrintListener(System.out::println);
	}

	@Test
	public void testStraightExecution() {
		sim.schedule("event1", 2.0, SimEvent.EVENT_PRIO_NORMAL, this::incCounter);
		sim.performRun();

		assertEquals("simTime", 2.0, sim.simTime(), 1e-6);
		assertEquals("simState", SimExecState.FINISHED, sim.state());
		assertEquals("count", 1, count);
	}

	@Test(expected = IllegalStateException.class)
	public void testCantRunTwice() {
		sim.schedule("event1", 2.0, SimEvent.EVENT_PRIO_NORMAL, this::incCounter);
		sim.schedule("event2", 3.0, SimEvent.EVENT_PRIO_NORMAL, this::incCounter);

		// normal execution first time
		sim.init();
		sim.beforeRun();
		sim.run();

		assertEquals("simTime", 3.0, sim.simTime(), 1e-6);
		assertEquals("simState", SimExecState.FINISHED, sim.state());
		assertEquals("count", 2, count);

		sim.run(); // should throw IllegalStateException
	}

	@Test
	public void testFixedRunLength() {
		sim.schedule("event1", 2.0, SimEvent.EVENT_PRIO_NORMAL, this::incCounter);
		sim.schedule("never executed", 24.0, SimEvent.EVENT_PRIO_NORMAL, this::incCounter);
		sim.setSimulationLength(23.0);

		sim.performRun();

		assertEquals("simTime", 23.0, sim.simTime(), 1e-6);
		assertEquals("simState", SimExecState.FINISHED, sim.state());
		assertEquals("count", 1, count);
	}

	@Test(expected = SimulationFailed.class)
	public void testException() {
		sim.schedule("incCounter", 2.0, SimEvent.EVENT_PRIO_NORMAL, this::incCounter);
		sim.schedule("throwException", 3.0, SimEvent.EVENT_PRIO_NORMAL, this::throwException);
		sim.schedule("not executed", 4.0, SimEvent.EVENT_PRIO_NORMAL, this::incCounter);

		try {
			sim.performRun();
		} catch (SimulationFailed sf) {
			assertTrue("exception cause", sf.getCause() != null && sf.getCause() instanceof IllegalStateException);
			throw sf;
		} finally {
			assertEquals("count", 1, count);
			assertEquals("simState", SimExecState.ERROR, sim.state());
			assertEquals("simTime", 3.0, sim.simTime(), 1e-6);
		}
	}

	@Test(expected = SimulationFailed.class)
	public void testHandledExceptionRethrow() {
		boolean[] wasCalled = { false };

		sim.setErrorHandler(e -> {
			wasCalled[0] = e instanceof IllegalStateException;
			return true;
		});

		sim.schedule("incCounter", 2.0, SimEvent.EVENT_PRIO_NORMAL, this::incCounter);
		sim.schedule("throwException", 3.0, SimEvent.EVENT_PRIO_NORMAL, this::throwException);
		sim.schedule("not executed", 4.0, SimEvent.EVENT_PRIO_NORMAL, this::incCounter);

		try {
			sim.performRun();
		} catch (SimulationFailed sf) {
			assertTrue("exception cause", sf.getCause() != null && sf.getCause() instanceof IllegalStateException);
			throw sf;
		} finally {
			assertTrue("handler called", wasCalled[0]);
			assertEquals("simTime", 3.0, sim.simTime(), 1e-6);
			assertEquals("count", 1, count);
			assertEquals("simState", SimExecState.ERROR, sim.state());
		}
	}

	@Test
	public void testRecoverableError() {
		boolean[] wasCalled = { false };

		sim.setErrorHandler(e -> {
			wasCalled[0] = e instanceof IllegalStateException;
			return false;
		});

		sim.schedule("incCounter1", 2.0, SimEvent.EVENT_PRIO_NORMAL, this::incCounter);
		sim.schedule("throwException", 3.0, SimEvent.EVENT_PRIO_NORMAL, this::throwException);
		sim.schedule("incCounter2", 4.0, SimEvent.EVENT_PRIO_NORMAL, this::incCounter);

		sim.performRun();

		assertTrue("handler called", wasCalled[0]);
		assertEquals("simTime", 4.0, sim.simTime(), 1e-6);
		assertEquals("simState", SimExecState.FINISHED, sim.state());
		assertEquals("count", 2, count);
	}

	@Test
	public void testRecoverableError2() {
		int[] wasCalled = { 0 };

		sim.setErrorHandler(e -> {
			if (e instanceof IllegalStateException) {
				wasCalled[0]++;
			}
			return false;
		});

		sim.schedule("incCounter1", 2.0, SimEvent.EVENT_PRIO_NORMAL, this::incCounter);
		sim.schedule("throwException", 3.0, SimEvent.EVENT_PRIO_NORMAL, this::throwException);
		sim.schedule("incCounter2", 4.0, SimEvent.EVENT_PRIO_NORMAL, this::incCounter);
		sim.schedule("throwException", 5.0, SimEvent.EVENT_PRIO_NORMAL, this::throwException);
		sim.schedule("incCounter2", 6.0, SimEvent.EVENT_PRIO_NORMAL, this::incCounter);

		sim.performRun();

		assertEquals("handler called", 2, wasCalled[0]);
		assertEquals("count", 3, count);
		assertEquals("simTime", 6.0, sim.simTime(), 1e-6);
		assertEquals("simState", SimExecState.FINISHED, sim.state());
	}

	@Test(expected = SimulationFailed.class)
	public void testErrorWithProcesses1() {
		boolean[] wasCalled = { false };

		Map<String, Object> res = SimContext.of(sim -> {
			sim.setErrorHandler(e -> {
				wasCalled[0] = e instanceof IllegalStateException;
				return true;
			});

			sim.schedule("incCounter1", 2.0, SimEvent.EVENT_PRIO_NORMAL, this::incCounter);
			sim.schedule("throwException", 3.0, SimEvent.EVENT_PRIO_NORMAL, this::throwException);
			sim.schedule("incCounter2", 4.0, SimEvent.EVENT_PRIO_NORMAL, this::incCounter);

			// main process is finished at that time
		});

		assertTrue("handler called", wasCalled[0]);
		assertEquals("simTime", 3.0, (Double) res.get("simTime"), 1e-6);
		assertEquals("count", 1, count);
	}

	@Test(expected = SimulationFailed.class)
	public void testErrorWithProcesses2() {
		boolean[] wasCalled = { false };

		Map<String, Object> res = SimContext.of(sim -> {
			sim.setErrorHandler(e -> {
				wasCalled[0] = e instanceof IllegalStateException;
				return true;
			});

			sim.schedule("incCounter1", 2.0, SimEvent.EVENT_PRIO_NORMAL, this::incCounter);
			sim.schedule("throwException", 3.0, SimEvent.EVENT_PRIO_NORMAL, this::throwException);
			sim.schedule("incCounter2", 4.0, SimEvent.EVENT_PRIO_NORMAL, Assert::fail);

			sim.currentProcess().suspend();
			fail("should never be reached");
		});

		assertTrue("handler called", wasCalled[0]);
		assertEquals("simTime", 3.0, (Double) res.get("simTime"), 1e-6);
		assertEquals("count", 1, count);
	}

	@Test(expected = SimulationFailed.class)
	public void testEventErrorWithProcess() {
		boolean[] wasCalled = { false };

		Map<String, Object> res = SimContext.of(sim -> {
			SimContext.activate(s -> {
				SimProcess<?> p = s.currentProcess();
				p.setLocalErrorHandler(e -> {
					Assert.fail("should never be called when event@3.0 throws the exception");
					return true;
				});
				p.waitFor(5.0); // assure process is still active when Exception is thrown
				fail(); // should never be reached
			});

			sim.setErrorHandler(e -> {
				wasCalled[0] = e instanceof IllegalStateException;
				return true;
			});

			sim.schedule("incCounter1", 2.0, SimEvent.EVENT_PRIO_NORMAL, this::incCounter);
			sim.schedule("throwException", 3.0, SimEvent.EVENT_PRIO_NORMAL, this::throwException);
			sim.schedule("incCounter2", 4.0, SimEvent.EVENT_PRIO_NORMAL, Assert::fail);

			// main process is finished at that time
		});

		assertTrue("handler called", wasCalled[0]);
		assertEquals("simTime", 3.0, (Double) res.get("simTime"), 1e-6);
		assertEquals("count", 1, count);
	}

	@Test(expected = SimulationFailed.class)
	public void testEventErrorWithFinishedProcess() {
		boolean[] wasCalled = { false };

		Map<String, Object> res = SimContext.of(sim -> {
			SimContext.activate(s -> {
				SimProcess<?> p = s.currentProcess();
				p.setLocalErrorHandler(e -> {
					Assert.fail("should never be called when event@3.0 throws the exception");
					return true;
				});
				// process will finish but still execute event loop after this point
			});

			sim.setErrorHandler(e -> {
				wasCalled[0] = e instanceof IllegalStateException;
				return true;
			});

			sim.schedule("incCounter1", 2.0, SimEvent.EVENT_PRIO_NORMAL, this::incCounter);
			sim.schedule("throwException", 3.0, SimEvent.EVENT_PRIO_NORMAL, this::throwException);
			sim.schedule("incCounter2", 4.0, SimEvent.EVENT_PRIO_NORMAL, Assert::fail);

			// main process is finished at that time
		});

		assertTrue("handler called", wasCalled[0]);
		assertEquals("simTime", 3.0, (Double) res.get("simTime"), 1e-6);
		assertEquals("count", 1, count);
	}

}
