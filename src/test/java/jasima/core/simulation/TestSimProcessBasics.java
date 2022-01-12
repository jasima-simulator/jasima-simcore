package jasima.core.simulation;

import static jasima.core.simulation.SimContext.activate;
import static jasima.core.simulation.SimContext.activateCallable;
import static jasima.core.simulation.SimContext.activateEntity;
import static jasima.core.simulation.SimContext.end;
import static jasima.core.simulation.SimContext.suspend;
import static jasima.core.simulation.SimContext.trace;
import static jasima.core.simulation.SimContext.waitCondition;
import static jasima.core.simulation.SimContext.waitFor;
import static jasima.core.util.observer.ObservableValues.isEqual;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;

import jasima.core.simulation.SimProcess.MightBlock;
import jasima.core.simulation.SimProcess.ProcessState;
import jasima.core.simulation.Simulation.SimulationFailed;
import jasima.core.util.ConsolePrinter;
import jasima.core.util.MsgCategory;
import jasima.core.util.SimProcessUtil;
import jasima.core.util.observer.ObservableValue;

public class TestSimProcessBasics {

	@Rule
	public Timeout globalTimeout = new Timeout(60000);

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void testGeneratorMethod() {
		Map<String, Object> res = Simulation.of(sim -> {
			sim.activate("generator", TestSimProcessBasics::generatorProcess);
		});
		assertEquals("simTime", 6.6, (Double) res.get("simTime"), 1e-6);
	}

	@Test
	public void testBasicExecution() {
		Map<String, Object> res = Simulation.of(sim -> {
			sim.activate("p1", () -> TestSimProcessBasics.simpleProcess("p1"));
		});
		assertEquals("simTime", 3.6, (Double) res.get("simTime"), 1e-6);
	}

	@Test
	public void testSuspendResumeByEvent() {
		Map<String, Object> res = Simulation.of(sim -> {
			SimProcess<Void> p = sim.activate(TestSimProcessBasics::suspendingProcess);
			sim.scheduleAt(2.0, SimEvent.EVENT_PRIO_NORMAL, () -> p.resume());
		});
		assertEquals("simTime", 3.0, (Double) res.get("simTime"), 1e-6);
	}

	@Test
	public void testSuspendResumeByProcess() {
		Map<String, Object> res = Simulation.of(sim -> {
			SimProcess<Void> p = sim.activate(TestSimProcessBasics::suspendingProcess);

			sim.activate(() -> {
				SimContext.waitFor(2.0);
				p.resume();
			});
		});
		assertEquals("simTime", 3.0, (Double) res.get("simTime"), 1e-6);
	}

	@Test
	public void testLambdaProcess() {
		Map<String, Object> res = Simulation.of(sim -> {
			sim.activate(() -> {
				SimContext.waitFor(2.0);
			});
		});
		assertEquals("simTime", 2.0, (Double) res.get("simTime"), 1e-6);
	}

	@Test
	public void testWaitCondition() {
		Map<String, Object> res = Simulation.of(sim -> {
			// the value we want to wait upon later
			ObservableValue<Integer> myValue = new ObservableValue<>(0);

			// some (parallel) process that eventually changes "myValue"
			activateEntity(new SimEntity() {
				@Override
				protected void lifecycle() throws MightBlock {
					waitFor(23.0);
					myValue.set(42);
				}
			});

			// main process waits until myValue is 42
			waitCondition(isEqual(myValue, 42));

			// after the wait: add myValue to the simulation results
			sim.addResult("myValue", myValue.get());
		});
		ConsolePrinter.printResults(res);

		assertEquals("simTime", 23.0, (Double) res.get("simTime"), 1e-6);
		assertEquals("myValue", 42, res.get("myValue"));
	}

	@Test
	public void testProcessJoin() {
		AtomicInteger processResult = new AtomicInteger(0);

		Map<String, Object> res = Simulation.of(sim -> {
			SimProcess<Integer> p1 = sim.activateCallable(() -> {
				waitFor(1.0);

				SimProcess<Integer> p2 = sim.activateCallable(() -> {
					SimProcess<?> p = SimContext.currentProcess();
					p.waitFor(5.0);
					return 42;
				});

				p2.join();

				int procRes = p2.get();
				assertEquals(42, procRes);

				waitFor(1.0);

				return 23;
			});

			processResult.set(p1.join().get());
		});
		assertEquals(23, processResult.get());
		assertEquals("simTime", 7.0, (Double) res.get("simTime"), 1e-6);
	}

	@Test
	public void testGetFromUnfinishedProcessShouldRaiseException() {
		expectedException.expect(SimulationFailed.class);
		expectedException.expectCause(isA(IllegalStateException.class));

		Simulation.of(sim -> {
			SimProcess<?> p1 = sim.activate(() -> {
				waitFor(23.0);
			});
			p1.get(); // triggers an IllegalStateException
		});

		fail("should never be reached");
	}

	@Test
	public void testJoinTerminatedProcess() {
		Map<String, Object> res = Simulation.of(sim -> {
			SimProcess<String> p2 = sim.activateCallable(() -> {
				waitFor(5.0);
				return "test";
			});

			waitFor(4);

			assertEquals(ProcessState.SCHEDULED, p2.processState());

			waitFor(2);

			assertEquals(ProcessState.TERMINATED, p2.processState());
			assertEquals("test", p2.get());

			waitFor(1);
		});

		assertEquals("simTime", 7.0, (Double) res.get("simTime"), 1e-6);
	}

	@Test
	public void testSuspendedProcessesAreClearedAfterExecution() throws Exception {
		AtomicReference<Simulation> simRef = new AtomicReference<>(null);
		Map<String, Object> res = SimContext.simulationOf("simulation1", sim -> {
			simRef.set(sim);

			for (int i = 0; i < 10; i++) {
				activate("process" + i, TestSimProcessBasics::suspendingProcess2);
			}
			System.out.println("MAIN suspending");
			suspend();
			fail("Mustn't be reached");
		});
		assertEquals("all processes suspended at time 1", 1.0, (Double) res.get("simTime"), 1e-6);
		assertEquals("unfinished processes should be accessible after run", 11, simRef.get().numRunnableProcesses());
		assertEquals("all Threads were cleared", 0,
				simRef.get().runnableProcesses().stream().filter(p -> p.executor != null).count());
	}

	@Test
	public void testEndOfUnactivatedProcess() throws Exception {
		AtomicBoolean lifecycleFinished = new AtomicBoolean(false);
		AtomicReference<Simulation> simRef = new AtomicReference<>(null);
		Map<String, Object> res = SimContext.simulationOf("simulation1", sim -> {
			simRef.set(sim);

			activate("someProcess", () -> {
				fail("Won't be reached");
			});
			end();

			lifecycleFinished.set(true);
		});
		assertTrue("lifecycle finished", lifecycleFinished.get());

		assertEquals("sim finished at time 0.0", 0.0, (Double) res.get("simTime"), 1e-6);
		assertEquals("unfinished processes should be accessible after run", 1, simRef.get().numRunnableProcesses());
		assertEquals("all Threads were cleared", 0,
				simRef.get().runnableProcesses().stream().filter(p -> p.executor != null).count());
	}

	@Test
	public void testMainSuspended() throws Exception {
		Map<String, Object> res = SimContext.simulationOf("simulation1", sim -> {
			waitFor(1.0);
			suspend();
			fail("Mustn't be reached");
		});
		assertEquals("main process suspended at time 1", 1.0, (Double) res.get("simTime"), 1e-6);
	}

	@Test
	public void testThreadReuse() throws Exception {
		AtomicReference<Thread> t = new AtomicReference<>(null);

		SimContext.simulationOf("simulation1", sim -> {
			for (int i = 0; i < 20; i++) {
				// start process in parallel to main process
				activate("process" + i, () -> {
					if (t.get() == null) {
						// everything has to be executed in this thread
						t.set(Thread.currentThread());
					} else {
						assertTrue(Thread.currentThread() == t.get());
					}
					waitFor(0.5);
				});

				// wait for its end in simulation time
				waitFor(1.0);

				// sleep for some wall time so thread can go to thread-pool again
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		});
		// test finished; everything ok, if this point is reached
	}

	@Test
	public void testProcessNames() throws MightBlock {
		Simulation sim = createSim();
		sim.setName("theSimulation");

		SimComponentContainerBase cs = new SimComponentContainerBase("a");
		cs.addChild(new SimEntity("b") {
			@Override
			protected void lifecycle() throws MightBlock {
				checkProcessName(".a.b.lifecycle");
			}
		});
		cs.addChild(new SimEntity("c") {
			@Override
			protected void lifecycle() throws MightBlock {
				checkProcessName(".a.c.lifecycle");
			}
		});

		sim.addComponent(cs);

		sim.setMainProcessActions(() -> {
			checkProcessName("theSimulation.simMain");

			activate("process", () -> {
				checkProcessName("process");
			});
		});

		sim.performRun();
	}

	private Simulation createSim() {
		Simulation sim = new Simulation();
		sim.setPrintLevel(MsgCategory.ALL);
		sim.addPrintListener(System.out::println);
		return sim;
	}

	private void checkProcessName(String expected) {
		assertEquals(expected, SimContext.currentProcess().getName());
	}

	@Test
	public void testRuntimeExceptionInChild() throws MightBlock {
		expectedException.expect(SimulationFailed.class);
		expectedException.expectCause(isA(RuntimeException.class));

		SimContext.simulationOf("theSimulation", sim -> {
			activate("process", () -> {
				waitFor(1.0);
				throw new RuntimeException("something went wrong");
			});
		});
	}

	@Test
	public void testRuntimeExceptionInChildWhileJoining() throws MightBlock {
		expectedException.expect(SimulationFailed.class);
		expectedException.expectCause(isA(RuntimeException.class));

		AtomicReference<Simulation> simRef = new AtomicReference<>(null);
		SimContext.simulationOf("theSimulation", sim -> {
			simRef.set(sim);
			SimProcess<Void> p1 = activate("process1", () -> {
				waitFor(1.0);
				throw new RuntimeException("something went wrong");
			});
			activate("process2", () -> {
				p1.join();
				fail("should never be reached");
			});
		});
		assertEquals("unfinished processes should be accessible after run", 1 + 2, simRef.get().numRunnableProcesses());
		assertEquals("all Threads were cleared", 0,
				simRef.get().runnableProcesses().stream().filter(p -> p.executor != null).count());
	}

	@Test
	public void testRuntimeExceptionInMainProcess() throws MightBlock {
		expectedException.expect(SimulationFailed.class);
		expectedException.expectCause(isA(RuntimeException.class));
		SimContext.simulationOf("theSimulation", sim -> {
			waitFor(1.0);
			throw new RuntimeException("something went wrong");
		});
	}

	@Test
	public void testRuntimeExceptionInEvent() throws MightBlock {
		expectedException.expect(SimulationFailed.class);
		expectedException.expectCause(isA(RuntimeException.class));
		SimContext.simulationOf("theSimulation", sim -> {
			sim.scheduleAt(0.5, 0, () -> {
				throw new RuntimeException("something went wrong");
			});
			waitFor(1.0);
			fail();
		});
	}

	@Test
	public void testProcessChain() throws MightBlock {
		int N = 500;
		long t = System.currentTimeMillis();

		SimContext.simulationOf("daSim", sim -> {
			SimProcess<Void> p = activate("process", () -> {
				waitFor(1.0);
			});
			for (int n = 0; n < N; n++) {
				SimProcess<Void> pOld = p;
				p = activate("process" + n, () -> {
					pOld.join();
					waitFor(1.0);
				});
			}
			p.join();
			assertEquals("main process finished", 1.0 + N, sim.simTime(), 1e-6);
		});

		System.out.println("  threads created: " + SimProcessUtil.numThreadsCreated());
		t = (System.currentTimeMillis() - t);
		System.out.println("  time [ms] " + t);
	}

	public static void suspendingProcess() throws MightBlock {
		waitFor(1.0);
		suspend();
		waitFor(1.0);
	}

	public static void suspendingProcess2(Simulation sim) throws MightBlock {
		SimProcess<?> simProcess = sim.currentProcess();

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
			new SimProcess<>(sim, () -> simpleProcess(name), name).awakeIn(0.0);
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

	@Test
	public void testManyProcesses() throws Exception {
//		fibTest(20);
		for (int n = 1; n <= 25; n++) {
			System.out.println("starting fibTest(" + n + ")...");
			fibTest(n);
			System.out.println("  threads created: " + SimProcessUtil.numThreadsCreated());
			System.out.println();
		}
		System.out.println("all done.");
	}

	private void fibTest(int n) {
		long t = System.currentTimeMillis();
		numWaits = numProcesses = 0;
		int i = n;
		Map<String, Object> res = SimContext.simulationOf("sim" + n, sim -> {
			System.out.println(Thread.currentThread().getName());
			SimProcess<Integer> fibProcess = activateCallable("fib(" + i + ")", s -> fibonacci("fib" + i, i));
			fibProcess.join();
			System.out.println("done with fib(" + i + "). Result is: " + fibProcess.get());
		});
		t = (System.currentTimeMillis() - t);
		System.err.println(i + "\tnumWaits: " + numWaits + "\tnumProcesses: " + numProcesses + "\tsimTime: "
				+ res.get("simTime") + "\truntime: " + t + "ms");
	}

	public int fibonacci(String ctx, int n) throws MightBlock {
		numProcesses++;

		int res;
		if (n == 1 || n == 2) {
			waitFor(1.0);
			numWaits++;
			res = 1;
		} else {
			String ctx1 = ctx + "-" + (n - 1);
			SimProcess<Integer> s1Calc = activateCallable(ctx1, () -> fibonacci(ctx1, n - 1));
			s1Calc.join(); // wait until finished

			trace("first join");

			String ctx2 = ctx + "-" + (n - 2);
			SimProcess<Integer> s2Calc = activateCallable(ctx2, () -> fibonacci(ctx2, n - 2));
			s2Calc.join(); // wait until finished

			trace("second join");

			res = s1Calc.get() + s2Calc.get();
		}

		return res;
	}

	@Test
	public void testManyProcesses2() throws Exception {
		numWaits = numProcesses = 0;

		AtomicLong procRes = new AtomicLong();
		long t = System.currentTimeMillis();
		Map<String, Object> res = SimContext.simulationOf(sim -> {
			SimProcess<Long> ackProcess = activateCallable(s -> ack(3, 7));
			ackProcess.join();
			Long l = ackProcess.get();
			procRes.set(l);
			System.out.println("done. Result is: " + l + "; simevents: " + sim.numEventsProcessed());
		});
		t = (System.currentTimeMillis() - t);
		System.err.println("\twaits: " + numWaits + "\tprocesses: " + numProcesses + "\tres: " + procRes.get()
				+ "\ttMax: " + res.get("simTime") + "\t" + t + "ms" + "\tdepth: " + dMax);
		System.out.println("  threads created: " + SimProcessUtil.numThreadsCreated());
	}

	static int d;
	static int dMax;

	private long ackermann(int m, long n) throws MightBlock {
		numProcesses++;
		long res;

		if (++d > dMax) {
			dMax = d;
		}

		if (m == 0) {
			res = n + 1;
			waitFor(res);
			numWaits++;
		} else if (n == 0) {
			res = activateCallable(() -> ackermann(m - 1, 1)).join().get();
		} else {
			long tmp1 = activateCallable(() -> ackermann(m, n - 1)).join().get();
			res = activateCallable(() -> ackermann(m - 1, tmp1)).join().get();
		}

		--d;

		return res;
	}

	private long ack(int n, long m) throws MightBlock {
		numProcesses++;
		if (++d > dMax) {
			dMax = d;
		}
		while (n != 0) {
			if (m == 0) {
				m = 1;
			} else {
				int n2 = n;
				long m2 = m;
				m = activateCallable(() -> ack(n2, m2 - 1)).join().get();
			}
			n = n - 1;
		}
		--d;
		waitFor(m + 1);
		numWaits++;
		return m + 1;
	}

	static Logger log = LogManager.getLogger(TestSimProcessBasics.class);

	public static void main(String... args) throws Exception {
		Map<String, Object> res = SimContext.simulationOf(sim -> {
			log.warn("Hello world.");
			waitFor(1);
		});
		Map<String, Object> res2 = SimContext.simulationOf(() -> {
			log.warn("Hello world.");
			waitFor(1);
		});
	}

}
