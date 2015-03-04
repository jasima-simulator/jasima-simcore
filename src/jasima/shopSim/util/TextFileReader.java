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

import jasima.core.util.Pair;
import jasima.core.util.Util;
import jasima.shopSim.core.JobShop;
import jasima.shopSim.core.WorkStation;
import jasima.shopSim.models.staticShop.StaticShopExperiment;
import jasima.shopSim.util.modelDef.DynamicSourceDef;
import jasima.shopSim.util.modelDef.IndividualMachineDef;
import jasima.shopSim.util.modelDef.JobDef;
import jasima.shopSim.util.modelDef.OperationDef;
import jasima.shopSim.util.modelDef.RouteDef;
import jasima.shopSim.util.modelDef.ShopDef;
import jasima.shopSim.util.modelDef.SourceDef;
import jasima.shopSim.util.modelDef.StaticSourceDef;
import jasima.shopSim.util.modelDef.WorkstationDef;
import jasima.shopSim.util.modelDef.streams.DblStreamDef;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This class creates a {@link ShopDef} based on the contents of a text file.
 * This can be used to configure a {@link JobShop}, e.g., as part of a
 * {@link StaticShopExperiment}. For examples of the file structure see the
 * examples in the directory "testInstances" or the model files in the package
 * jasima.shopSim.models.mimac.
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 */
public class TextFileReader {

	private static final String JOB_SECT_MARKER = "jobs";
	private static final String JOB_SECT_DYN_MARKER = "jobs_dynamic";
	private static final String NAME_MARKER = "machineName";
	private static final String SETUP_MATRIX_MARKER = "setup";
	private static final String NUM_IN_GROUP_MARKER = "numInGroup";
	private static final String MACHINE_RELEASE_MARKER = "machineReleaseDates";

	private static final String DEF_SETUP = "DEF_SETUP";

	private ShopDef data;
	private HashMap<String, Integer> machIds;
	private boolean haveData;

	public TextFileReader() {
		super();

		haveData = false;
		data = null;
		machIds = new HashMap<String, Integer>();
	}

	public ShopDef getShopDef() {
		return haveData ? data : null;
	}

	public ShopDef readData(BufferedReader r) {
		data = new ShopDef();
		try {
			int numMachines = Integer.parseInt(Util.nextNonEmptyLine(r));
			WorkstationDef[] ws = new WorkstationDef[numMachines];
			for (int i = 0; i < numMachines; i++) {
				WorkstationDef wd = new WorkstationDef();
				ws[i] = wd;
			}
			data.setWorkstations(ws);

			int numRoutes = Integer.parseInt(Util.nextNonEmptyLine(r));
			RouteDef[] rs = new RouteDef[numRoutes];
			for (int i = 0; i < numRoutes; i++) {
				RouteDef rd = new RouteDef();
				rs[i] = rd;
			}
			data.setRoutes(rs);

			readMachOrderAndSetups(r);
			readProcTimesAndBatching(r);

			// read setups, optional
			String s = readMachineParams(r);

			// read jobs, optional
			while (s != null) {
				s = readJobs(r, s);
			}

			haveData = true;
			return data;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected String readJobs(BufferedReader r, String s) throws IOException {
		if (JOB_SECT_MARKER.equalsIgnoreCase(s))
			s = readJobs(r);
		else if (JOB_SECT_DYN_MARKER.equalsIgnoreCase(s))
			s = readDynJobs(r);
		else
			throw new IllegalArgumentException(s);
		return s;
	}

	private void readMachOrderAndSetups(BufferedReader r) throws IOException {
		RouteDef[] routes = data.getRoutes();
		final int numRoutes = routes.length;

		// read machine order and required setup states
		String[][] machTmp = Util.read2DimStrings(r, numRoutes);

		for (int i = 0; i < numRoutes; i++) {
			RouteDef rd = routes[i];
			int numOps = machTmp[i].length;

			OperationDef[] ops = new OperationDef[numOps];
			rd.setOperations(ops);

			for (int j = 0; j < numOps; j++) {
				OperationDef od = new OperationDef();
				ops[j] = od;

				String[] dat = machTmp[i][j].split(";");
				assert dat.length > 0 && dat.length <= 2;

				od.setSetup(WorkStation.DEF_SETUP_STR);
				// do we have setups?
				if (dat.length == 2) {
					od.setSetup(dat[1]);
				}

				String machId = dat[0];
				int idx = getMachIdx(machId);
				od.setWorkstation(data.getWorkstations()[idx]);
			}
		}
	}

	private void readProcTimesAndBatching(BufferedReader r) throws IOException {
		RouteDef[] routes = data.getRoutes();
		final int numRoutes = routes.length;

		// read processing times and batch families for each operation
		String[][] tmp = Util.read2DimStrings(r, numRoutes);

		for (int i = 0; i < numRoutes; i++) {
			RouteDef rd = routes[i];
			int numOps = tmp[i].length;

			OperationDef[] ops = rd.getOperations();

			for (int j = 0; j < numOps; j++) {
				OperationDef od = ops[j];

				String[] dat = tmp[i][j].split(";");
				assert dat.length == 1 || dat.length == 3;

				// parse processing time
				od.setProcTime(Double.parseDouble(dat[0]));

				od.setBatchFamily(WorkStation.BATCH_INCOMPATIBLE);
				od.setMaxBatchSize(1);

				// do we have a batch family?
				if (dat.length == 3) {
					od.setBatchFamily(dat[1]);
					od.setMaxBatchSize(Integer.parseInt(dat[2]));
				}
			}
		}
	}

	private int getMachIdx(String machId) {
		Integer idx = machIds.get(machId);
		if (idx == null) {
			try {
				idx = Integer.parseInt(machId) - 1;
			} catch (NumberFormatException ignore) {
				idx = machIds.size();
			}
			assert !machIds.containsValue(idx);
			machIds.put(machId, idx);
		}
		return idx.intValue();
	}

	private String readJobs(BufferedReader r) throws IOException {
		// read job specs, adjust route number to be a valid array index
		String s = Util.nextNonEmptyLine(r);
		if (s == null)
			return s;

		int numRoutes = data.getRoutes().length;

		int numJobs = Integer.parseInt(s);
		JobDef[] jobs = new JobDef[numJobs];

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

			RouteDef rd = data.getRoutes()[route];

			jobs[n] = new JobDef(rd, rel, due, w, name);
		}

		StaticSourceDef sd = new StaticSourceDef();
		sd.setJobSpecs(jobs);
		SourceDef[] as = Util.addToArray(data.getJobSources(), SourceDef.class,
				sd);
		data.setJobSources(as);

		return Util.nextNonEmptyLine(r);
	}

	private String readDynJobs(BufferedReader r) throws IOException {
		// read job specs, adjust route number to be a valid array index
		String s = Util.nextNonEmptyLine(r);
		if (s == null)
			return s;

		RouteDef route = null;
		DblStreamDef iats = null;
		DblStreamDef dueDates = null;
		DblStreamDef weights = null;
		// int numJobs = -1;

		ArrayList<String> errors = new ArrayList<String>();

		if (!"route".equalsIgnoreCase(s))
			throw new RuntimeException("parse error '" + s + "'");
		s = Util.nextNonEmptyLine(r);
		int rt = Integer.parseInt(s);
		if (rt < 1 || rt > data.getRoutes().length)
			throw new RuntimeException("Invalid route number " + rt);
		rt--;// adjust to zero-based route index
		route = data.getRoutes()[rt];

		s = Util.nextNonEmptyLine(r);
		if (!"arrivals".equalsIgnoreCase(s))
			throw new RuntimeException("parse error '" + s + "'");
		s = Util.nextNonEmptyLine(r);
		iats = DblStreamDef.parseDblStream(s, errors);
		if (errors.size() > 0)
			throw new RuntimeException("parse error '" + s + "', "
					+ Arrays.toString(errors.toArray()));

		s = Util.nextNonEmptyLine(r);
		if (!"due_dates".equalsIgnoreCase(s))
			throw new RuntimeException("parse error '" + s + "'");
		s = Util.nextNonEmptyLine(r);
		dueDates = DblStreamDef.parseDblStream(s, errors);
		if (errors.size() > 0)
			throw new RuntimeException("parse error '" + s + "', "
					+ Arrays.toString(errors.toArray()));

		s = Util.nextNonEmptyLine(r);
		if (!"weights".equalsIgnoreCase(s))
			throw new RuntimeException("parse error '" + s + "'");
		s = Util.nextNonEmptyLine(r);
		weights = DblStreamDef.parseDblStream(s, errors);
		if (errors.size() > 0)
			throw new RuntimeException("parse error '" + s + "', "
					+ Arrays.toString(errors.toArray()));

		s = Util.nextNonEmptyLine(r);

		int numJobs = -1;

		// optional
		if ("numJobs".equalsIgnoreCase(s)) {
			s = Util.nextNonEmptyLine(r);
			numJobs = Integer.parseInt(s);
			s = Util.nextNonEmptyLine(r);
		}

		DynamicSourceDef sd = new DynamicSourceDef();
		sd.setIats(iats);
		sd.setDueDates(dueDates);
		sd.setWeights(weights);
		sd.setRoute(route);
		if (numJobs >= 0)
			sd.setNumJobs(numJobs);

		SourceDef[] as = Util.addToArray(data.getJobSources(), SourceDef.class,
				sd);
		data.setJobSources(as);

		return s;
	}

	private String readMachineParams(BufferedReader r) throws IOException {
		// setup times
		String sm = Util.nextNonEmptyLine(r);
		WorkstationDef ms = null;
		while (sm != null && !JOB_SECT_MARKER.equalsIgnoreCase(sm)
				&& !JOB_SECT_DYN_MARKER.equalsIgnoreCase(sm)) {
			if (SETUP_MATRIX_MARKER.equals(sm)) {
				Map<Pair<String, String>, Double> matrix = readSetupMatrix(r);

				ArrayList<String> states = new ArrayList<String>();
				states.add(DEF_SETUP);
				for (Pair<String, String> p : matrix.keySet()) {
					if (!states.contains(p.a))
						states.add(p.a);
					if (!states.contains(p.b))
						states.add(p.b);
				}

				double[][] times = new double[states.size()][states.size()];
				for (int i = 0; i < states.size(); i++) {
					String s1 = states.get(i);
					for (int j = 0; j < states.size(); j++) {
						String s2 = states.get(j);
						Double d = matrix.get(new Pair<String, String>(s1, s2));
						times[i][j] = d == null ? 0.0 : d.doubleValue();
					}
				}

				ms.setSetupStates(states.toArray(new String[states.size()]));
				ms.setSetupTimes(times);
			} else if (NAME_MARKER.equals(sm)) {
				String name = Util.nextNonEmptyLine(r);
				ms.setName(name);
			} else if (NUM_IN_GROUP_MARKER.equals(sm)) {
				int inGroup = Integer.parseInt(Util.nextNonEmptyLine(r));
				IndividualMachineDef[] im = Util.initializedArray(inGroup,
						IndividualMachineDef.class);
				ms.setMachines(im);
			} else if (MACHINE_RELEASE_MARKER.equals(sm)) {
				String[] ss = Util.nextNonEmptyLine(r).trim().split("\\s+");
				IndividualMachineDef[] im = ms.getMachines();

				for (int i = 0; i < ss.length; i++) {
					im[i].setMachRelDate(Double.parseDouble(ss[i]));
				}
			} else {
				int m = getMachIdx(sm.trim());
				ms = data.getWorkstations()[m];
			}

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

}
