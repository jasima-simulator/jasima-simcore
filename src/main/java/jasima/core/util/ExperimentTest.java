/*
This file is part of jasima, the Java simulator for manufacturing and logistics.
 
Copyright 2010-2022 jasima contributors (see license.txt)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package jasima.core.util;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Precision;
import org.junit.Rule;
import org.junit.rules.ErrorCollector;

import jasima.core.experiment.Experiment;
import jasima.core.statistics.SummaryStat;

/**
 * Utility class that can be used as a base class for JUnit tests which check
 * for many results of an {@link Experiment} at once. Deriving a new test class
 * from {@code ExperimentTest} and calling {@link #checkResults(Map, Map)} many
 * results of an experiment can be validated with a single method call.
 * 
 * @author Torsten Hildebrandt, 2012-08-08
 */
public class ExperimentTest {

	@Rule
	public ErrorCollector errorCollector = new ErrorCollector();

	/**
	 * precision in terms of ULPs (Units in the last place), so FP comparisons work
	 * for large and small numbers;
	 * 
	 * @see <a href=
	 *      "https://randomascii.wordpress.com/2012/02/25/comparing-floating-point-numbers-2012-edition/">
	 *      </a>
	 */
	protected int maxUlps = 10;

	/**
	 * Checks whether key sets of actual and expected results are the same.
	 * 
	 * @param resActual   The map of results actually obtained.
	 * @param resExpected The map of expected results.
	 */
	protected void checkKeySets(Map<String, Object> resActual, Map<String, Object> resExpected) {
		Set<String> keysAct = new HashSet<String>(resActual.keySet());
		Set<String> keysExp = new HashSet<String>(resExpected.keySet());

		HashSet<String> all = new HashSet<String>();
		all.addAll(keysAct);
		all.addAll(keysExp);

		HashSet<String> onlyExp = new HashSet<String>(all);
		onlyExp.removeAll(keysAct);

		HashSet<String> onlyAct = new HashSet<String>(all);
		onlyAct.removeAll(keysExp);
		errorCollector.checkThat("key sets should be equal.\n" + "keys missing in actual result map: " + onlyExp + ";\n"
				+ "keys only in actual result map: " + onlyAct, keysAct, is(keysExp));
	}

	/**
	 * Checks if all keys in <code>resExpected</code> are also present in
	 * <code>resActual</code> and have the same values. Additional keys in
	 * <code>resActual</code> as well as the key <code>"runTime"</code> are ignored.
	 * 
	 * @param resActual   The map of results actually obtained.
	 * @param resExpected The map of expected results.
	 */
	protected void checkResults(Map<String, Object> resActual, Map<String, Object> resExpected) {
		for (String name : resExpected.keySet()) {
			if (Experiment.RUNTIME.equals(name) || name.endsWith("." + Experiment.RUNTIME))
				continue;
			errorCollector.checkThat(name, resActual.keySet(), hasItem(name));

			Object expected = resExpected.get(name);
			Object actual = resActual.get(name);
			if (actual == null)
				continue;

			name = "result entry '" + name + "'";

			if (expected instanceof SummaryStat) {
				checkValueStat(name, (SummaryStat) expected, (SummaryStat) actual);
			} else if (expected instanceof Double) {
				Number exp = (Number) expected;
				Number act = (Number) actual;
				checkDouble(name, act.doubleValue(), exp.doubleValue());
			} else if (expected instanceof Double) {
				Number exp = (Number) expected;
				Number act = (Number) actual;
				checkDouble(name, act.doubleValue(), exp.doubleValue());
			} else
				errorCollector.checkThat(name, actual, is(expected));
		}
	}

	protected void checkFloat(String name, float act, float exp) {
		if (act != exp) {
			boolean cmp = Precision.equals(act, exp, maxUlps);
			errorCollector.checkThat(name + ";  act: " + act + ";  exp: " + exp, cmp, is(true));
		}
	}

	protected void checkDouble(String name, double act, double exp) {
		if (Double.compare(exp, act) != 0) {
			boolean cmp = Precision.equals(act, exp, maxUlps);
			errorCollector.checkThat(name + ";  act: " + act + ";  exp: " + exp, cmp, is(true));
		}
	}

	protected void checkValueStat(String name, SummaryStat exp, SummaryStat act) {
		errorCollector.checkThat(name + " (numObs)", act.numObs(), is(exp.numObs()));
		checkDouble(name + " (weightSum)", act.weightSum(), exp.weightSum());
		checkDouble(name + " (mean)", act.mean(), exp.mean());
		checkDouble(name + " (min)", act.min(), exp.min());
		checkDouble(name + " (max)", act.max(), exp.max());
		checkDouble(name + " (stdDev)", act.stdDev(), exp.stdDev());
	}

}
