package jasima.core.experiment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import jasima.core.statistics.SummaryStat;
import jasima.core.util.ConsolePrinter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import org.junit.Test;

public class TestFFEFactorSetting {

	private static final class ObjectWithNumber {
		private final Number number;

		public ObjectWithNumber(Number number) {
			super();
			this.number = Objects.requireNonNull(number);
		}

		@Override
		public String toString() {
			return number.toString();
		}
	}

	@Test
	public void testBool() {
		FullFactorialExperiment ffe = createFFE();

		addBoolFactors(ffe);

		SummaryStat field = runAndGetResult(ffe);

		assertTrue(field.mean() == 3);
		assertTrue(field.numObs() == 110);
	}

	@Test
	public void testInts1() {
		FullFactorialExperiment ffe = createFFE();

		addIntFactors1(ffe);

		SummaryStat field = runAndGetResult(ffe);

		assertTrue(field.mean() == 3);
		assertTrue(field.numObs() == 3136);
	}

	@Test
	public void testInts2() {
		FullFactorialExperiment ffe = createFFE();

		addIntFactors2(ffe);

		SummaryStat field = runAndGetResult(ffe);

		assertTrue(field.mean() == 3);
		assertTrue(field.numObs() == 3136);
	}

	@Test
	public void testFloats() {
		FullFactorialExperiment ffe = createFFE();

		addFloatFactors(ffe);
		SummaryStat field = runAndGetResult(ffe);

		assertTrue(field.mean() == 3);
		assertTrue(field.numObs() == 12100);
	}

	@Test
	public void testChars() {
		FullFactorialExperiment ffe = createFFE();

		addCharFactors(ffe);

		SummaryStat field = runAndGetResult(ffe);

		assertTrue(field.mean() == 3);
		assertTrue(field.numObs() == 36);
	}

	@Test
	public void testNumber() {
		FullFactorialExperiment ffe = createFFE();

		ffe.addFactors("number", (byte) 23, (short) 24, 25, 26l,
				new BigInteger("27"), 23.1f, 23.2d, new BigDecimal("23.3"),
				null);

		SummaryStat field = runAndGetResult(ffe);

		assertTrue(field.mean() == 3);
		assertTrue(field.numObs() == 9);
	}

	@Test
	public void testBoolFail() {
		FullFactorialExperiment ffe = createFFE();

		ffe.addFactors("bool1", true, false, null);

		Map<String, Object> res = ffe.runExperiment();
		ffe.printResults();

		SummaryStat field = (SummaryStat) res.get("field");
		assertTrue(field.numObs() == 2);

		SummaryStat abort = (SummaryStat) res.get("baseExperiment.expAborted");
		assertTrue(abort.numObs() == 3);
		assertTrue(abort.sum() == 1);

		assertNotNull(res.get("baseExperiment.exception"));
		assertNotNull(res.get("baseExperiment.exceptionMessage"));
		assertEquals(1, (int) res.get("expAborted"));
	}

	private SummaryStat runAndGetResult(FullFactorialExperiment ffe) {
		Map<String, Object> res = ffe.runExperiment();

		SummaryStat field = (SummaryStat) res.get("field");
		return field;
	}

	private FullFactorialExperiment createFFE() {
		TestPrimitivesExperiment base = new TestPrimitivesExperiment();

		FullFactorialExperiment ffe = new FullFactorialExperiment();
		ffe.setBaseExperiment(base);
		ffe.setAbortUponBaseExperimentAbort(true);
		ffe.addNotifierListener(new ConsolePrinter());
		return ffe;
	}

	private void addBoolFactors(FullFactorialExperiment ffe) {
		ffe.addFactors("bool1", true, false, 0, 1, "0", "1", "true", "false",
				"yes", "no");
		ffe.addFactors("bool2", true, false, 0, 1, "0", "1", "true", "false",
				"yes", "no", null);
	}

	private void addIntFactors1(FullFactorialExperiment ffe) {
		ffe.addFactors("int1", (byte) 23, (short) 24, 25, 26l, new BigInteger(
				"27"), "28", new ObjectWithNumber(29));
		ffe.addFactors("int2", (byte) 23, (short) 24, 25, 26l, new BigInteger(
				"27"), null, "28", new ObjectWithNumber(29));

		ffe.addFactors("byte1", (byte) 23, (short) 24, 25, 26l, new BigInteger(
				"27"), "28", new ObjectWithNumber(29));
		ffe.addFactors("byte2", (byte) 23, (short) 24, 25, 26l, new BigInteger(
				"27"), null, "28", new ObjectWithNumber(29));
	}

	private void addIntFactors2(FullFactorialExperiment ffe) {
		ffe.addFactors("short1", (byte) 23, (short) 24, 25, 26l,
				new BigInteger("27"), "28", new ObjectWithNumber(29));
		ffe.addFactors("short2", (byte) 23, (short) 24, 25, 26l,
				new BigInteger("27"), null, "28", new ObjectWithNumber(29));

		ffe.addFactors("long1", (byte) 23, (short) 24, 25, 26l, new BigInteger(
				"27"), "28", new ObjectWithNumber(29));
		ffe.addFactors("long2", (byte) 23, (short) 24, 25, 26l, new BigInteger(
				"27"), null, "28", new ObjectWithNumber(29));
	}

	private void addFloatFactors(FullFactorialExperiment ffe) {
		ffe.addFactors("float1", (byte) 23, (short) 24, 25, 26l,
				new BigInteger("27"), 23.1f, 23.2d, new BigDecimal("23.3"),
				"28.6", new ObjectWithNumber(29.3));
		ffe.addFactors("float2", (byte) 23, (short) 24, 25, 26l,
				new BigInteger("27"), 23.1f, 23.2d, new BigDecimal("23.3"),
				null, "28.6", new ObjectWithNumber(29.3));

		ffe.addFactors("double1", (byte) 23, (short) 24, 25, 26l,
				new BigInteger("27"), 23.1f, 23.2d, new BigDecimal("23.3"),
				"28.6", new ObjectWithNumber(29.3));
		ffe.addFactors("double2", (byte) 23, (short) 24, 25, 26l,
				new BigInteger("27"), 23.1f, 23.2d, new BigDecimal("23.3"),
				null, "28.6", new ObjectWithNumber(29.3));
	}

	private void addCharFactors(FullFactorialExperiment ffe) {
		ffe.addFactors("char1", 'a', "b");
		ffe.addFactors("char2", 'a', "b", null);

		ffe.addFactors("string", 'a', "b", null, 23, new Object(),
				new BigDecimal(48));
	}

}
