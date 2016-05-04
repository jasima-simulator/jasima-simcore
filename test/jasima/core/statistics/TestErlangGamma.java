package jasima.core.statistics;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import jasima.core.random.continuous.DblErlang;
import jasima.core.random.continuous.DblGamma;
import jasima.core.util.MersenneTwister;

public class TestErlangGamma {

	@Test
	public void test1() {
		DblErlang d = new DblErlang(5, 10.0);
		d.setRndGen(new MersenneTwister(42));

		// check
		SummaryStat stat = new SummaryStat();
		for (int i = 0; i < 1000000; i++) {
			stat.value(d.nextDbl());
		}

		System.out.println(stat.mean());
		System.out.println(stat.variance());

		assertEquals(5 * 10.0, d.getNumericalMean(), 1e-6);

		// check sample to be close enough to analytical results
		assertEquals("mean", 5 * 10.0, stat.mean(), 1e-1);
		assertEquals("variance", 5 * 10.0 * 10.0, stat.variance(), 1.0);
	}

	@Test
	public void test2() {
		DblGamma d = new DblGamma(5, 10.0);
		d.setRndGen(new MersenneTwister(42));

		// check
		SummaryStat stat = new SummaryStat();
		for (int i = 0; i < 1000000; i++) {
			stat.value(d.nextDbl());
		}

		System.out.println(stat.mean());
		System.out.println(stat.variance());

		assertEquals(5 * 10.0, d.getNumericalMean(), 1e-6);

		// check sample to be close enough to analytical results
		assertEquals("mean", 5 * 10.0, stat.mean(), 1e-1);
		assertEquals("variance", 5 * 10.0 * 10.0, stat.variance(), 1.0);
	}

}
