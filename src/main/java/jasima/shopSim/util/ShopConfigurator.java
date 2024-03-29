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
package jasima.shopSim.util;

import jasima.core.random.continuous.DblSequence;
import jasima.core.simulation.Simulation;
import jasima.core.simulation.arrivalprocess.ArrivalsStationary;
import jasima.shopSim.core.DynamicJobSource;
import jasima.shopSim.core.IndividualMachine;
import jasima.shopSim.core.Job;
import jasima.shopSim.core.JobSource;
import jasima.shopSim.core.Operation;
import jasima.shopSim.core.Route;
import jasima.shopSim.core.Shop;
import jasima.shopSim.core.StaticJobSource;
import jasima.shopSim.core.StaticJobSource.JobSpec;
import jasima.shopSim.core.WorkStation;
import jasima.shopSim.util.modelDef.DynamicSourceDef;
import jasima.shopSim.util.modelDef.JobDef;
import jasima.shopSim.util.modelDef.OperationDef;
import jasima.shopSim.util.modelDef.RouteDef;
import jasima.shopSim.util.modelDef.ShopDef;
import jasima.shopSim.util.modelDef.SourceDef;
import jasima.shopSim.util.modelDef.StaticSourceDef;
import jasima.shopSim.util.modelDef.WorkstationDef;

public class ShopConfigurator {

	private ShopDef shopDef;

	public ShopConfigurator() {
		super();
	}

	public static void configureModel(Shop shop, ShopDef sd) {
		ShopConfigurator tfr = sd.getShopConfigurator();
		tfr.setShopDef(sd);
		tfr.configureMdl(shop);
	}

	public void configureSimulation(Simulation sim) {
		sim.setName(shopDef.getName());

		if (shopDef.getSimulationLength() >= 0.0)
			sim.setSimulationLength(shopDef.getSimulationLength());
	}

	public void configureMdl(Shop shop) {
		shop.setEnableLookAhead(shopDef.isEnableLookAhead());
		shop.setStopAfterNumJobs(shopDef.getStopAfterNumJobs());
		shop.setMaxJobsInSystem(shopDef.getMaxJobsInSystem());

		// create all machines
		for (WorkstationDef wd : shopDef.getWorkstations()) {
			int groupSize = wd.getNumInGroup();
			double[] rel = wd.getMachReleaseDates();
			String[] initialSetups = wd.getInitialSetups();

			WorkStation m = new WorkStation(groupSize);
			m.setName(wd.getName());
			shop.addMachine(m);

			if (wd.getSetupStates() != null) {
				int i = 0;
				for (String s : wd.getSetupStates()) {
					int idx = m.translateSetupState(s);
					assert i == idx;
					i++;
				}

				m.setSetupMatrix(wd.getSetupTimes());
			}

			for (int i = 0; i < groupSize; i++) {
				IndividualMachine im = m.machDat()[i];

				if (rel != null)
					im.relDate = rel[i];

				String setup = initialSetups != null ? initialSetups[i] : WorkStation.DEF_SETUP_STR;
				im.initialSetup = m.translateSetupState(setup);
			}
		}

		// create all routes
		int numRoutes = shopDef.getRoutes().length;
		Route[] routes = new Route[numRoutes];
		for (int i = 0; i < numRoutes; i++) {
			Route r = new Route();
			RouteDef rd = shopDef.getRoutes()[i];
			initOperations(shop, r, rd);
			routes[i] = r;
		}
		shop.routes = routes;

		// create job source if jobs specified in file
		for (SourceDef sd : shopDef.getJobSources()) {
			JobSource s = null;
			if (sd instanceof StaticSourceDef) {
				s = createStaticSource((StaticSourceDef) sd);
			} else {
				s = createDynamicSource(shop, (DynamicSourceDef) sd);
			}

			shop.addJobSource(s);
		}

	}

	private JobSource createDynamicSource(Shop shop, DynamicSourceDef sd) {
		int route = sd.getRoute();
		DblSequence iats = sd.getIats().createStream();
		DblSequence dueDates = sd.getDueDates().createStream();
		DblSequence weights = sd.getWeights().createStream();
		final int numJobs = sd.getNumJobs();

		DynamicJobSource ds = new DynamicJobSource() {

			private int numCreated;

			@Override
			public void init() {
				super.init();
				numCreated = 0;
			}

			@Override
			public Job createNextJob() {
				Job j = super.createNextJob();

				numCreated++;
				if (numJobs > 0 && numCreated > numJobs)
					stopArrivals = true;

				return j;
			}
		};
		ds.setRoute(shop.getRoutes()[route]);

		ArrivalsStationary arrivals = new ArrivalsStationary();
		arrivals.setArrivalAtTimeZero(false);
		arrivals.setInterArrivalTimes(iats.clone());

		ds.setArrivalProcess(arrivals);
		ds.setDueDateFactors(dueDates.clone());
		ds.setJobWeights(weights.clone());

		return ds;
	}

	private StaticJobSource createStaticSource(StaticSourceDef ssd) {
		StaticJobSource s = new StaticJobSource();

		JobDef[] jobDefs = ssd.getJobSpecs();
		JobSpec[] jss = new JobSpec[jobDefs.length];
		for (int i = 0; i < jobDefs.length; i++) {
			JobDef jd = jobDefs[i];
			JobSpec js = new JobSpec(jd.getRoute(), jd.getReleaseDate(), jd.getDueDate(), jd.getWeight(), jd.getName());
			jss[i] = js;
		}
		s.jobs = jss;
		return s;
	}

	private void initOperations(Shop shop, Route r, RouteDef rd) {
		int numOps = rd.getOperations().length;

		for (int i = 0; i < numOps; i++) {
			OperationDef od = rd.getOperations()[i];
			Operation o = new Operation();

			// TODO: od.getName()
			o.setMachine((WorkStation) shop.machines().getChild(od.getWorkstation()));
			o.setProcTime(od.getProcTime());
			// TODO: od.procTimeReal

			o.setSetupState(o.getMachine().translateSetupState(od.getSetup()));

			o.setBatchFamily(od.getBatchFamily());
			o.setMaxBatchSize(od.getMaxBatchSize());

			r.addSequentialOperation(o);
		}
	}

	public ShopDef getShopDef() {
		return shopDef;
	}

	public void setShopDef(ShopDef shopDef) {
		this.shopDef = shopDef;
	}
}
