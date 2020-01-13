package examples.processes;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import jasima.core.simulation.SimEvent;
import jasima.core.simulation.Simulation;
import jasima.core.simulation.processes.SimContext;
import jasima.core.simulation.processes.SimProcess;
import jasima.core.simulation.processes.SimProcess.ProcessState;
import jasima.core.util.MsgCategory;

public class SimProcessTest {

//	@Rule
//	public Timeout globalTimeout = Timeout.seconds(1); // 1 seconds max per method tested

	private Simulation sim;

	@Before
	public void setUp() throws Exception {
		sim = new Simulation();
		sim.setPrintLevel(MsgCategory.ALL);
		sim.addPrintListener(System.out::println);
	}

	@Test
	public void testGeneratorMethod() {
		new SimProcess<>(sim, SimProcessTest::generatorProcess);
		sim.performRun();
		assertEquals(6.6, sim.simTime(), 1e-6);
	}

	@Test
	public void testBasicExecution() {
		new SimProcess<>(sim, () -> SimProcessTest.simpleProcess("p1"));
		sim.performRun();

		assertEquals(3.6, sim.simTime(), 1e-6);
	}

	@Test
	public void testSuspendResumeByEvent() {
		SimProcess<Void> p = new SimProcess<>(sim, () -> SimProcessTest.suspendingProcess());
		sim.schedule(2.0, SimEvent.EVENT_PRIO_NORMAL, () -> p.resume());

		sim.performRun();
		assertEquals(3.0, sim.simTime(), 1e-6);
	}

	@Test
	public void testSuspendResumeByProcess() {
		SimProcess<Void> p = new SimProcess<>(sim, () -> SimProcessTest.suspendingProcess());
		new SimProcess<>(sim, () -> {
			SimProcess<?> simProcess = SimProcess.current();
			awakeIn(2.0);
			resume(p);
		});

		sim.performRun();
		assertEquals(3.0, sim.simTime(), 1e-6);
	}

	@Test
	public void testLambdaProcess() {
		new SimProcess<>(sim, () -> {
			SimProcess<?> simProcess = SimProcess.current();
			simProcess.awakeIn(2.0);
		});

		sim.performRun();
		assertEquals(2.0, sim.simTime(), 1e-6);
	}

	@Test
	public void testProcessJoin() {
		SimProcess<Integer> p1 = new SimProcess<>(sim, () -> {
			SimProcess<?> simProcess = SimProcess.current();
			
			simProcess.awakeIn(1.0);
			
			SimProcess<Integer> p2 = new SimProcess<>(sim, () -> {
				SimProcess<?> p = SimProcess.current();
				p.awakeIn(5.0);
				
				return 42;
			});
			
			int procRes = simProcess.join(p2);
			assertEquals(42, procRes);

			simProcess.awakeIn(1.0);
			
			return 23;
		});

		sim.performRun();

		assertEquals(23, p1.join(p1).intValue());	
		assertEquals(7.0, sim.simTime(), 1e-6);
	}

	@Test
	public void testJoinTerminatedProcess() {
		new SimProcess<>(sim, () -> {
			SimProcess<?> simProcess = SimProcess.current();
			
			SimProcess<String> p2 = new SimProcess<>(sim, () -> {
				SimProcess<?> p = SimProcess.current();
				p.awakeIn(5.0);
				
				return "test";
			});
			
			simProcess.awakeIn(4);
			
			assertEquals(ProcessState.SCHEDULED, p2.processState());
			
			simProcess.awakeIn(2);
			
			assertEquals(ProcessState.TERMINATED, p2.processState());
			assertEquals("test", simProcess.join(p2));

			simProcess.awakeIn(1);
		});

		sim.performRun();
		
		assertEquals(7.0, sim.simTime(), 1e-6);
	}

	public void suspendingProcess() {
		SimProcess<?> simProcess = requireNonNull(sim.currentProcess());
		assertEquals(simProcess, SimContext.currentProcess());

		simProcess.awakeIn(1.0);
		simProcess.suspend();
		simProcess.awakeIn(1.0);
	}

	public static void generatorProcess() {
		SimProcess<?> simProcess = SimContext.currentProcess();
		Simulation sim = simProcess.getSim();

		for (int i = 0; i < 3; i++) {
			simProcess.awakeIn(1.0);
			sim.trace("generator", i);

			String name = "sub" + i;
			new SimProcess<>(sim, () -> ProcTest.xxx2(name));
		}
	}

	public static void simpleProcess(String name) {
		SimProcess<?> simProcess = SimProcess.current();
		Simulation sim = simProcess.getSim();

		simProcess.awakeIn(1.2);
		sim.trace(name, "1");
		simProcess.awakeIn(1.2);
		sim.trace(name, "2");
		simProcess.awakeIn(1.2);
		sim.trace(name, "3");
	}

}
