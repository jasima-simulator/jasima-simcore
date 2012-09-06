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

import jasima.core.util.MersenneTwister;
import jasima.core.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * Allows to run a certain maximum number of experiments chosen randomly. This
 * number of experiments run is usually smaller than the number of possible
 * factor combinations, i.e., only a fraction of possible designs is tested.
 * 
 * Torsten Hildebrandt <hil@biba.uni-bremen.de>, 2012-06-08
 */
public class RandomFractionalExperiment extends FullFactorialExperiment {

	private static final long serialVersionUID = 6227676813209467282L;

	// attributes
	private int maxConfigurations = 0;

	// fields used during run
	private Random rnd;
	private double threshold;
	private PriorityQueue<Pair<Double, Experiment>> data;

	public RandomFractionalExperiment() {
		super();
	}

	public RandomFractionalExperiment(int maxConfigurations) {
		this();

		setMaxConfigurations(maxConfigurations);
	}

	@Override
	public void init() {
		if (maxConfigurations <= 0)
			throw new IllegalArgumentException(
					"'maxConfigurations' has to be >=1.");

		super.init();

		rnd = new MersenneTwister(getInitialSeed());
		data = new PriorityQueue<Pair<Double, Experiment>>(maxConfigurations,
				new Comparator<Pair<Double, Experiment>>() {
					@Override
					public int compare(Pair<Double, Experiment> o1,
							Pair<Double, Experiment> o2) {
						return -Double.compare(o1.a, o2.a);
					}
				});
	}

	@Override
	protected void createExperiments() {
		super.createExperiments();

		for (Pair<Double, Experiment> e : data) {
			experiments.add(e.b);
		}

		data = null;
	}

	@Override
	protected void handleConfig(ArrayList<Pair<String, Object>> conf) {
		if (isValidConfiguration(conf)) {
			numConfs++;

			// accept this configuration?
			double v = rnd.nextDouble();
			if (v < threshold || data.size() < maxConfigurations) {
				if (data.size() == maxConfigurations) {
					Pair<Double, Experiment> del = data.poll();
					assert del.a == threshold;
				}

				data.add(new Pair<Double, Experiment>(v,
						createExperimentForConf(conf)));

				threshold = data.peek().a;
			}
		}
	}

	public int getMaxConfigurations() {
		return maxConfigurations;
	}

	public void setMaxConfigurations(int maxConfigurations) {
		this.maxConfigurations = maxConfigurations;
	}

}
