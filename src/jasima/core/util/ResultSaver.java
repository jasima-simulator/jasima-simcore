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
package jasima.core.util;

import jasima.core.experiment.AbstractMultiExperiment;
import jasima.core.experiment.Experiment;
import jasima.core.experiment.ExperimentListenerBase;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

/**
 * This class uses Java's serialization mechanism to save experiment results in
 * a binary file.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version $Id:: $
 */
public class ResultSaver extends ExperimentListenerBase {

	private static final long serialVersionUID = -6333367038346285154L;

	// save intermediate results after 100 results
	private static final int MAX_UNSAVED = 100;
	// or save a copy every 10 minutes
	private static final long SAVE_INTERVAL = 10 * 60 * 1000;

	protected static class ColumnData implements Serializable {
		private static final long serialVersionUID = 3548750636065811872L;

		public final String name;
		public final boolean isParamColumn;
		public int sortedIndex;

		public ColumnData(String name, boolean isParamColumn) {
			super();
			this.name = name;
			this.isParamColumn = isParamColumn;
		}
	}

	private String resultFileName = null;

	protected String tmpFileName;
	private ObjectOutputStream tmpDatOut;
	private int unsavedResults;
	private long nextSaveTime;
	private ArrayList<ColumnData> columns = new ArrayList<ColumnData>();

	@Override
	protected void starting(Experiment e) {
		columns.clear();
		unsavedResults = 0;
		nextSaveTime = 0;
		String s = getResultFileName();
		if (s == null)
			s = String.format(Locale.ENGLISH,
					"runResults_%1$tF_%1$tH%1$tM%1$tS",
					System.currentTimeMillis());
		tmpFileName = s + ".sav";
		try {
			tmpDatOut = new ObjectOutputStream(new BufferedOutputStream(
					new FileOutputStream(tmpFileName)));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	protected void multiExperimentCompletedTask(AbstractMultiExperiment me,
			int numTasksExecuted, Experiment e, Map<String, Object> runRes) {
		saveExperiment(e, runRes);
	}

	@Override
	protected void finished(Experiment e, Map<String, Object> results) {
		// write marker for begin of main results
		addCell(-3, null);
		columns.clear();
		saveExperiment(e, results);

		try {
			tmpDatOut.close();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	protected void flushTmpFile() {
		try {
			tmpDatOut.flush();
			tmpDatOut.reset();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void addCell(int colIdx, Object value) {
		value = convertValue(value);
		try {
			tmpDatOut.writeObject(new CellData(colIdx, value));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void saveExperiment(Experiment e, Map<String, Object> runRes) {
		saveParams("", e);
		saveData(runRes);

		// write marker for end of record
		addCell(-1, null);

		unsavedResults++;

		if (unsavedResults >= MAX_UNSAVED
				|| System.currentTimeMillis() > nextSaveTime) {
			flushTmpFile();
			unsavedResults = 0;
			nextSaveTime = System.currentTimeMillis() + SAVE_INTERVAL;
		}
	}

	private int getColumnIndex(String s, boolean isParamColumn) {
		int res = -1;

		for (int i = 0; i < columns.size(); i++) {
			ColumnData cd = columns.get(i);
			if (cd.name.equals(s) && cd.isParamColumn == isParamColumn) {
				res = i;
				break;
			}
		}

		// new column?
		if (res < 0) {
			res = columns.size();
			ColumnData cd = new ColumnData(s, isParamColumn);
			addCell(-2, cd);
			columns.add(cd);
		}

		return res;
	}

	private void saveParams(String prefix, Experiment e) {
		saveExperiment(prefix, e, true);
	}

	private void saveData(Map<String, Object> res) {
		for (String name : res.keySet()) {
			Object value = res.get(name);

			if (value != null && value instanceof Experiment) {
				saveExperiment(name + '.', (Experiment) value, false);
			} else {
				int colIdx = getColumnIndex(name, false);
				addCell(colIdx, value);
			}
		}
	}

	private void saveExperiment(String prefix, Experiment e, boolean isParam) {
		// write experiment's class name as a separate column
		String cn = prefix + "className";
		int cnIdx = getColumnIndex(cn, isParam);
		addCell(cnIdx, e.getClass().getName());

		// write all properties
		Map<String, Object> ps = e.getPropsWithValues();
		for (String s : ps.keySet()) {
			Object v = ps.get(s);

			if (v != null && v instanceof Experiment) {
				saveExperiment(prefix + s + '.', (Experiment) v, isParam);
			} else {
				int colIdx = getColumnIndex(prefix + s, isParam);

				addCell(colIdx, v);
			}
		}
	}

	private Object convertValue(Object v) {
		// convert everything not Serializable to a String
		if (v == null) {
			return "null";
		} else if (v instanceof Serializable) {
			return v;
		} else if (v.getClass().isArray()) {
			// an array which is not Serializable, i.e., components are not
			// Serializable
			return Util.arrayToString(v);
		} else
			return v.toString();
	}

	public String getResultFileName() {
		return resultFileName;
	}

	public void setResultFileName(String resultFileName) {
		this.resultFileName = resultFileName;
	}

	protected static class CellData implements Serializable {

		private static final long serialVersionUID = -1799390860125722470L;

		public final int colIdx;
		public final Object value;

		public CellData(int colIdx, Object value) {
			super();
			this.colIdx = colIdx;
			this.value = value;
		}
	}

}
