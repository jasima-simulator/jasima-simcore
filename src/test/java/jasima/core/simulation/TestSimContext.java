package jasima.core.simulation;

import static jasima.core.simulation.SimContext.waitFor;
import static jasima.core.simulation.Simulation.SIM_TIME;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.junit.Test;
import org.junit.rules.Timeout;

import jasima.core.simulation.SimProcess.MightBlock;
import jasima.core.simulation.Simulation.SimulationFailed;

public class TestSimContext {
//	@Rule
	public Timeout globalTimeout = new Timeout(5000);

	@Test
	public void testSequentialSimulation() {
		Map<String, Object> res1 = SimContext.simulationOf("sim1", sim -> {
			// default locale is "en-GB"
			waitFor(2.0);
			String localizedMsg = SimContext.message("Test");
			System.out.println(localizedMsg);
		});
		assertEquals(2.0, (Double) res1.get("simTime"), 1e-6);

		Map<String, Object> res2 = SimContext.simulationOf("sim2", sim -> {
			sim.setLocale(Locale.GERMANY);
			waitFor(3.0);
			System.out.println(SimContext.message("Test"));
		});
		assertEquals(3.0, (Double) res2.get("simTime"), 1e-6);
	}

	@Test
	public void testParallelSimulation() throws Exception {
		Future<Map<String, Object>> f1 = SimContext.async("sim1", sim -> {
			// default locale is "en-GB"
			waitFor(2.0);
			String localizedMsg = SimContext.message("Test");
			System.out.println(localizedMsg);
		});
		Future<Map<String, Object>> f2 = SimContext.async("sim2", sim -> {
			sim.setLocale(Locale.GERMANY);
			waitFor(3.0);
			System.out.println(SimContext.message("Test"));
		});

		// wait until both finished
		Map<String, Object> res1 = f1.get();
		Map<String, Object> res2 = f2.get();

		// check results
		assertEquals(2.0, (Double) res1.get("simTime"), 1e-6);
		assertEquals(3.0, (Double) res2.get("simTime"), 1e-6);
	}

	@Test
	public void testNestedSimulation1() throws Exception {
		Map<String, Object> res1 = SimContext.simulationOf("outer", sim -> {
			waitFor(1.0);

			Map<String, Object> res = SimContext.simulationOf("inner", s -> {
				waitFor(3.0);
			});
			assertEquals(3.0, (Double) res.get("simTime"), 1e-6);

			waitFor(1.0);
		});

		assertEquals(2.0, (Double) res1.get("simTime"), 1e-6);
	}

	@Test
	public void testNestedSimulationWithError() throws Exception {
		try {
			Map<String, Object> res1 = SimContext.simulationOf("outer", sim -> {
				waitFor(1.0);

				Map<String, Object> res = SimContext.simulationOf("inner", s -> {
					waitFor(3.0);
					throw new IllegalStateException();
				});
				assertEquals(3.0, (Double) res.get("simTime"), 1e-6);

				waitFor(1.0);
			});

			assertEquals(2.0, (Double) res1.get("simTime"), 1e-6);
		} catch (SimulationFailed outer) {
			assertThat(outer.getCause(), instanceOf(SimulationFailed.class));
			assertThat(outer.getCause().getCause(), instanceOf(IllegalStateException.class));
		}
	}

	@Test
	public void testNestedSimulationInSubprocess() throws Exception {
		Map<String, Object> res1 = SimContext.simulationOf("outer", sim -> {
			waitFor(1.0);

			SimContext.activate("sub-process", s -> {
				waitFor(1.0);

				Map<String, Object> res = SimContext.simulationOf("inner", ss -> {
					waitFor(3.0);
				});
				assertEquals(3.0, (Double) res.get("simTime"), 1e-6);
			});

			waitFor(1.0);
		});

		assertEquals(2.0, (Double) res1.get("simTime"), 1e-6);
	}

	@Test
	public void testNestedSimulationInSubprocessWithError() throws Exception {
		try {
			Map<String, Object> res1 = SimContext.simulationOf("outer", sim -> {
				waitFor(1.0);

				SimContext.activate("sub-process", () -> {
					waitFor(1.0);

					Map<String, Object> res = SimContext.simulationOf("inner", s2 -> {
						waitFor(3.0);
						throw new IllegalStateException();
					});
					assertEquals(3.0, (Double) res.get("simTime"), 1e-6);
				});

				waitFor(1.0);
			});

			assertEquals(2.0, (Double) res1.get("simTime"), 1e-6);
		} catch (SimulationFailed outer) {
			assertThat(outer.getCause(), instanceOf(SimulationFailed.class));
			assertThat(outer.getCause().getCause(), instanceOf(IllegalStateException.class));
		}
	}

	@Test
	public void testNestedSimulation2() throws Exception {
		Map<String, Object> res1 = SimContext.simulationOf("outer", () -> {
			waitFor(1.0);

			Future<Map<String, Object>> f = SimContext.async("inner", s -> {
				waitFor(3.0);
			});
			Map<String, Object> res = rethrowUnchecked(f::get);
			assertEquals(3.0, (Double) res.get("simTime"), 1e-6);

			waitFor(1.0);
		});

		// check results
		assertEquals(2.0, (Double) res1.get("simTime"), 1e-6);
	}

	@Test
	public void testNestedSimulation3() throws Exception {
		SimContext.simulationOf(outerSim -> {
			outerSim.setSimTimeToMillisFactor(DAYS);
			Instant outerStart = Instant.parse("2020-01-01T15:00:00Z");
			outerSim.setSimTimeStartInstant(outerStart);

			for (int n = 0; n < 5; n++) {
				// create innerSim, init with state of outerSim
				Map<String, Object> resInner = SimContext.simulationOf(innerSim -> {
					innerSim.setSimTimeToMillisFactor(MINUTES);

					Instant innerStartAbs = outerSim.simTimeAbs();
					innerSim.setSimTimeStartInstant(innerStartAbs);

					waitFor(1, DAYS); // simulate 1 day in innerSim (with simTime in minutes)
					assertEquals("inner.simTimeAbsAtEnd", innerStartAbs.plus(1, DAYS), innerSim.simTimeAbs());
				});

				// inspect and use results from "innerSim"
				assertEquals("inner.simTimeAtEnd", 1 * 24 * 60.0, (Double) resInner.get(SIM_TIME), 1e-6);

				waitFor(1); // simulate 1 day in outerSim
			}

			assertEquals("simTimeAtEnd", 5.0, outerSim.simTime(), 1e-6);
			assertEquals("simTimeAbsAtEnd", outerStart.plus(5, DAYS), outerSim.simTimeAbs());
		});
	}

	@Test
	public void testNestedSimulation3_methodRefs() throws Exception {
		SimContext.simulationOf(this::runOuterSim);
	}

	private void runOuterSim(Simulation sim) throws MightBlock {
		sim.setSimTimeToMillisFactor(DAYS);
		Instant outerStart = Instant.parse("2020-01-01T15:00:00Z");
		sim.setSimTimeStartInstant(outerStart);

		for (int n = 0; n < 5; n++) {
			// create innerSim, init with state of outerSim
			Map<String, Object> resInner = SimContext.simulationOf(innerSim -> runInnerSim(innerSim, sim.simTimeAbs()));

			// inspect and use results from "innerSim"
			assertEquals("inner.simTimeAtEnd", 1 * 24 * 60.0, (Double) resInner.get(SIM_TIME), 1e-6);

			waitFor(1); // simulate 1 day in outerSim
		}

		assertEquals("simTimeAtEnd", 5.0, sim.simTime(), 1e-6);
		assertEquals("simTimeAbsAtEnd", outerStart.plus(5, DAYS), sim.simTimeAbs());
	}

	private void runInnerSim(Simulation sim, Instant innerStartAbs) throws MightBlock {
		sim.setSimTimeToMillisFactor(MINUTES);
		sim.setSimTimeStartInstant(innerStartAbs);

		waitFor(1, DAYS); // simulate 1 day in innerSim (with simTime in minutes)
		assertEquals("inner.simTimeAbsAtEnd", innerStartAbs.plus(1, DAYS), sim.simTimeAbs());
	}

	@Test(expected = SimulationFailed.class)
	public void cantSetInitialSimTimeOnceRunning() throws Exception {
		// triggers IllegalStateException which is then wrapped in a SimulationFailed
		SimContext.simulationOf(sim -> {
			sim.setInitialSimTime(0.0);
		});
	}

	@Test
	public void testTimeConvertInDays() throws Exception {
		SimContext.simulationOf(sim -> {
			sim.setSimTimeToMillisFactor(DAYS);

			Instant startInstant = Instant.parse("2020-01-01T15:00:00Z");
			sim.setSimTimeStartInstant(startInstant);

			waitFor(5); // wait 5 days

			assertEquals("simTimeAtEnd", 5.0, sim.simTime(), 1e-6);
			assertEquals("simTimeAbsAtEnd", startInstant.plus(5, DAYS), sim.simTimeAbs());
		});
	}

	@Test
	public void testWaitForDuration() throws Exception {
		SimContext.simulationOf(sim -> {
			Instant startInstant = Instant.parse("2020-01-01T15:00:00Z");
			sim.setSimTimeStartInstant(startInstant);

			waitFor(Duration.ofDays(5)); // wait 5 days

			assertEquals("simTimeAbsAtEnd", startInstant.plus(5, DAYS), sim.simTimeAbs());
		});
	}

	@Test
	public void testTimeConvertInMinutes() throws Exception {
		SimContext.simulationOf(sim -> {
			sim.setSimTimeToMillisFactor(MINUTES);

			Instant startInstant = Instant.parse("2020-01-01T15:00:00Z");
			sim.setSimTimeStartInstant(startInstant);

			waitFor(5 * 24 * 60.0); // wait 5 days

			assertEquals("simTimeAtEnd", 5 * 24 * 60.0, sim.simTime(), 1e-6);
			assertEquals("simTimeAbsAtEnd", startInstant.plus(5, DAYS), sim.simTimeAbs());
		});
	}

	@Test
	public void testTimeConvertDefaultIsMinutes() throws Exception {
		SimContext.simulationOf(sim -> {
			Instant startInstant = Instant.parse("2020-01-01T15:00:00Z");
			sim.setSimTimeStartInstant(startInstant);

			waitFor(5 * 24 * 60.0); // wait 5 days

			assertEquals("simTimeAtEnd", 5 * 24 * 60.0, sim.simTime(), 1e-6);
			assertEquals("simTimeAbsAtEnd", startInstant.plus(5, DAYS), sim.simTimeAbs());
		});
	}

	private <R> R rethrowUnchecked(Callable<R> c) {
		try {
			return c.call();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
