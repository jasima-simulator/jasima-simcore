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
package jasima.core.experiment;

import jasima.core.util.Util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Systematically tests all possible combinations of various factors and their
 * values on a base experiment.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version 
 *          "$Id$"
 */
public class FullFactorialExperiment extends AbstractMultiExperiment {

	private static final long serialVersionUID = -7045310595384248793L;

	/**
	 * Allows finer control of the way a base experiment is configured than the
	 * usual mechanism using JavaBean properties. If an object implementing
	 * ComplexFactorSetter is passed as a value when calling
	 * {@link FullFactorialExperiment#addFactor(String, Object)
	 * addFactor(String, Object)} then instead of setting a bean property the
	 * method {@link #configureExperiment(Experiment)} is called.
	 * 
	 * @see FullFactorialExperiment#addFactor(String, Object)
	 */
	public interface ComplexFactorSetter extends Serializable {
		/**
		 * Configures an experiment.
		 * 
		 * @param e
		 *            The experiment to configure.
		 */
		void configureExperiment(final Experiment e);
	}

	/**
	 * An (optional) way to veto certain configurations, because some
	 * combinations of factors and their values might not make sense.
	 * 
	 * @see FullFactorialExperiment#setConfigurationValidator(ConfigurationValidator)
	 */
	public interface ConfigurationValidator extends Serializable {
		boolean isValid(Map<String, Object> configuration);
	}

	// parameters

	private ArrayList<String> factorNames;
	private Map<String, List<Object>> factors;
	private Experiment baseExperiment;
	private ConfigurationValidator configurationValidator = null;

	// fields used during run

	protected int numConfs = 0;

	public FullFactorialExperiment() {
		super();
		factors = new LinkedHashMap<String, List<Object>>();
		factorNames = new ArrayList<String>();
	}

	@Override
	public void init() {
		numConfs = 0;
		super.init();
	}

	@Override
	protected void createExperiments() {
		print("building configurations ...");

		int numFactors = getFactorNames().size();
		int[] numValuesPerFactor = new int[numFactors];
		ArrayList<String> factorNames = new ArrayList<String>(getFactorNames());

		// calculate totals
		int i = 0;
		for (String name : factorNames) {
			int n = getFactorValues(name).size();
			numValuesPerFactor[i++] = n;
		}

		// create and add experiments
		int[] is = new int[numFactors];
		do {
			addExperimentForConf(is);
		} while (createNextCombination(is, numValuesPerFactor));

		print("executing experiments ...");
	}

	private static boolean createNextCombination(int[] is,
			int[] numValuesPerFactor) {
		assert is.length == numValuesPerFactor.length;
		for (int i = is.length - 1; i >= 0; i--) {
			if (++is[i] >= numValuesPerFactor[i]) {
				is[i] = 0;
			} else
				return true;
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

		if (getConfigurationValidator() == null
				|| getConfigurationValidator().isValid(c)) {
			numConfs++;
			experiments.add(createExperimentForConf(c));
		}
	}

	protected Experiment createExperimentForConf(HashMap<String, Object> conf) {
		Experiment e = getBaseExperiment().silentClone();
		configureRunExperiment(e);

		for (String name : conf.keySet()) {
			Object value = conf.get(name);

			if (value != null && value instanceof ComplexFactorSetter) {
				((ComplexFactorSetter) value).configureExperiment(e);
			} else
				Util.setProperty(e, name, Util.cloneIfPossible(value));
		}

		return e;
	}

	@Override
	protected final String prefix() {
		return "conf";
	}

	/**
	 * Returns the number of experiment configurations to be executed.
	 */
	@Override
	public int getNumExperiments() {
		return numConfs;
	}

	/**
	 * Clears all configurations previously added using addFactor.
	 */
	public void clearFactors() {
		factors.clear();
		factorNames.clear();
	}

	/**
	 * Adds a configuration to test.
	 * 
	 * @param name
	 *            The factor name. This should be the name of a Java Beans
	 *            Property of the base experiment, otherwise execution of the
	 *            experiment will fail. In case {@code value} is a
	 *            {@link ComplexFactorSetter}, name can be arbitrary.
	 * @param value
	 *            The value to test for factor {@code name}.
	 */
	public void addFactor(String name, Object value) {
		List<Object> values = factors.get(name);
		if (values == null) {
			values = new ArrayList<Object>();
			factors.put(name, values);
			factorNames.add(name);
		}

		if (values.contains(value))
			throw new IllegalArgumentException("No duplicate values allowed: '"
					+ value + "' (factor '" + name + "')");

		values.add(value);
	}

	/**
	 * Convenience method to set a factor "factorName" for all possible values
	 * of an enumeration.
	 * <p />
	 * 
	 * Say an experiment has a property "color" of type ColorEnum
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
	 * addFactor(&quot;color&quot;, RED);
	 * addFactor(&quot;color&quot;, GREEN);
	 * addFactor(&quot;color&quot;, BLUE);
	 * </pre>
	 * 
	 * @see #addFactor(String, Object)
	 */
	public <E extends Enum<?>> void addFactors(String factorName,
			Class<E> enumClass) {
		for (Object enumValue : enumClass.getEnumConstants()) {
			addFactor(factorName, enumValue);
		}
	}

	/**
	 * Convenience method to add all elements in {@code values} as a possible
	 * value for a factor/property {@code factorName}. This method is equivalent
	 * to repeatedly calling {@link #addFactor(String, Object)} for each element
	 * in {@code values}.
	 * 
	 * @see #addFactor(String, Object)
	 */
	public void addFactors(String factorName, Object... values) {
		for (Object o : values) {
			addFactor(factorName, o);
		}
	}

	/**
	 * Convenience method to add all elements in {@code values} as a possible
	 * value for a factor/property {@code factorName}. This method is equivalent
	 * to repeatedly calling {@link #addFactor(String, Object)} for each element
	 * in {@code values}.
	 * 
	 * @see #addFactor(String, Object)
	 */
	public void addFactors(String factorName, Collection<?> values) {
		for (Object o : values) {
			addFactor(factorName, o);
		}
	}

	/**
	 * Return the number of factors.
	 */
	public int getNumFactors() {
		return factors.size();
	}

	/**
	 * Returns a list of all factors.
	 */
	public List<String> getFactorNames() {
		return Collections.unmodifiableList(factorNames);
	}

	/**
	 * Returns a list with all values of a certain factor.
	 */
	public List<?> getFactorValues(String name) {
		List<Object> res = factors.get(name);
		return res == null ? null : Collections.unmodifiableList(res);
	}

	public Experiment getBaseExperiment() {
		return baseExperiment;
	}

	/**
	 * Sets the base experiment that is executed multiple times.
	 */
	public void setBaseExperiment(Experiment baseExperiment) {
		this.baseExperiment = baseExperiment;
	}

	public ConfigurationValidator getConfigurationValidator() {
		return configurationValidator;
	}

	public void setConfigurationValidator(
			ConfigurationValidator configurationValidator) {
		this.configurationValidator = configurationValidator;
	}

	@Override
	public FullFactorialExperiment clone() throws CloneNotSupportedException {
		FullFactorialExperiment e = (FullFactorialExperiment) super.clone();

		if (baseExperiment != null)
			e.baseExperiment = baseExperiment.clone();

		e.factors = new LinkedHashMap<String, List<Object>>(factors);

		return e;
	}

}
