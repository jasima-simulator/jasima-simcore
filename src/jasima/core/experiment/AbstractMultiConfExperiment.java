/*******************************************************************************
 * Copyright (c) 2010-2013 Torsten Hildebrandt and jasima contributors
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
 *
 * $Id$
 *******************************************************************************/
package jasima.core.experiment;

import jasima.core.util.Util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Base class for experiments that execute variations of a
 * {@code baseExperiment} by changing its properties.
 * <p>
 * 
 * The order in which properties are applied is determined by the length of the
 * property name to guarantee that sub-properties are set after the object
 * containing them. This also applies when {@link ComplexFactorSetter} is used.
 * Properties with equally long names are executed in an undefined order.
 * Exceptions are null, which is regarded as having a length of -1, and
 * {@link #KEY_EXPERIMENT}, which is regarded has having a length of -2.
 * {@link #KEY_EXPERIMENT} can be present in any number of configurations. If it
 * is present in all configurations, baseExperiment need not be set.
 * 
 * @author Robin Kreis <r.kreis@uni-bremen.de>
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 */
public abstract class AbstractMultiConfExperiment extends
		AbstractMultiExperiment {

	private static final long serialVersionUID = 8651960788951812186L;

	public static final String KEY_EXPERIMENT = "@";

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

	private Experiment baseExperiment = null;
	private ConfigurationValidator configurationValidator = null;

	// fields used during run
	protected int numConfs = 0;

	@Override
	public void init() {
		numConfs = 0;

		if (getBaseExperiment() == null) {
			throw new IllegalArgumentException("Please set a base experiment.");
		}

		super.init();
	}

	protected abstract void createExperiments();

	protected void handleConfig(Map<String, Object> conf) {
		if (isValidConfiguration(conf)) {
			numConfs++;
			try {
				experiments.add(createExperimentForConf(conf));
			} catch (final Exception e) {
				print(ExpMsgCategory.ERROR, e.getMessage());
				experiments.add(new Experiment() {

					private static final long serialVersionUID = 4259612422796656502L;

					@Override
					protected void produceResults() {
						aborted++;
						super.produceResults();

						resultMap.put(Experiment.EXCEPTION_MESSAGE,
								e.getMessage());
						resultMap.put(Experiment.EXCEPTION,
								Util.exceptionToString(e));
					}

					@Override
					protected void performRun() {
						// do nothing
					}
				});
			}
		}
	}

	protected Experiment createExperimentForConf(Map<String, Object> conf) {
		Experiment e = conf.containsKey(KEY_EXPERIMENT) ? ((Experiment) conf
				.get(KEY_EXPERIMENT)).silentClone() : getBaseExperiment()
				.silentClone();
		configureRunExperiment(e);

		List<Map.Entry<String, Object>> entries = new ArrayList<Map.Entry<String, Object>>(
				conf.entrySet());
		// sort by length
		Collections.sort(entries, new Comparator<Map.Entry<String, Object>>() {
			@Override
			public int compare(Entry<String, Object> o1,
					Entry<String, Object> o2) {
				String a = o1.getKey();
				String b = o2.getKey();
				if (a == b)
					return 0;
				if (a == null)
					return -1;
				if (b == null)
					return 1;
				return a.length() - b.length();
			}
		});

		for (Map.Entry<String, Object> p : entries) {
			if (p.getKey().equals(KEY_EXPERIMENT))
				continue;
			if (p.getValue() != null
					&& p.getValue() instanceof ComplexFactorSetter) {
				((ComplexFactorSetter) p.getValue()).configureExperiment(e);
			} else {
				Util.setProperty(e, p.getKey(),
						Util.cloneIfPossible(p.getValue()));
			}
		}

		return e;
	}

	protected boolean isValidConfiguration(Map<String, Object> conf) {
		if (getConfigurationValidator() == null)
			return true;

		return getConfigurationValidator().isValid(conf);
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
	public AbstractMultiConfExperiment clone()
			throws CloneNotSupportedException {
		AbstractMultiConfExperiment e = (AbstractMultiConfExperiment) super
				.clone();

		if (baseExperiment != null)
			e.baseExperiment = baseExperiment.clone();

		return e;
	}
}
