/*******************************************************************************
 * Copyright 2011, 2012 Torsten Hildebrandt and BIBA - Bremer Institut für Produktion und Logistik GmbH
 *
 * This file is part of jasima, v1.0.
 *
 * jasima is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jasima is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jasima.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package jasima.core.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.matchers.JUnitMatchers.hasItem;
import jasima.core.experiment.Experiment;
import jasima.core.statistics.SummaryStat;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Rule;
import org.junit.rules.ErrorCollector;

/**
 * Base class for JUnit tests which check for many results of an
 * {@link Experiment} at once. Deriving a new test class from
 * {@code ExperimentTest} and calling {@link #checkResults(Map, Map)} many
 * results of an experiment can be validated with a single method call.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>, 2012-08-08
 * @version $Id$
 */
public class ExperimentTest {

	@Rule
	public ErrorCollector errorCollector = new ErrorCollector();

	public static final double EPS = 1e-6d;

	/**
	 * Checks whether key sets of actual and expected results are the same.
	 * 
	 * @param resActual
	 * @param resExpected
	 */
	public void checkKeySets(Map<String, Object> resActual,
			Map<String, Object> resExpected) {
		Set<String> keysAct = new HashSet<String>(resActual.keySet());
		Set<String> keysExp = new HashSet<String>(resExpected.keySet());

		HashSet<String> all = new HashSet<String>();
		all.addAll(keysAct);
		all.addAll(keysExp);

		HashSet<String> onlyExp = new HashSet<String>(all);
		onlyExp.removeAll(keysAct);

		HashSet<String> onlyAct = new HashSet<String>(all);
		onlyAct.removeAll(keysExp);
		errorCollector.checkThat("key sets should be equal.\n"
				+ "keys missing in actual result map: " + onlyExp + ";\n"
				+ "keys only in actual result map: " + onlyAct, keysAct,
				is(keysExp));
	}

	/**
	 * Checks if all keys in <code>resExpected</code> are also present in
	 * <code>resActual</code> and have the same values. Additional keys in
	 * <code>resActual</code> as well as the key <code>"runTime"</code> are
	 * ignored.
	 * 
	 * @param resActual
	 * @param resExpected
	 */
	public void checkResults(Map<String, Object> resActual,
			Map<String, Object> resExpected) {
		for (String name : resExpected.keySet()) {
			if (Experiment.RUNTIME.equals(name)
					|| name.endsWith("." + Experiment.RUNTIME))
				continue;
			errorCollector.checkThat(name, resActual.keySet(), hasItem(name));

			Object expected = resExpected.get(name);
			Object actual = resActual.get(name);
			if (actual == null)
				continue;

			name = "result entry '" + name + "'";

			if (expected instanceof SummaryStat) {
				checkValueStat(name, (SummaryStat) expected,
						(SummaryStat) actual);
			} else if (expected instanceof Double) {
				Double exp = (Double) expected;
				Double act = (Double) actual;
				checkDouble(name, act, exp);
			} else if (expected instanceof Float) {
				Float exp = (Float) expected;
				Float act = (Float) actual;
				Double e = exp == null ? null : exp.doubleValue();
				Double a = act == null ? null : act.doubleValue();
				errorCollector.checkThat(name, e, closeTo(a, EPS));
			} else
				errorCollector.checkThat(name, actual, is(expected));
		}
	}

	private void checkDouble(String name, double act, double exp) {
		if (Double.compare(exp, act) != 0)
			errorCollector.checkThat(name, exp, closeTo(act, EPS));
	}

	public void checkValueStat(String name, SummaryStat exp, SummaryStat act) {
		errorCollector.checkThat(name + " (numObs)", act.numObs(),
				is(exp.numObs()));
		checkDouble(name + " (weightSum)", act.weightSum(), exp.weightSum());
		checkDouble(name + " (mean)", act.mean(), exp.mean());
		checkDouble(name + " (min)", act.min(), exp.min());
		checkDouble(name + " (max)", act.max(), exp.max());
		checkDouble(name + " (stdDev)", act.stdDev(), exp.stdDev());
	}

}
