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
package jasima.shopSim.models.mimac;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.math3.distribution.ExponentialDistribution;

import jasima.core.random.continuous.DblDistribution;
import jasima.core.random.continuous.DblSequence;
import jasima.core.simulation.arrivalprocess.ArrivalsStationary;
import jasima.shopSim.core.DynamicJobSource;
import jasima.shopSim.core.ShopExperiment;
import jasima.shopSim.util.TextFileReader;
import jasima.shopSim.util.modelDef.ShopDef;

/**
 * Implements simulations of the MIMAC Scenarios.
 * 
 * @author Torsten Hildebrandt, 2010-03-12
 */
public class MimacExperiment extends ShopExperiment {

	private static final long serialVersionUID = -1460963355772995049L;

	public static enum DataSet {
		FAB4("fab4.txt", new double[] { 1440d / 4.07999048, 1440d / 0.447999403 }), FAB4r("fab4r.txt",
				new double[] { 1440d / 4.07999048, 1440d / 0.447999403 }), FAB6("fab6.txt",
						new double[] { 1440d / 0.7875, 1440d / 0.258125, 1440d / 0.54641667, 1440d / 0.65891667,
								1440d / 2.196375, 1440d / 1.39279167, 1440d / 0.58391667, 1440d / 0.50358333,
								1440d / 0.68033333 });

		public final String resourceName;
		public final double[] defaultIats;

		private ShopDef def = null;

		DataSet(String resoureName, double[] defaultIats) {
			this.resourceName = resoureName;
			this.defaultIats = defaultIats;
		}

		public synchronized ShopDef getShopDef() {
			ShopDef def = this.def;

			if (def == null) {
				TextFileReader reader = createNewReader();
				def = reader.getShopDef();
			}

			return def;
		}

		private TextFileReader createNewReader() {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();

			String name = resourceName;
			String baseName = MimacExperiment.class.getName();
			int index = baseName.lastIndexOf('.');
			if (index != -1) {
				name = baseName.substring(0, index).replace('.', '/') + "/" + name;
			}

			InputStream inp = cl.getResourceAsStream(name);
			if (inp == null)
				throw new RuntimeException("Can't find input stream '" + name + "'.");
			BufferedReader in = new BufferedReader(new InputStreamReader(inp));
			TextFileReader reader = new TextFileReader();
			reader.readData(in);

			try {
				in.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			return reader;
		}

	};

	private DataSet scenario;
	private DblSequence jobWeights;
	private DblSequence dueDateFactors;
	private DblSequence[] interArrivalTimes;
	private boolean arrivalAtTimeZero = false;

	@Override
	protected void createShop() {
		super.createShop();

		// configure model from file
		getScenario().getShopDef().getShopConfigurator().configureMdl(shop);

		// create job sources
		DblSequence[] iats = getInterArrivalTimes();
		if (iats != null && shop.routes.length != iats.length) {
			throw new RuntimeException("Number of routes (" + shop.routes.length + ") and inter-arrival streams ("
					+ iats.length + ") doesn't match.");
		}

		// create job sources for each route, i.e., product
		for (int i = 0; i < shop.routes.length; i++) {
			DynamicJobSource s = new DynamicJobSource();
			s.setRoute(shop.routes[i]);

			ArrivalsStationary arrivals = new ArrivalsStationary();
			arrivals.setArrivalAtTimeZero(isArrivalAtTimeZero());
			if (iats != null && iats[i] != null) {
				arrivals.setInterArrivalTimes(iats[i]);
			} else {
				arrivals.setInterArrivalTimes(
						new DblDistribution(new ExponentialDistribution(getScenario().defaultIats[i])));
			}
			s.setArrivalProcess(arrivals);

			if (getDueDateFactors() != null)
				s.setDueDateFactors(getDueDateFactors());

			if (getJobWeights() != null)
				s.setJobWeights(getJobWeights());

			shop.addJobSource(s);
		}
	}

	@Override
	protected void configureShop() {
		super.configureShop();
	}

	@Override
	public MimacExperiment clone() {
		MimacExperiment c = (MimacExperiment) super.clone();

		if (jobWeights != null)
			c.jobWeights = jobWeights.clone();

		if (dueDateFactors != null)
			c.dueDateFactors = dueDateFactors.clone();

		if (interArrivalTimes != null) {
			c.interArrivalTimes = new DblSequence[interArrivalTimes.length];
			for (int i = 0; i < interArrivalTimes.length; i++) {
				c.interArrivalTimes[i] = interArrivalTimes[i].clone();
			}
		}

		return c;
	}

	//
	//
	// boring getters and setters for parameters below
	//
	//

	public void setScenario(DataSet scenario) {
		this.scenario = scenario;
	}

	public DataSet getScenario() {
		return scenario;
	}

	public void setInterArrivalTimes(DblSequence[] interArrivalTimes) {
		this.interArrivalTimes = interArrivalTimes;
	}

	public DblSequence[] getInterArrivalTimes() {
		return interArrivalTimes;
	}

	public void setJobWeights(DblSequence jobWeights) {
		this.jobWeights = jobWeights;
	}

	public DblSequence getJobWeights() {
		return jobWeights;
	}

	public void setDueDateFactors(DblSequence dueDateFactors) {
		this.dueDateFactors = dueDateFactors;
	}

	public DblSequence getDueDateFactors() {
		return dueDateFactors;
	}

	public void setArrivalAtTimeZero(boolean arrivalAtTimeZero) {
		this.arrivalAtTimeZero = arrivalAtTimeZero;
	}

	public boolean isArrivalAtTimeZero() {
		return arrivalAtTimeZero;
	}

}
