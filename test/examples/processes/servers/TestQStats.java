package examples.processes.servers;

import static jasima.core.simulation.SimContext.waitFor;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import jasima.core.simulation.SimContext;
import jasima.core.simulation.SimProcess.MightBlock;
import jasima.core.statistics.SummaryStat;
import jasima.core.statistics.TimeWeightedSummaryStat;
import jasima.core.util.ConsolePrinter;

public class TestQStats {

	@Test
	public void testNoop() throws MightBlock {
		Map<String, Object> res = SimContext.of(sim -> {
			Q<String> q = new Q<String>();
			QStatCollector<String> qColl = new QStatCollector<>(q, sim);
		});
		ConsolePrinter.printResults(null, res);

		TimeWeightedSummaryStat stats = (TimeWeightedSummaryStat) res.get("qStats");
		checkCountMeanMinMaxWeightSum(stats, 2, 0.0, 0.0, 0.0, 0.0);
		assertEquals("last value", 0.0, stats.lastValue(), 1e-6);
	}

	@Test
	public void testNoopWithDelay() throws MightBlock {
		Map<String, Object> res = SimContext.of(sim -> {
			Q<String> q = new Q<String>();
			QStatCollector<String> qColl = new QStatCollector<>(q, sim);
			waitFor(1.0);
		});
		ConsolePrinter.printResults(null, res);

		TimeWeightedSummaryStat stats = (TimeWeightedSummaryStat) res.get("qStats");
		checkCountMeanMinMaxWeightSum(stats, 2, 0.0, 0.0, 0.0, 1.0);
		assertEquals("last value", 0.0, stats.lastValue(), 1e-6);
	}

	@Test
	public void testAverageInv() throws MightBlock {
		Map<String, Object> res = SimContext.of(sim -> {
			Q<String> q = new Q<String>();
			QStatCollector<String> qColl = new QStatCollector<>(q, sim);
			waitFor(100.0);
			q.put("item");
			waitFor(100.0);
		});
		TimeWeightedSummaryStat stats = (TimeWeightedSummaryStat) res.get("qStats");

		ConsolePrinter.printResults(null, res);

		checkCountMeanMinMaxWeightSum(stats, 2 + 1, (0.0 * 100.0 + 1.0 * 100.0) / 200.0, 0.0, 1.0, 200.0);

		assertEquals("last value", 1.0, stats.lastValue(), 1e-6);
	}

	@Test
	public void testCreationDuringSim() throws MightBlock {
		Map<String, Object> res = SimContext.of(sim -> {
			Q<String> q = new Q<String>();
			waitFor(100.0);
			QStatCollector<String> qColl = new QStatCollector<>(q, sim);
			waitFor(100.0);
			q.put("item");
			waitFor(100.0);
		});
		TimeWeightedSummaryStat stats = (TimeWeightedSummaryStat) res.get("qStats");

		ConsolePrinter.printResults(null, res);

		checkCountMeanMinMaxWeightSum(stats, 2 + 1, (0.0 * 100.0 + 1.0 * 100.0) / 200.0, 0.0, 1.0, 200.0);

		assertEquals("last value", 1.0, stats.lastValue(), 1e-6);
	}

	@Test
	public void testCreationDuringSim2() throws MightBlock {
		Map<String, Object> res = SimContext.of(sim -> {
			Q<String> q = new Q<String>();
			q.put("item");
			waitFor(100.0);
			QStatCollector<String> qColl = new QStatCollector<>(q, sim);
			waitFor(100.0);
		});
		TimeWeightedSummaryStat stats = (TimeWeightedSummaryStat) res.get("qStats");

		ConsolePrinter.printResults(null, res);

		checkCountMeanMinMaxWeightSum(stats, 2, (1.0 * 100.0) / 100.0, 1.0, 1.0, 100.0);

		assertEquals("last value", 1.0, stats.lastValue(), 1e-6);
	}

	@Test
	public void testStats() throws MightBlock {
		Map<String, Object> res = SimContext.of(sim -> {
			Q<String> q = new Q<String>();
			QStatCollector<String> qColl = new QStatCollector<>(q, sim);

			waitFor(1.0);
			q.put("test1");
			waitFor(2.0);
			q.put("test2");
			waitFor(1.0);
			q.take();
		});
		TimeWeightedSummaryStat stats = (TimeWeightedSummaryStat) res.get("qStats");

		ConsolePrinter.printResults(null, res);

		checkCountMeanMinMaxWeightSum(stats, 3 + 2, (0.0 * 1.0 + 1.0 * 2.0 + 2.0 * 1.0) / 4.0, 0.0, 2.0, 4.0);

		assertEquals("last value", 1.0, stats.lastValue(), 1e-6);
	}

	private static void checkCountMeanMinMaxWeightSum(SummaryStat stats, int numObs, double mean, double min,
			double max, double weightSum) {
		assertEquals("mean", mean, stats.mean(), 1e-6);
		assertEquals("min", min, stats.min(), 1e-6);
		assertEquals("max", max, stats.max(), 1e-6);
		assertEquals("numObs", numObs, stats.numObs());
		assertEquals("weightSum", weightSum, stats.weightSum(), 1e-6);
	}

}
