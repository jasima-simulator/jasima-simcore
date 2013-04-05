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
package jasima.shopSim.util;

import jasima.core.random.continuous.DblConst;
import jasima.core.random.continuous.DblDistribution;
import jasima.core.random.continuous.DblStream;
import jasima.core.random.continuous.DblUniformRange;
import jasima.core.random.discrete.IntEmpirical;
import jasima.core.random.discrete.IntUniformRange;
import jasima.core.simulation.arrivalprocess.ArrivalsStationary;
import jasima.core.util.Pair;
import jasima.core.util.Util;
import jasima.shopSim.core.DynamicJobSource;
import jasima.shopSim.core.Job;
import jasima.shopSim.core.JobShop;
import jasima.shopSim.core.Operation;
import jasima.shopSim.core.Route;
import jasima.shopSim.core.StaticJobSource;
import jasima.shopSim.core.StaticJobSource.JobSpec;
import jasima.shopSim.core.WorkStation;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.commons.math3.distribution.ExponentialDistribution;

/**
 * This class configures a StaticShopModel based on the contents of a text file.
 * This class is used mostly for test purposes. For examples of the file
 * structure see the examples in the directory "testInstances".
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 */
public class TextFileReader {

	static class RouteSpec {
		public double[] procTimes;
		public int[] machSpec;
		public String[] setups;
		public String[] batchFamilies;
		public int[] batchSizes;
	}

	private static final String JOB_SECT_MARKER = "jobs";
	private static final String JOB_SECT_DYN_MARKER = "jobs_dynamic";
	private static final String NAME_MARKER = "machineName";
	private static final String SETUP_MATRIX_MARKER = "setup";
	private static final String NUM_IN_GROUP_MARKER = "numInGroup";
	private static final String MACHINE_RELEASE_MARKER = "machineReleaseDates";

	private static final String DEF_SETUP = "DEF_SETUP";

	private boolean haveData = false;

	private int numMachines;
	private double[][] machRelDates;
	private int numRoutes;
	private RouteSpec[] routeSpecs;
	private ArrayList<JobSpec[]> jobSpecs;
	private ArrayList<HashMap<String, Object>> jobSpecsDynamic;
	private HashMap<Pair<String, String>, Double>[] setupMatrices;
	private String[] name;
	private int[] numInGroup;

	public TextFileReader() {
		super();
	}

	private void clearData() {
		routeSpecs = null;
		jobSpecs = null;
		jobSpecsDynamic = null;
		setupMatrices = null;
		machRelDates = null;
		numMachines = numRoutes = -1;
		haveData = false;
	}

	public void readData(BufferedReader r) {
		try {
			numMachines = Integer.parseInt(Util.nextNonEmptyLine(r));
			numRoutes = Integer.parseInt(Util.nextNonEmptyLine(r));

			routeSpecs = new RouteSpec[numRoutes];
			for (int i = 0; i < numRoutes; i++) {
				RouteSpec rs = new RouteSpec();
				routeSpecs[i] = rs;
			}
			readMachOrderAndSetups(r);
			readProcTimesAndBatching(r);

			// read setups, optional
			String s = readMachineParams(r);

			// read jobs, optional
			jobSpecs = null;
			jobSpecsDynamic = null;
			while (s != null) {
				s = readJobs(r, s);
			}

			haveData = true;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected String readJobs(BufferedReader r, String s) throws IOException {
		if (JOB_SECT_MARKER.equalsIgnoreCase(s))
			s = readJobs(r);
		else if (JOB_SECT_DYN_MARKER.equalsIgnoreCase(s))
			s = readDynJobs(r);
		return s;
	}

	private void readProcTimesAndBatching(BufferedReader r) throws IOException {
		// read processing times and batch families for each operation
		String[][] tmp = Util.read2DimStrings(r, numRoutes);

		for (int i = 0; i < numRoutes; i++) {
			RouteSpec rs = routeSpecs[i];

			rs.procTimes = new double[tmp[i].length];
			rs.batchFamilies = new String[tmp[i].length];
			rs.batchSizes = new int[tmp[i].length];

			for (int j = 0; j < rs.procTimes.length; j++) {
				String[] dat = tmp[i][j].split(";");
				assert dat.length == 1 || dat.length == 3;

				// do we have a batch family?
				if (dat.length == 3) {
					rs.batchFamilies[j] = dat[1];
					rs.batchSizes[j] = Integer.parseInt(dat[2]);
				} else {
					rs.batchFamilies[j] = "BATCH_INCOMPATIBLE";
					rs.batchSizes[j] = 1;
				}

				// parse processing time
				rs.procTimes[j] = Double.parseDouble(dat[0]);
			}
		}
	}

	private void readMachOrderAndSetups(BufferedReader r) throws IOException {
		// read machine order and required setup states
		String[][] machTmp = Util.read2DimStrings(r, numRoutes);

		for (int i = 0; i < numRoutes; i++) {
			RouteSpec rs = routeSpecs[i];

			rs.machSpec = new int[machTmp[i].length];
			rs.setups = new String[rs.machSpec.length];

			for (int j = 0; j < rs.machSpec.length; j++) {
				String[] dat = machTmp[i][j].split(";");
				assert dat.length > 0 && dat.length <= 2;

				// do we have setups?
				if (dat.length == 2) {
					rs.setups[j] = dat[1];
				} else
					rs.setups[j] = DEF_SETUP;

				rs.machSpec[j] = Integer.parseInt(dat[0])-1;
			}
		}
	}

	private String readJobs(BufferedReader r) throws IOException {
		// read job specs, adjust route number to be a valid array index
		String s = Util.nextNonEmptyLine(r);
		if (s == null)
			return s;

		int numJobs = Integer.parseInt(s);
		JobSpec[] curJobs = new JobSpec[numJobs];

		String[][] rawVals = Util.read2DimStrings(r, numJobs);
		for (int n = 0; n < rawVals.length; n++) {
			String[] i = rawVals[n];

			int route = Integer.parseInt(i[0]);
			if (route < 1 || route > numRoutes)
				throw new IllegalArgumentException("Invalid route number "
						+ route);
			// adjust to zero-based route index
			route--;

			double rel = Double.parseDouble(i[1]);
			double due = Double.parseDouble(i[2]);
			double w = Double.parseDouble(i[3]);
			String name = i.length >= 5 ? i[4] : null;

			curJobs[n] = new JobSpec(route, rel, due, w, name);
		}

		if (jobSpecs == null)
			jobSpecs = new ArrayList<StaticJobSource.JobSpec[]>();

		jobSpecs.add(curJobs);

		return Util.nextNonEmptyLine(r);
	}

	private String readDynJobs(BufferedReader r) throws IOException {
		// read job specs, adjust route number to be a valid array index
		String s = Util.nextNonEmptyLine(r);
		if (s == null)
			return s;

		int route = -1;
		DblStream iats = null;
		DblStream dueDates = null;
		DblStream weights = null;
		int numJobs = -1;

		if (!"route".equalsIgnoreCase(s))
			throw new RuntimeException("parse error '" + s + "'");
		s = Util.nextNonEmptyLine(r);
		route = Integer.parseInt(s);
		if (route < 1 || route > numRoutes)
			throw new IllegalArgumentException("Invalid route number " + route);
		route--;// adjust to zero-based route index

		s = Util.nextNonEmptyLine(r);
		if (!"arrivals".equalsIgnoreCase(s))
			throw new RuntimeException("parse error '" + s + "'");
		s = Util.nextNonEmptyLine(r);
		iats = parseDblStream(s);

		s = Util.nextNonEmptyLine(r);
		if (!"due_dates".equalsIgnoreCase(s))
			throw new RuntimeException("parse error '" + s + "'");
		s = Util.nextNonEmptyLine(r);
		dueDates = parseDblStream(s);

		s = Util.nextNonEmptyLine(r);
		if (!"weights".equalsIgnoreCase(s))
			throw new RuntimeException("parse error '" + s + "'");
		s = Util.nextNonEmptyLine(r);
		weights = parseDblStream(s);

		// optional
		s = Util.nextNonEmptyLine(r);
		if ("numJobs".equalsIgnoreCase(s)) {
			s = Util.nextNonEmptyLine(r);
			numJobs = Integer.parseInt(s);
			s = Util.nextNonEmptyLine(r);
		}

		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("route", route);
		hm.put("iats", iats);
		hm.put("dueDates", dueDates);
		hm.put("weights", weights);
		hm.put("numJobs", numJobs);

		if (jobSpecsDynamic == null)
			jobSpecsDynamic = new ArrayList<HashMap<String, Object>>();
		jobSpecsDynamic.add(hm);

		return s;
	}

	private DblStream parseDblStream(String s) {
		StringTokenizer sst = new StringTokenizer(s, "()", false);
		ArrayList<String> ss = new ArrayList<String>();
		while (sst.hasMoreTokens()) {
			ss.add(sst.nextToken().trim());
		}
		if (ss.size() != 2)
			throw new RuntimeException("invalid stream configuration '" + s
					+ "'");

		String type = ss.get(0);
		String parms = ss.get(1);

		DblStream stream = null;

		if ("const".equalsIgnoreCase(type)) {
			double[] ll = Util.parseDblList(parms);
			stream = new DblConst(ll);
		} else if ("emp".equalsIgnoreCase(type)) {
			ArrayList<Pair<Integer, Double>> l = new ArrayList<Pair<Integer, Double>>();
			StringTokenizer st = new StringTokenizer(parms, "<");
			while (st.hasMoreTokens()) {
				String v = st.nextToken().replace(">", "").trim();
				String[] vv = v.split(",");
				int v1 = Integer.parseInt(vv[0]);
				double p1 = Double.parseDouble(vv[1]);
				l.add(new Pair<Integer, Double>(v1, p1));
			}

			double[] probs = new double[l.size()];
			int[] values = new int[l.size()];
			for (int i = 0; i < l.size(); i++) {
				Pair<Integer, Double> p = l.get(i);
				values[i] = p.a;
				probs[i] = p.b;
			}

			stream = new IntEmpirical(probs, values);
		} else if ("dblUnif".equalsIgnoreCase(type)) {
			double[] ll = Util.parseDblList(parms);
			if (ll.length == 2)
				stream = new DblUniformRange(ll[0], ll[1]);
		} else if ("intUnif".equalsIgnoreCase(type)) {
			int[] ll = Util.parseIntList(parms);
			if (ll.length == 2)
				stream = new IntUniformRange(ll[0], ll[1]);
		} else if ("dblExp".equalsIgnoreCase(type)) {
			double d = Double.parseDouble(parms);
			stream = new DblDistribution(new ExponentialDistribution(d));
		}

		if (stream == null)
			throw new RuntimeException("invalid stream configuration '" + s
					+ "'");

		return stream;
	}

	private String readMachineParams(BufferedReader r) throws IOException {
		// setup times
		setupMatrices = new HashMap[numMachines];

		name = new String[numMachines];
		numInGroup = new int[numMachines];
		machRelDates = new double[numMachines][];

		String sm = Util.nextNonEmptyLine(r);
		int[] ms = null;
		while (sm != null && !JOB_SECT_MARKER.equalsIgnoreCase(sm)
				&& !JOB_SECT_DYN_MARKER.equalsIgnoreCase(sm)) {
			if (SETUP_MATRIX_MARKER.equals(sm)) {
				HashMap<Pair<String, String>, Double> matrix = readSetupMatrix(r);
				for (int m = 0; m < ms.length; m++) {
					setupMatrices[ms[m] - 1] = matrix;
				}
			} else if (NAME_MARKER.equals(sm)) {
				String name = Util.nextNonEmptyLine(r);
				for (int m = 0; m < ms.length; m++) {
					this.name[ms[m] - 1] = name;
				}
			} else if (NUM_IN_GROUP_MARKER.equals(sm)) {
				int inGroup = Integer.parseInt(Util.nextNonEmptyLine(r));
				for (int m = 0; m < ms.length; m++) {
					this.numInGroup[ms[m] - 1] = inGroup;
				}
			} else if (MACHINE_RELEASE_MARKER.equals(sm)) {
				String[] ss = Util.nextNonEmptyLine(r).trim().split("\\s+");

				double[] rds = new double[ss.length];
				for (int i = 0; i < ss.length; i++) {
					rds[i] = Double.parseDouble(ss[i]);
				}

				for (int m = 0; m < ms.length; m++) {
					this.machRelDates[ms[m] - 1] = rds.clone();
				}
			} else
				ms = Util.parseIntList(sm);

			sm = Util.nextNonEmptyLine(r);
		}
		return sm;
	}

	public static HashMap<Pair<String, String>, Double> readSetupMatrix(
			BufferedReader r) throws IOException {
		int numStates = Integer.parseInt(Util.nextNonEmptyLine(r));
		String[][] spec = Util.read2DimStrings(r, numStates);

		HashMap<Pair<String, String>, Double> matrix = new HashMap<Pair<String, String>, Double>();
		for (int n = 0; n < numStates; n++) {
			for (int n2 = 0; n2 < numStates; n2++) {
				Pair<String, String> pair = new Pair<String, String>(
						spec[n][0], spec[n2][0]);

				String s = spec[n][n2 + 1];
				double val = !"x".equalsIgnoreCase(s) ? Double.parseDouble(s)
						: 0.0d;

				matrix.put(pair, val);
			}
		}
		return matrix;
	}

	public static void configureModel(JobShop shop, BufferedReader rd) {
		TextFileReader tfr = new TextFileReader();
		tfr.readData(rd);
		tfr.configureMdl(shop);
		tfr.clearData();
	}

	public void configureMdl(JobShop shop) {
		if (!haveData)
			throw new IllegalStateException("No data read yet.");

		// create all machines
		for (int i = 0; i < numMachines; i++) {
			int groupSize = numInGroup[i] == 0 ? 1 : numInGroup[i];

			WorkStation m = new WorkStation(groupSize);
			m.setName(name[i]);
			shop.addMachine(m);

			if (setupMatrices[i] != null)
				m.setSetupMatrix(createSetupMatrix(setupMatrices[i], m));

			if (machRelDates[i] != null) {
				double[] rds = machRelDates[i];
				if (rds.length != groupSize)
					throw new IllegalStateException(
							"Invallid number of machine release dates, found: "
									+ rds.length + ", expected: " + groupSize);
				for (int n = 0; n < groupSize; n++) {
					m.machDat[n].relDate = rds[n];
				}
			}
		}

		// create all routes
		Route[] routes = new Route[numRoutes];
		for (int i = 0; i < numRoutes; i++) {
			Route r = new Route();

			initOperations(shop, r, routeSpecs[i]);

			// machSpec[i], procTimes[i], setups[i],
			// batchFamilies[i], batchSizes[i]);

			routes[i] = r;
		}
		shop.routes = routes;

		// create job source if jobs specified in file
		if (jobSpecs != null) {
			for (JobSpec[] js : jobSpecs) {
				StaticJobSource s = new StaticJobSource();
				s.jobs = js.clone();
				shop.addJobSource(s);
			}
		}

		// create dynamic job source if jobs specified in file
		if (jobSpecsDynamic != null) {
			for (HashMap<String, Object> hm : jobSpecsDynamic) {
				int route = (Integer) hm.get("route");
				DblStream iats = (DblStream) hm.get("iats");
				DblStream dueDates = (DblStream) hm.get("dueDates");
				DblStream weights = (DblStream) hm.get("weights");
				final int numJobs = (Integer) hm.get("numJobs");

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
						if (numJobs > 0 && numCreated >= numJobs)
							stopArrivals = true;

						return j;
					}
				};
				ds.setRoute(shop.routes[route]);

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

				shop.addJobSource(ds);
			}
		}
	}

	private double[][] createSetupMatrix(
			HashMap<Pair<String, String>, Double> hashMap, WorkStation m) {
		int matrixDim = (int) Math.sqrt(hashMap.size());
		assert matrixDim * matrixDim == hashMap.size();

		double[][] res = new double[matrixDim][];
		for (int i = 0; i < matrixDim; i++) {
			res[i] = new double[matrixDim];
		}

		for (Pair<String, String> entry : hashMap.keySet()) {
			int fromState = m.translateSetupState(entry.a);
			int toState = m.translateSetupState(entry.b);

			res[fromState][toState] = hashMap.get(entry);
		}

		return res;
	}

	private void initOperations(JobShop shop, Route r, RouteSpec rs
	// int[] is,
	// double[] procTimes, String[] setups, String[] batchFamilies,
	// int[] batchSizes
	) {
		if (rs.machSpec.length != rs.procTimes.length)
			throw new IllegalArgumentException(
					"time for operation missing or vice versa");
		if (rs.machSpec.length != rs.setups.length)
			throw new IllegalArgumentException(
					"setup state for operation missing or vice versa");

		for (int i = 0; i < rs.procTimes.length; i++) {
			Operation o = new Operation();

			o.machine = shop.machines[rs.machSpec[i]];
			o.procTime = rs.procTimes[i];

			// assert setupStateTranslate[o.machine.index].contains(setups[i]);
			o.setupState = o.machine.translateSetupState(rs.setups[i]);

			o.batchFamily = rs.batchFamilies[i];
			o.maxBatchSize = rs.batchSizes[i];

			r.addSequentialOperation(o);
		}
	}

	public JobSpec getJobSpec(int jobSpec, int idx) {
		return jobSpecs.get(jobSpec)[idx];
	}

	public int getNumJobs(int jobSpec) {
		return jobSpecs.get(jobSpec).length;
	}

	public int getNumMachines() {
		return numMachines;
	}

	public int getNumInGroup(int machine) {
		return numInGroup[machine] == 0 ? 1 : numInGroup[machine];
	}

	public double getMachineReleaseDate(int m, int machInGroup) {
		if (machRelDates == null || machRelDates[m] == null)
			return 0.0;
		return machRelDates[m][machInGroup];
	}

	public int getNumRoutes() {
		return numRoutes;
	}

	public int[] getMachineOrder(int route) {
		RouteSpec rs = routeSpecs[route];
		int[] res = new int[rs.machSpec.length];
		for (int i = 0; i < res.length; i++) {
			res[i] = rs.machSpec[i];
		}
		return res;
	}

	public double[] getProcTimes(int route) {
		RouteSpec rs = routeSpecs[route];
		double[] res = new double[rs.procTimes.length];
		for (int i = 0; i < res.length; i++) {
			res[i] = rs.procTimes[i];
		}
		return res;
	}
}
