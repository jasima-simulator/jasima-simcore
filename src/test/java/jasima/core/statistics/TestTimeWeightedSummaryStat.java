package jasima.core.statistics;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notANumber;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class TestTimeWeightedSummaryStat {

	TimeWeightedSummaryStat s = new TimeWeightedSummaryStat();

	@Test
	public void testEmpty() throws Exception {
		assertEquals("numObs", 0, s.numObs());
		assertThat("mean", s.mean(), is(notANumber()));
		assertThat("min", s.min(), is(notANumber()));
		assertThat("max", s.max(), is(notANumber()));
		assertThat("lastValue", s.lastValue(), is(notANumber()));
		assertThat("lastWeight", s.lastWeight(), is(notANumber()));
		assertThat("weightSum", s.weightSum(), is(notANumber()));
		assertThat("lastTime", s.lastTime(), is(notANumber()));
	}

	@Test
	public void testSingleValue() throws Exception {
		// not even a single time interval was closed
		s.value(3.0, 100.0);
		assertEquals("numObs", 1, s.numObs());
		assertEquals("lastValue", 3.0, s.lastValue(), 1e-6);
		assertEquals("lastWeight", 0.0, s.lastWeight(), 1e-6);
		assertEquals("lastTime", 100.0, s.lastTime(), 1e-6);
		assertEquals("mean", 3.0, s.mean(), 1e-6);
		assertEquals("min", 3.0, s.min(), 1e-6);
		assertEquals("max", 3.0, s.max(), 1e-6);
		assertEquals("weightSum", 0.0, s.weightSum(), 1e-6);
	}

	@Test
	public void testSingleInterval() throws Exception {
		s.value(3.0, 100.0);
		s.value(3.0, 200.0);
		assertEquals("numObs", 2, s.numObs());
		assertEquals("lastValue", 3.0, s.lastValue(), 1e-6);
		assertEquals("lastWeight", 100.0, s.lastWeight(), 1e-6);
		assertEquals("lastTime", 200.0, s.lastTime(), 1e-6);
		assertEquals("mean", 3.0, s.mean(), 1e-6);
		assertEquals("min", 3.0, s.min(), 1e-6);
		assertEquals("max", 3.0, s.max(), 1e-6);
		assertEquals("weightSum", 100.0, s.weightSum(), 1e-6);
	}

	@Test
	public void testZeroTimeDiff() throws Exception {
		s.value(3.0, 100.0);
		s.value(3.0, 100.0);
		assertEquals("numObs", 2, s.numObs());
		assertEquals("lastValue", 3.0, s.lastValue(), 1e-6);
		assertEquals("lastWeight", 0.0, s.lastWeight(), 1e-6);
		assertEquals("lastTime", 100.0, s.lastTime(), 1e-6);
		assertEquals("mean", 3.0, s.mean(), 1e-6);
		assertEquals("min", 3.0, s.min(), 1e-6);
		assertEquals("max", 3.0, s.max(), 1e-6);
		assertEquals("weightSum", 0.0, s.weightSum(), 1e-6);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNegativeTimeDiff() throws Exception {
		s.value(3.0, 100.0);
		s.value(3.0, 90.0);
	}

	@Test
	public void testThreeValues() throws Exception {
		s.value(3.0, 100.0);
		s.value(4.0, 200.0);
		s.value(5.0, 300.0);
		assertEquals("numObs", 3, s.numObs());
		assertEquals("lastValue", 5.0, s.lastValue(), 1e-6);
		assertEquals("lastWeight", 100.0, s.lastWeight(), 1e-6);
		assertEquals("lastTime", 300.0, s.lastTime(), 1e-6);
		assertEquals("mean", (3.0 * 100.0 + 4 * 100) / 200.0, s.mean(), 1e-6);
		assertEquals("min", 3.0, s.min(), 1e-6);
		assertEquals("max", 5.0, s.max(), 1e-6);
		assertEquals("weightSum", 200.0, s.weightSum(), 1e-6);
		// not defined
		assertThat("variance", s.variance(), is(notANumber()));
		assertThat("variancePopulation", s.variancePopulation(), is(notANumber()));
		assertThat("stdDev", s.stdDev(), is(notANumber()));
	}

	@Test
	public void testThreeValuesDesc() throws Exception {
		s.value(5.0, 100.0);
		s.value(4.0, 200.0);
		s.value(3.0, 300.0);
		assertEquals("numObs", 3, s.numObs());
		assertEquals("lastValue", 3.0, s.lastValue(), 1e-6);
		assertEquals("lastWeight", 100.0, s.lastWeight(), 1e-6);
		assertEquals("lastTime", 300.0, s.lastTime(), 1e-6);
		assertEquals("mean", (5.0 * 100.0 + 4 * 100) / 200.0, s.mean(), 1e-6);
		assertEquals("min", 3.0, s.min(), 1e-6);
		assertEquals("max", 5.0, s.max(), 1e-6);
		assertEquals("weightSum", 200.0, s.weightSum(), 1e-6);
		// not defined
		assertThat("variance", s.variance(), is(notANumber()));
		assertThat("variancePopulation", s.variancePopulation(), is(notANumber()));
		assertThat("stdDev", s.stdDev(), is(notANumber()));
	}

}
