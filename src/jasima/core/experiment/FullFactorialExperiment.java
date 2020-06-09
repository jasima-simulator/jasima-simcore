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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jasima.core.util.MsgCategory;
import jasima.core.util.i18n.I18n;

/**
 * Systematically tests all possible combinations of various discrete factors
 * and their values on a base experiment.
 * 
 * @author Torsten Hildebrandt
 * 
 * @see RandomFractionalExperiment
 * @see OCBAExperiment
 */
public class FullFactorialExperiment extends AbstractMultiConfExperiment {

	private static final long serialVersionUID = 1612150171949724274L;

	// fields for parameters
	private int maxConfigurations = 1000000;
	private Map<String, List<Object>> factors;

	// fields used during run
	private ArrayList<String> factorNames;

	public FullFactorialExperiment() {
		super();
		setAbortUponBaseExperimentAbort(false);
		factors = new LinkedHashMap<String, List<Object>>();
	}

	/**
	 * Clears all configurations previously added using addFactor.
	 */
	public void clearFactors() {
		factors.clear();
	}

	/**
	 * Adds a configuration to test.
	 * 
	 * @param name  The factor name. This should be the name of a Java Beans
	 *              Property of the base experiment, otherwise execution of the
	 *              experiment will fail. In case {@code value} is a
	 *              {@link AbstractMultiConfExperiment.ComplexFactorSetter}, name
	 *              can be arbitrary.
	 * @param value The value to test for factor {@code name}.
	 */
	public void addFactor(String name, Object value) {
		List<Object> values = factors.get(name);
		if (values == null) {
			values = new ArrayList<Object>();
			factors.put(name, values);
		}

		if (values.contains(value))
			throw new IllegalArgumentException("No duplicate values allowed: '" + value + "' (factor '" + name + "')");

		values.add(value);
	}

	/**
	 * Adds a new value for the factor "name". This allows putting in a Java 8
	 * method reference or lambda expression as a factor setter.
	 */
	public void addFactor(String name, ComplexFactorSetter value) {
		addFactor(name, (Object) value);
	}

	/**
	 * <p>
	 * Convenience method to set a factor "factorName" for all possible values of an
	 * enumeration.
	 * </p>
	 * <p>
	 * Say an experiment has a property "color" of type {@code ColorEnum}
	 * 
	 * <pre>
	 * enum ColorEnum {
	 * 	RED, GREEN, BLUE
	 * }
	 * </pre>
	 * 
	 * than calling <code>addFactors("color",ColorEnum.class)</code> would be
	 * equivalent to manually adding three configurations:
	 * 
	 * <pre>
	 * addFactor(&quot;color&quot;, ColorEnum.RED);
	 * addFactor(&quot;color&quot;, ColorEnum.GREEN);
	 * addFactor(&quot;color&quot;, ColorEnum.BLUE);
	 * </pre>
	 * 
	 * @see #addFactor(String, Object)
	 * @param factorName name of the factor.
	 * @param enumClass  The enumeration, of which all members will be used as a
	 *                   value.
	 * @param <E>        Any enumeration type.
	 */
	public <E extends Enum<?>> void addFactors(String factorName, Class<E> enumClass) {
		for (Object enumValue : enumClass.getEnumConstants()) {
			addFactor(factorName, enumValue);
		}
	}

	/**
	 * Convenience method to add all elements in {@code values} as a possible value
	 * for a factor/property {@code factorName}. This method is equivalent to
	 * repeatedly calling {@link #addFactor(String, Object)} for each element in
	 * {@code values}.
	 * 
	 * @see #addFactor(String, Object)
	 * 
	 * @param factorName Name of the factor.
	 * @param values     Values to use for this factor.
	 */
	public void addFactors(String factorName, Object... values) {
		for (Object o : values) {
			addFactor(factorName, o);
		}
	}

	/**
	 * Convenience method to add all elements in {@code values} as a possible value
	 * for a factor/property {@code factorName}. This method is equivalent to
	 * repeatedly calling {@link #addFactor(String, Object)} for each element in
	 * {@code values}.
	 * 
	 * @see #addFactor(String, Object)
	 * @param factorName Name of the factor.
	 * @param values     A collection of values to use for this factor.
	 */
	public void addFactors(String factorName, Collection<?> values) {
		for (Object o : values) {
			addFactor(factorName, o);
		}
	}

	/**
	 * Returns a read-only collection of all factor names that are currently
	 * defined.
	 * 
	 * @return A collection of all factor names.
	 */
	public Collection<String> getFactorNames() {
		return Collections.unmodifiableSet(factors.keySet());
	}

	/**
	 * Returns a list with all values of a certain factor.
	 * 
	 * @param name A factor name.
	 * @return A list with all values for the given factor name.
	 */
	public List<?> getFactorValues(String name) {
		List<Object> res = factors.get(name);
		return res == null ? null : Collections.unmodifiableList(res);
	}

	@Override
	public FullFactorialExperiment clone() {
		FullFactorialExperiment e = (FullFactorialExperiment) super.clone();

		e.factors = new LinkedHashMap<String, List<Object>>(factors);

		return e;
	}

	@Override
	protected void createExperiments() {
		factorNames = new ArrayList<String>(getFactorNames());
		int numFactors = factorNames.size();
		int[] numValuesPerFactor = new int[numFactors];

		// calculate totals
		long total = 1;
		int i = 0;
		for (String name : factorNames) {
			int n = getFactorValues(name).size();
			numValuesPerFactor[i++] = n;

			long last = total;
			total *= n;
			// check for overflow
			if (total < last) {
				throw new RuntimeException("Too many combinations.");
			}
		}

		print(MsgCategory.INFO, "building and validating configurations, %d theoretical combinations ...", total);

		// create and add experiments
		int[] is = new int[numFactors];
		do {
			addExperimentForConf(is);
			if (getMaxConfigurations() > 0 && experiments.size() > getMaxConfigurations()) {
				throw new RuntimeException(String.format(I18n.DEF_LOCALE,
						"More than %d configurations. Consider reducing the number of factors and/or factor values or using an optimization algorithm instead.",
						getMaxConfigurations()));
			}
		} while (createNextCombination(is, numValuesPerFactor));

		print(MsgCategory.INFO, "executing %d experiments ...", experiments.size());

		factorNames = null;
	}

	private static boolean createNextCombination(int[] is, int[] numValuesPerFactor) {
		assert is.length == numValuesPerFactor.length;
		for (int i = is.length - 1; i >= 0; i--) {
			if (++is[i] >= numValuesPerFactor[i]) {
				is[i] = 0;
			} else {
				return true;
			}
		}

		return false;
	}

	protected void addExperimentForConf(int[] conf) {
		assert conf.length == factorNames.size();

		HashMap<String, Object> c = new HashMap<String, Object>();

		for (int f = 0; f < conf.length; f++) {
			String name = factorNames.get(f);
			Object value = factors.get(name).get(conf[f]);

			c.put(name, value);
		}

		Experiment e = createExperimentForConf(c);
		if (e != null) {
			experiments.add(e);
		}
	}

	// just make public, so it appears as a property in the GUI
	@Override
	public void setCommonRandomNumbers(boolean commonRandomNumbers) {
		super.setCommonRandomNumbers(commonRandomNumbers);
	}

	/**
	 * Returns the current setting for the maximum number of configurations to run.
	 * 
	 * @return The maximum number of configurations.
	 */
	public int getMaxConfigurations() {
		return maxConfigurations;
	}

	/**
	 * Sets the maximum number of configurations (i.e., sub-experiments) that are
	 * allowed to execute. The default value is 1,000,000. If there are more valid
	 * configurations/factor combinations, then the {@code FullFactorialExperiment}
	 * will abort in the initialization phase.
	 * 
	 * @param maxConfigurations The maximum number of configurations to allow.
	 */
	public void setMaxConfigurations(int maxConfigurations) {
		this.maxConfigurations = maxConfigurations;
	}

}
