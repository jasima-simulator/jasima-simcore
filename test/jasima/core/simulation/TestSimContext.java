package jasima.core.simulation;

import static jasima.core.simulation.SimContext.waitFor;
import static org.junit.Assert.assertEquals;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

public class TestSimContext {
//	@Rule
	public Timeout globalTimeout = new Timeout(5000);

	@Test
	public void testSequentialSimulation() {
		Map<String, Object> res1 = SimContext.of("sim1", sim -> {
			// default locale is "en-GB"
			waitFor(2.0);
			String localizedMsg = SimContext.message("Test");
			System.out.println(localizedMsg);
		});
		assertEquals(2.0, (Double) res1.get("simTime"), 1e-6);

		Map<String, Object> res2 = SimContext.of("sim2", sim -> {
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
		Map<String, Object> res1 = SimContext.of("outer", sim -> {
			waitFor(1.0);

			Map<String, Object> res = SimContext.of("inner", s -> {
				waitFor(3.0);
			});
			assertEquals(3.0, (Double) res.get("simTime"), 1e-6);

			waitFor(1.0);
		});

		assertEquals(2.0, (Double) res1.get("simTime"), 1e-6);
	}

	@Test
	public void testNestedSimulation2() throws Exception {
		Map<String, Object> res1 = SimContext.of("outer", () -> {
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
