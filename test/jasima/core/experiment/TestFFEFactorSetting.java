/*******************************************************************************
 * This file is part of jasima, v1.3, the Java simulator for manufacturing and 
 * logistics.
 *  
 * Copyright (c) 2015 		jasima solutions UG
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package jasima.core.experiment;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import org.junit.Test;

import jasima.core.run.ConsoleRunner;
import jasima.core.statistics.SummaryStat;

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

		ffe.addFactors("number", (byte) 23, (short) 24, 25, 26l, new BigInteger("27"), 23.1f, 23.2d,
				new BigDecimal("23.3"), null);

		SummaryStat field = runAndGetResult(ffe);

		assertTrue(field.mean() == 3);
		assertTrue(field.numObs() == 9);
	}

	@Test
	public void testBoolFail() {
		FullFactorialExperiment ffe = createFFE();

		ffe.addFactors("bool1", true, false, null);

		try {
			ffe.runExperiment();
		} catch (IllegalArgumentException expected) {
		}

		Map<String, Object> res = ffe.getResults();
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

	@Test
	public void testFFEErrorsNotAborting() {
		FullFactorialExperiment ffe = createFFE();
		ffe.setAbortUponBaseExperimentAbort(false);

		ffe.addFactors("exceptionWhenSetToTrue", false, true);
		ffe.addFactors("exceptionDuringExecution", false, true);
		ffe.addFactors("bool1", true, false, null);

		Map<String, Object> res = ConsoleRunner.run(ffe);

		assertThat("main abort state", res.get("expAborted"), is(0));

		SummaryStat field = (SummaryStat) res.get("field");
		assertThat("runs with results", field.numObs(), is(2));

		SummaryStat abort = (SummaryStat) res.get("baseExperiment.expAborted");
		assertThat("all executed", abort.numObs(), is(12));
		assertThat("aborted", abort.sum(), closeTo(10.0, 1e-6));
	}

	@Test
	public void testFFEErrorsAborting() {
		FullFactorialExperiment ffe = createFFE();
		ffe.setAbortUponBaseExperimentAbort(true);

		ffe.addFactors("exceptionWhenSetToTrue", false, true);
		ffe.addFactors("exceptionDuringExecution", false, true);
		ffe.addFactors("bool1", true, false, null);

		Map<String, Object> res = ConsoleRunner.run(ffe);

		assertThat("main abort state", res.get("expAborted"), is(1));

		SummaryStat field = (SummaryStat) res.get("field");
		assertThat("runs with results", field.numObs(), is(2));

		SummaryStat abort = (SummaryStat) res.get("baseExperiment.expAborted");
		assertThat("all executed", abort.numObs(), is(3));
		assertThat("aborted", abort.sum(), closeTo(1.0, 1e-6));
	}

	@Test
	public void testAbortUponBaseExperimentAbortDefaultValue() {
		FullFactorialExperiment ffe = new FullFactorialExperiment();
		assertTrue("abortUponBaseExperimentAbort", !ffe.isAbortUponBaseExperimentAbort());
	}

	private SummaryStat runAndGetResult(FullFactorialExperiment ffe) {
		Map<String, Object> res = ffe.runExperiment();

		SummaryStat field = (SummaryStat) res.get("field");
		return field;
	}

	private FullFactorialExperiment createFFE() {
		ExpTestPrimitives base = new ExpTestPrimitives();

		FullFactorialExperiment ffe = new FullFactorialExperiment();
		ffe.setBaseExperiment(base);
		ffe.setAbortUponBaseExperimentAbort(true);
//		ffe.addListener(new ConsolePrinter());

		return ffe;
	}

	private void addBoolFactors(FullFactorialExperiment ffe) {
		ffe.addFactors("bool1", true, false, 0, 1, "0", "1", "true", "false", "yes", "no");
		ffe.addFactors("bool2", true, false, 0, 1, "0", "1", "true", "false", "yes", "no", null);
	}

	private void addIntFactors1(FullFactorialExperiment ffe) {
		ffe.addFactors("int1", (byte) 23, (short) 24, 25, 26l, new BigInteger("27"), "28", new ObjectWithNumber(29));
		ffe.addFactors("int2", (byte) 23, (short) 24, 25, 26l, new BigInteger("27"), null, "28",
				new ObjectWithNumber(29));

		ffe.addFactors("byte1", (byte) 23, (short) 24, 25, 26l, new BigInteger("27"), "28", new ObjectWithNumber(29));
		ffe.addFactors("byte2", (byte) 23, (short) 24, 25, 26l, new BigInteger("27"), null, "28",
				new ObjectWithNumber(29));
	}

	private void addIntFactors2(FullFactorialExperiment ffe) {
		ffe.addFactors("short1", (byte) 23, (short) 24, 25, 26l, new BigInteger("27"), "28", new ObjectWithNumber(29));
		ffe.addFactors("short2", (byte) 23, (short) 24, 25, 26l, new BigInteger("27"), null, "28",
				new ObjectWithNumber(29));

		ffe.addFactors("long1", (byte) 23, (short) 24, 25, 26l, new BigInteger("27"), "28", new ObjectWithNumber(29));
		ffe.addFactors("long2", (byte) 23, (short) 24, 25, 26l, new BigInteger("27"), null, "28",
				new ObjectWithNumber(29));
	}

	private void addFloatFactors(FullFactorialExperiment ffe) {
		ffe.addFactors("float1", (byte) 23, (short) 24, 25, 26l, new BigInteger("27"), 23.1f, 23.2d,
				new BigDecimal("23.3"), "28.6", new ObjectWithNumber(29.3));
		ffe.addFactors("float2", (byte) 23, (short) 24, 25, 26l, new BigInteger("27"), 23.1f, 23.2d,
				new BigDecimal("23.3"), null, "28.6", new ObjectWithNumber(29.3));

		ffe.addFactors("double1", (byte) 23, (short) 24, 25, 26l, new BigInteger("27"), 23.1f, 23.2d,
				new BigDecimal("23.3"), "28.6", new ObjectWithNumber(29.3));
		ffe.addFactors("double2", (byte) 23, (short) 24, 25, 26l, new BigInteger("27"), 23.1f, 23.2d,
				new BigDecimal("23.3"), null, "28.6", new ObjectWithNumber(29.3));
	}

	private void addCharFactors(FullFactorialExperiment ffe) {
		ffe.addFactors("char1", 'a', "b");
		ffe.addFactors("char2", 'a', "b", null);

		ffe.addFactors("string", 'a', "b", null, 23, new Object(), new BigDecimal(48));
	}

}
