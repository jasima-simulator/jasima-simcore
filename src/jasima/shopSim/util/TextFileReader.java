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

import jasima.core.util.Pair;
import jasima.core.util.Util;
import jasima.shopSim.core.JobShop;
import jasima.shopSim.core.Operation;
import jasima.shopSim.core.Route;
import jasima.shopSim.core.StaticJobSource;
import jasima.shopSim.core.StaticJobSource.JobSpec;
import jasima.shopSim.core.WorkStation;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * This class configures a StaticShopModel based on the contents of a text file.
 * This class is used mostly for test purposes. For examples of the file
 * structure see the examples in the directory "testInstances".
 * 
 * @author Torsten Hildebrandt
 * @version "$Id$"
 */
public class TextFileReader {

	private static final String JOB_SECT_MARKER = "jobs";
	private static final String NAME_MARKER = "machineName";
	private static final String SETUP_MATRIX_MARKER = "setup";
	private static final String NUM_IN_GROUP_MARKER = "numInGroup";
	private static final String MACHINE_RELEASE_MARKER = "machineReleaseDates";

	private static final String DEF_SETUP = "DEF_SETUP";

	private boolean haveData = false;

	private int numMachines;
	private double[][] procTimes;
	private int[][][] machSpec;
	private double[][] machRelDates;
	private String[][] setups;
	private String[][] batchFamilies;
	private int[][] batchSizes;
	private int numRoutes;
	private JobSpec[] jobSpecs;
	private HashMap<Pair<String, String>, Double>[] setupMatrices;
	private String[] name;
	private int[] numInGroup;

	public TextFileReader() {
		super();
	}

	private void clearData() {
		procTimes = null;
		machSpec = null;
		setups = null;
		batchFamilies = null;
		batchSizes = null;
		jobSpecs = null;
		setupMatrices = null;

		numMachines = numRoutes = -1;

		haveData = false;
	}

	public void readData(BufferedReader r) {
		try {
			numMachines = Integer.parseInt(Util.nextNonEmptyLine(r));
			numRoutes = Integer.parseInt(Util.nextNonEmptyLine(r));

			readMachOrderAndSetups(r);

			readProcTimesAndBatching(r);

			// read setups, optional
			String s = readMachineParams(r);

			// read jobs, optional
			if (JOB_SECT_MARKER.equalsIgnoreCase(s))
				readJobs(r);
			else
				jobSpecs = null;

			haveData = true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void readProcTimesAndBatching(BufferedReader r) throws IOException {
		// read processing times and batch families for each operation
		String[][] tmp = Util.read2DimStrings(r, numRoutes);

		procTimes = new double[numRoutes][];
		batchFamilies = new String[numRoutes][];
		batchSizes = new int[numRoutes][];

		for (int i = 0; i < procTimes.length; i++) {
			procTimes[i] = new double[tmp[i].length];
			batchFamilies[i] = new String[tmp[i].length];
			batchSizes[i] = new int[tmp[i].length];

			for (int j = 0; j < procTimes[i].length; j++) {
				String[] dat = tmp[i][j].split(";");
				assert dat.length == 1 || dat.length == 3;

				// do we have a batch family?
				if (dat.length == 3) {
					batchFamilies[i][j] = dat[1];
					batchSizes[i][j] = Integer.parseInt(dat[2]);
				} else {
					batchFamilies[i][j] = "BATCH_INCOMPATIBLE";
					batchSizes[i][j] = 1;
				}

				// parse processing time
				procTimes[i][j] = Double.parseDouble(dat[0]);
			}
		}
	}

	private void readMachOrderAndSetups(BufferedReader r) throws IOException {
		// read machine order and required setup states
		String[][] machTmp = Util.read2DimStrings(r, numRoutes);

		machSpec = new int[numRoutes][][];
		setups = new String[numRoutes][];

		for (int i = 0; i < machSpec.length; i++) {
			machSpec[i] = new int[machTmp[i].length][];
			setups[i] = new String[machSpec[i].length];

			for (int j = 0; j < machSpec[i].length; j++) {
				String[] dat = machTmp[i][j].split(";");
				assert dat.length > 0 && dat.length <= 2;

				// do we have setups?
				if (dat.length == 2) {
					setups[i][j] = dat[1];
				} else
					setups[i][j] = DEF_SETUP;

				// parse alternative machines
				int[] ms = Util.parseIntList(dat[0]);

				// substract 1 to have valid array indices
				for (int k = 0; k < ms.length; k++)
					ms[k]--;

				machSpec[i][j] = ms;
			}
		}
	}

	private void readJobs(BufferedReader r) throws IOException {
		// read job specs, adjust route number to be a valid array index
		String s = Util.nextNonEmptyLine(r);
		if (s == null)
			return;

		int numJobs = Integer.parseInt(s);
		jobSpecs = new JobSpec[numJobs];

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

			jobSpecs[n] = new JobSpec(route, rel, due, w, name);
		}
	}

	private String readMachineParams(BufferedReader r) throws IOException {
		// setup times
		setupMatrices = new HashMap[numMachines];

		name = new String[numMachines];
		numInGroup = new int[numMachines];

		machRelDates = new double[numMachines][];

		String sm = Util.nextNonEmptyLine(r);
		int[] ms = null;
		while (sm != null && !JOB_SECT_MARKER.equalsIgnoreCase(sm)) {
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

			initOperations(shop, r, machSpec[i], procTimes[i], setups[i],
					batchFamilies[i], batchSizes[i]);

			routes[i] = r;
		}
		shop.routes = routes;

		// create job source if jobs specified in file
		if (jobSpecs != null) {
			StaticJobSource s = new StaticJobSource();
			s.jobs = jobSpecs.clone();
			shop.addJobSource(s);
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

	private void initOperations(JobShop shop, Route r, int[][] is,
			double[] procTimes, String[] setups, String[] batchFamilies,
			int[] batchSizes) {

		if (is.length != procTimes.length)
			throw new IllegalArgumentException(
					"time for operation missing or vice versa");
		if (is.length != setups.length)
			throw new IllegalArgumentException(
					"setup state for operation missing or vice versa");

		for (int i = 0; i < procTimes.length; i++) {
			Operation o = new Operation();

			assert is[i].length == 1 : "Alternative machines per operation not allowed.";
			o.machine = shop.machines[is[i][0]];
			o.procTime = procTimes[i];

			// assert setupStateTranslate[o.machine.index].contains(setups[i]);
			o.setupState = o.machine.translateSetupState(setups[i]);

			o.batchFamily = batchFamilies[i];
			o.maxBatchSize = batchSizes[i];

			r.addSequentialOperation(o);
		}
	}

	public JobSpec getJobSpec(int i) {
		return jobSpecs[i];
	}

	public int getNumJobs() {
		return jobSpecs.length;
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
		int[] res = new int[procTimes[route].length];
		for (int i = 0; i < res.length; i++) {
			int[] ms = machSpec[route][i];

			if (ms.length != 1)
				throw new IllegalArgumentException(
						"Can't handle alternative machines.");

			res[i] = ms[0];
		}
		return res;
	}

	public double[] getProcTimes(int route) {
		double[] res = new double[procTimes[route].length];
		for (int i = 0; i < res.length; i++) {
			res[i] = procTimes[route][i];
		}
		return res;
	}
}
