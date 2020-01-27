package jasima.core.simulation;

import static jasima.core.simulation.SimContext.activate;
import static jasima.core.simulation.SimContext.suspend;
import static jasima.core.simulation.SimContext.waitFor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Ignore;
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
		new SimProcess<>(sim, TestSimProcessBasics::generatorProcess, "generator").awakeIn(0.0);
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
				waitFor(5.0);
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

	@Test
	public void testSuspendedProcessesAreClearedAfterExecution() throws Exception {
		AtomicReference<Simulation> sim = new AtomicReference<>(null);
		Map<String, Object> res = SimContext.of("simulation1", () -> {
			sim.set(SimContext.currentSimulation());

			for (int i = 0; i < 10; i++) {
				activate(TestSimProcessBasics::suspendingProcess, "process" + i);
			}
			System.out.println("MAIN suspending");
			suspend();
			fail("Mustn't be reached");
		});

		Thread.sleep(200);

		assertEquals("all processes suspended at time 1", 1.0, (Double) res.get("simTime"), 1e-6);
		assertEquals("unfinished processes should be accessible after tun", 11, sim.get().numRunnableProcesses());
		assertEquals("all Threads were cleared", 0,
				sim.get().runnableProcesses().stream().filter(p -> p.executor != null).count());
	}

	@Test
	public void testThreadReuse() throws Exception {
		AtomicReference<Thread> t = new AtomicReference<>(null);

		SimContext.of("simulation1", sim -> {
			for (int i = 0; i < 10; i++) {
				// start process 
				activate(() -> {
					if (t.get() == null) {
						// everything has to be executed in this thread
						t.set(Thread.currentThread());
					} else {
						assertTrue(Thread.currentThread() == t.get());
					}
					waitFor(0.5);
				}, "process" + i).join();

				// wait for it's end in simulation time
				waitFor(1.0);

				// sleep for some wall time so thread can go to thread-pool again
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		});
		System.out.println("test finished");
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
			new SimProcess<>(sim, () -> TestSimProcessBasics.simpleProcess(name), name).awakeIn(0.0);
		}
	}

	public static void simpleProcess(String name) throws MightBlock {
		SimProcess<?> simProcess = SimContext.currentProcess();
		Simulation sim = simProcess.getSim();

		sim.trace(name, "0");
		simProcess.waitFor(1.2);
		sim.trace(name, "1");
		simProcess.waitFor(1.2);
		sim.trace(name, "2");
		simProcess.waitFor(1.2);
		sim.trace(name, "3");
	}

	int numWaits, numProcesses;

	@Test @Ignore
	public void testManyProcesses() throws Exception {
		for (int n = 10; n <= 20; n++) {
			long t = System.currentTimeMillis();
			numWaits = numProcesses = 0;
			int i = n;
			Map<String, Object> res = SimContext.of("simulation1", sim -> {
				System.out.println("starting process...");
				SimProcess<Integer> fibProcess = activate(() -> this.fibonacci(i));
				fibProcess.join();
				System.out.println("done. Result is: " + fibProcess.get());
			});
			t = (System.currentTimeMillis() - t);
			System.err.println(i + "\t" + numWaits + "\t" + numProcesses + "\t" + res.get("simTime") + "\t" + t + "ms");
		}
		System.out.println("all done.");
	}

	public int fibonacci(int n) throws MightBlock {
		numProcesses++;

		int res;
		if (n == 1 || n == 2) {
			waitFor(1.0);
			numWaits++;
			res = 1;
		} else {
			SimProcess<Integer> s1Calc = activate(() -> fibonacci(n - 1));
			s1Calc.join(); // wait until finished

			SimProcess<Integer> s2Calc = activate(() -> fibonacci(n - 2));
			s2Calc.join(); // wait until finished

			res = s1Calc.get() + s2Calc.get();
		}

		return res;
	}

}
