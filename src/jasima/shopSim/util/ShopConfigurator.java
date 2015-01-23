/*******************************************************************************
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This file is part of jasima, v1.2.
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
package jasima.shopSim.util;

import jasima.core.random.continuous.DblStream;
import jasima.core.simulation.arrivalprocess.ArrivalsStationary;
import jasima.shopSim.core.DynamicJobSource;
import jasima.shopSim.core.IndividualMachine;
import jasima.shopSim.core.Job;
import jasima.shopSim.core.JobShop;
import jasima.shopSim.core.JobSource;
import jasima.shopSim.core.Operation;
import jasima.shopSim.core.Route;
import jasima.shopSim.core.StaticJobSource;
import jasima.shopSim.core.StaticJobSource.JobSpec;
import jasima.shopSim.core.WorkStation;
import jasima.shopSim.util.modelDef.DynamicSourceDef;
import jasima.shopSim.util.modelDef.IndividualMachineDef;
import jasima.shopSim.util.modelDef.JobDef;
import jasima.shopSim.util.modelDef.OperationDef;
import jasima.shopSim.util.modelDef.RouteDef;
import jasima.shopSim.util.modelDef.ShopDef;
import jasima.shopSim.util.modelDef.SourceDef;
import jasima.shopSim.util.modelDef.StaticSourceDef;
import jasima.shopSim.util.modelDef.WorkstationDef;

import java.util.Arrays;

public class ShopConfigurator {

	private ShopDef shopDef;

	public ShopConfigurator() {
		super();
	}

	public static void configureModel(JobShop shop, ShopDef sd) {
		ShopConfigurator tfr = sd.getShopConfigurator();
		tfr.setShopDef(sd);
		tfr.configureMdl(shop);
	}

	public void configureMdl(JobShop shop) {
		shop.setName(shopDef.getName());
		shop.setSimulationLength(shopDef.getSimulationLength());
		shop.setEnableLookAhead(shopDef.isEnableLookAhead());
		shop.setStopAfterNumJobs(shopDef.getStopAfterNumJobs());
		shop.setMaxJobsInSystem(shopDef.getMaxJobsInSystem());

		// create all machines
		for (WorkstationDef wd : shopDef.getWorkstations()) {
			int groupSize = 1;
			if (wd.getMachines() != null && wd.getMachines().length > 0) {
				groupSize = wd.getMachines().length;
			}

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
				IndividualMachineDef imd = wd.getMachines() != null ? wd
						.getMachines()[i] : new IndividualMachineDef();

				im.relDate = imd.getMachRelDate();
				im.initialSetup = m.translateSetupState(imd.getInitialSetup());
				im.name = imd.getName();
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

	private JobSource createDynamicSource(JobShop shop, DynamicSourceDef sd) {
		int route = indexOf(sd.getRoute(), shopDef.getRoutes());
		DblStream iats = sd.getIats().createStream();
		DblStream dueDates = sd.getDueDates().createStream();
		DblStream weights = sd.getWeights().createStream();
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

		try {
			ArrivalsStationary arrivals = new ArrivalsStationary();
			arrivals.setArrivalAtTimeZero(false);
			arrivals.setInterArrivalTimes(iats.clone());

			ds.setArrivalProcess(arrivals);
			ds.setDueDateFactors(dueDates.clone());
			ds.setJobWeights(weights.clone());
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
		
		return ds;
	}

	private StaticJobSource createStaticSource(StaticSourceDef ssd) {
		StaticJobSource s = new StaticJobSource();

		JobDef[] jobDefs = ssd.getJobSpecs();
		JobSpec[] jss = new JobSpec[jobDefs.length];
		for (int i = 0; i < jobDefs.length; i++) {
			JobDef jd = jobDefs[i];
			JobSpec js = new JobSpec(
					indexOf(jd.getRoute(), shopDef.getRoutes()),
					jd.getReleaseDate(), jd.getDueDate(), jd.getWeight(),
					jd.getName());
			jss[i] = js;
		}
		s.jobs = jss;
		return s;
	}

	private int indexOf(Object route, Object[] routes) {
		return Arrays.asList(routes).indexOf(route);
	}

	private void initOperations(JobShop shop, Route r, RouteDef rd) {
		int numOps = rd.getOperations().length;

		for (int i = 0; i < numOps; i++) {
			OperationDef od = rd.getOperations()[i];
			Operation o = new Operation();

			// TODO: od.getName()
			o.machine = shop.machines[indexOf(od.getWorkstation(),
					shopDef.getWorkstations())];
			o.procTime = od.getProcTime();

			o.setupState = o.machine.translateSetupState(od.getSetup());

			o.batchFamily = od.getBatchFamily();
			o.maxBatchSize = od.getMaxBatchSize();

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
