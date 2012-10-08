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

import jasima.core.util.Pair;
import jasima.core.util.Util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Systematically tests all possible combinations of various factors and their
 * values on a base experiment.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version $Id$
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

	private Map<String, List<Object>> factors;
	private Experiment baseExperiment;
	private ConfigurationValidator configurationValidator = null;

	// fields used during run
	protected int numConfs = 0;

	public FullFactorialExperiment() {
		super();
		factors = new LinkedHashMap<String, List<Object>>();
	}

	@Override
	public void init() {
		numConfs = 0;
		super.init();
	}

	@Override
	protected void createExperiments() {
		print("building configurations ...");

		ArrayList<String> nameList = new ArrayList<String>(getFactorNames());
		ArrayList<Pair<String, Object>> conf = new ArrayList<Pair<String, Object>>();
		buildConfigurations(nameList, 0, conf);
	}

	private void buildConfigurations(ArrayList<String> nameList, int i,
			ArrayList<Pair<String, Object>> conf) {
		if (i == nameList.size()) {
			handleConfig(conf);
			return;
		}

		String name = nameList.get(i);
		conf.add(null); // later replaced by a Pair

		Iterator<?> values = getFactorValues(name).iterator();
		while (values.hasNext()) {
			conf.set(i, new Pair<String, Object>(name, values.next()));
			buildConfigurations(nameList, i + 1, conf);
		}

		conf.remove(i);
	}

	protected void handleConfig(ArrayList<Pair<String, Object>> conf) {
		if (isValidConfiguration(conf)) {
			numConfs++;
			experiments.add(createExperimentForConf(conf));
		}
	}

	protected Experiment createExperimentForConf(
			ArrayList<Pair<String, Object>> conf) {
		Experiment e = getBaseExperiment().silentClone();
		configureRunExperiment(e);

		for (Pair<String, Object> p : conf) {
			if (p.b != null && p.b instanceof ComplexFactorSetter) {
				((ComplexFactorSetter) p.b).configureExperiment(e);
			} else
				Util.setProperty(e, p.a, Util.cloneIfPossible(p.b));
		}

		return e;
	}

	protected boolean isValidConfiguration(ArrayList<Pair<String, Object>> conf) {
		if (getConfigurationValidator() == null)
			return true;

		HashMap<String, Object> c = new HashMap<String, Object>();
		for (Pair<String, Object> keyValue : conf) {
			assert c.get(keyValue.a) == null; // not yet present
			c.put(keyValue.a, keyValue.b);
		}

		return getConfigurationValidator().isValid(c);
	}

	@Override
	protected void storeRunResults(Experiment e, Map<String, Object> r) {
		super.storeRunResults(e, r);
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

	public Collection<String> getFactorNames() {
		return factors.keySet();
	}

	public Collection<?> getFactorValues(String name) {
		List<Object> res = factors.get(name);
		return res == null ? null : Collections.unmodifiableCollection(res);
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
