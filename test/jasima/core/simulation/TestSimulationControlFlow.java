package jasima.core.simulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import jasima.core.simulation.Simulation.SimExecState;
import jasima.core.simulation.Simulation.SimulationFailed;

public class TestSimulationControlFlow {

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

		sim.init();
		sim.beforeRun();
		sim.run();

		assertEquals("simTime", 3.0, sim.simTime(), 1e-6);
		assertEquals("simState", SimExecState.FINISHED, sim.state());
		assertEquals("count", 2, count);

		sim.run(); // should not be possible
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

}
