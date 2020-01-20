package jasima.core.simulation;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import jasima.core.simulation.SimProcess.MightBlock;
import jasima.core.simulation.SimProcess.ProcessState;
import jasima.core.util.MsgCategory;

public class TestSimProcessBasics {

	private Simulation sim;

	@Before
	public void setUp() throws Exception {
		sim = new Simulation();
		sim.setPrintLevel(MsgCategory.ALL);
		sim.addPrintListener(System.out::println);
	}

	@Test
	public void testGeneratorMethod() {
		new SimProcess<>(sim, TestSimProcessBasics::generatorProcess).awakeIn(0.0);
		sim.performRun();
		assertEquals(6.6, sim.simTime(), 1e-6);
	}

	@Test
	public void testBasicExecution() {
		new SimProcess<>(sim, () -> TestSimProcessBasics.simpleProcess("p1")).awakeIn(0.0);
		sim.performRun();

		assertEquals(3.6, sim.simTime(), 1e-6);
	}

	@Test
	public void testSuspendResumeByEvent() {
		SimProcess<Void> p = new SimProcess<>(sim, () -> TestSimProcessBasics.suspendingProcess());
		p.awakeIn(0.0);
		sim.schedule(2.0, SimEvent.EVENT_PRIO_NORMAL, () -> p.resume());

		sim.performRun();
		assertEquals(3.0, sim.simTime(), 1e-6);
	}

	@Test
	public void testSuspendResumeByProcess() {
		SimProcess<Void> p = new SimProcess<>(sim, () -> TestSimProcessBasics.suspendingProcess());
		new SimProcess<>(sim, () -> {
			SimProcess<?> simProcess = SimContext.currentProcess();
			simProcess.waitFor(2.0);
			p.resume();
		}).awakeIn(0.0);

		sim.performRun();
		assertEquals(3.0, sim.simTime(), 1e-6);
	}

	@Test
	public void testLambdaProcess() {
		new SimProcess<>(sim, () -> {
			SimProcess<?> simProcess = SimContext.currentProcess();
			simProcess.waitFor(2.0);
		}).awakeIn(0.0);

		sim.performRun();
		assertEquals(2.0, sim.simTime(), 1e-6);
	}

	@Test
	public void testProcessJoin() {
		SimProcess<Integer> p1 = new SimProcess<>(sim, () -> {
			SimProcess<?> simProcess = SimContext.currentProcess();

			simProcess.waitFor(1.0);

			SimProcess<Integer> p2 = new SimProcess<>(sim, () -> {
				SimProcess<?> p = SimContext.currentProcess();
				p.waitFor(5.0);
				return 42;
			});
			p2.awakeIn(0.0);

			p2.join();
			int procRes = p2.get();
			assertEquals(42, procRes);

			simProcess.waitFor(1.0);

			return 23;
		});
		p1.awakeIn(0.0);

		sim.performRun();

		assertEquals(23, p1.get().intValue());
		assertEquals(7.0, sim.simTime(), 1e-6);
	}

	@Test
	public void testJoinTerminatedProcess() {
		new SimProcess<>(sim, () -> {
			SimProcess<?> simProcess = SimContext.currentProcess();

			SimProcess<String> p2 = new SimProcess<>(sim, () -> {
				SimContext.waitFor(5.0);
				return "test";
			});
			p2.awakeIn(0.0);

			simProcess.waitFor(4);

			assertEquals(ProcessState.SCHEDULED, p2.processState());

			simProcess.waitFor(2);

			assertEquals(ProcessState.TERMINATED, p2.processState());
			assertEquals("test", p2.get());

			simProcess.waitFor(1);
		}).awakeIn(0.0);

		sim.performRun();

		assertEquals(7.0, sim.simTime(), 1e-6);
	}

	public static void suspendingProcess() throws MightBlock {
		SimProcess<?> simProcess = SimContext.currentProcess();

		simProcess.waitFor(1.0);
		simProcess.suspend();
		simProcess.waitFor(1.0);
	}

	public static void generatorProcess() throws MightBlock {
		SimProcess<?> simProcess = SimContext.currentProcess();
		Simulation sim = simProcess.getSim();

		for (int i = 0; i < 3; i++) {
			simProcess.waitFor(1.0);
			sim.trace("generator", i);

			String name = "sub" + i;
			new SimProcess<>(sim, () -> TestSimProcessBasics.simpleProcess(name)).awakeIn(0.0);
			;
		}
	}

	public static void simpleProcess(String name) throws MightBlock {
		SimProcess<?> simProcess = SimContext.currentProcess();
		Simulation sim = simProcess.getSim();

		simProcess.waitFor(1.2);
		sim.trace(name, "1");
		simProcess.waitFor(1.2);
		sim.trace(name, "2");
		simProcess.waitFor(1.2);
		sim.trace(name, "3");
	}

}
