package jasima.core.simulation;

import static jasima.core.simulation.SimContext.activate;
import static jasima.core.simulation.SimContext.activateCallable;
import static jasima.core.simulation.SimContext.suspend;
import static jasima.core.simulation.SimContext.waitFor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import jasima.core.simulation.SimProcess.MightBlock;
import jasima.core.simulation.SimProcess.ProcessState;
import jasima.core.util.MsgCategory;

public class TestSimProcessBasics {
	@Rule
	public Timeout globalTimeout = new Timeout(2000);

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
		AtomicReference<Simulation> simRef = new AtomicReference<>(null);
		Map<String, Object> res = SimContext.of("simulation1", sim -> {
			simRef.set(sim);

			for (int i = 0; i < 10; i++) {
				activate(TestSimProcessBasics::suspendingProcess2, "process" + i);
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
	public void testMainSuspended() throws Exception {
		Map<String, Object> res = SimContext.of("simulation1", sim -> {
			waitFor(1.0);
			suspend();
			fail("Mustn't be reached");
		});
		assertEquals("main process suspended at time 1", 1.0, (Double) res.get("simTime"), 1e-6);
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
				}, "process" + i);

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
		// test finished, everything ok, if this point is reached
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

	@Test
	@Ignore
	public void testManyProcesses() throws Exception {
		for (int n = 10; n <= 20; n++) {
			long t = System.currentTimeMillis();
			numWaits = numProcesses = 0;
			int i = n;
			Map<String, Object> res = SimContext.of(sim -> {
				SimProcess<Integer> fibProcess = activateCallable(s -> fibonacci(i));
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
			SimProcess<Integer> s1Calc = activateCallable(() -> fibonacci(n - 1));
			s1Calc.join(); // wait until finished

			SimProcess<Integer> s2Calc = activateCallable(() -> fibonacci(n - 2));
			s2Calc.join(); // wait until finished

			res = s1Calc.get() + s2Calc.get();
		}

		return res;
	}

	static int w, p, j;

	public static int fib(int n) throws MightBlock {
		p++;

		int res;
		if (n == 1 || n == 2) {
			waitFor(1.0);
			w++;
			res = 1;
		} else {
			SimProcess<Integer> s1Calc = activateCallable(() -> fib(n - 1));
			j++;
			s1Calc.join(); // wait until finished

			SimProcess<Integer> s2Calc = activateCallable(() -> fib(n - 2));
			j++;
			s2Calc.join(); // wait until finished

			res = s1Calc.get() + s2Calc.get();
		}

		return res;
	}

	@Test
//	@Ignore
	public void testManyProcesses2() throws Exception {
		AtomicLong procRes = new AtomicLong();
		long t = System.currentTimeMillis();
		Map<String, Object> res = SimContext.of(sim -> {
			SimProcess<Long> fibProcess = activateCallable(s -> ack(3, 3));
			fibProcess.join();
			Long l = fibProcess.get();
			procRes.set(l);
			System.out.println("done. Result is: " + l);
		});
		t = (System.currentTimeMillis() - t);
		System.err.println("\t" + numWaits + "\t" + numProcesses + "\t" + procRes.get() + "\t" + res.get("simTime")
				+ "\t" + t + "ms");
	}

	static int d;
	static int dMax;

	public static long ackermann(int m, long n) throws MightBlock {
		long res;

		if (++d > dMax) {
			dMax = d;
		}

		if (m == 0) {
			res = n + 1;
			waitFor(res);
		} else if (n == 0) {
			res = activateCallable(() -> ackermann(m - 1, 1)).join().get();
		} else {
			long tmp1 = activateCallable(() -> ackermann(m, n - 1)).join().get();
			res = activateCallable(() -> ackermann(m - 1, tmp1)).join().get();
		}

		--d;

		return res;
	}

	static long ack(int n, long m) throws MightBlock {
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
		return m + 1;
	}

	static Logger log = LogManager.getLogger(TestSimProcessBasics.class);

	public static void main(String... args) throws Exception {
		Simulation s = new Simulation();
		s.setMainProcessActions(sim -> {
			log.warn("Hello world.");
			waitFor(1);
		});
		s.setMainProcessActions(() -> {
			log.warn("Hello world.");
			waitFor(1);
		});
		Map<String, Object> res = SimContext.of(sim -> {
			log.warn("Hello world.");
			waitFor(1);
		});
		Map<String, Object> res2 = SimContext.of(() -> {
			log.warn("Hello world.");
			waitFor(1);
		});
//		d = dMax = 0;
//		System.out.println(ack(3, 9) + "\t" + d + "\t" + dMax);
//		d = dMax = 0;
//		System.out.println(ackermann(3, 9) + "\t" + d + "\t" + dMax);
//		for (int n = 10; n < 11; n++) {
//			long t = System.currentTimeMillis();
//			w = p = j = 0;
//			int i = n;
//			Map<String, Object> res = SimContext.of(sim -> {
//				SimProcess<Integer> fibProcess = activateCallable(s -> fib(i));
//				fibProcess.join();
//				System.out.println("done. Result is: " + fibProcess.get());
//			});
//			t = (System.currentTimeMillis() - t);
//			System.err.println(n + "\tw=" + w + "\tp=" + p + "\tj=" + j + "\t" + res.get("simTime") + "\t" + t + "ms");
//		}
//		d = dMax = 0;
//		System.out.println(ack(3, 9) + "\t" + d + "\t" + dMax);
//		d = dMax = 0;
//		System.out.println(ackermann(3, 9) + "\t" + d + "\t" + dMax);
//		for (int m = 0; m <= 3; m++) {
//			for (int n = 0; n <= 20; n++) {
//				System.out.println(m + "\t" + n + "\t" + ack(m, n));
//			}
//		}
	}

}
