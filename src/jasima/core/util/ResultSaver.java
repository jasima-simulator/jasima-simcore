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
import jasima.core.experiment.FullFactorialExperiment;
import jasima.core.experiment.MultipleReplicationExperiment;

import java.io.BufferedOutputStream;
import java.io.File;
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
 * @version $Id: ResultSaver.java 38 2012-09-11 12:27:38Z
 *          THildebrandt@gmail.com$
 */
public class ResultSaver extends ExperimentListenerBase {

	public static final String SER_EXTENSION = ".jasResBin";

	private static final long serialVersionUID = -6333367038346285154L;

	// save intermediate results after 100 results
	private static final int MAX_UNSAVED = 100;
	// or save every 5 minutes
	private static final long SAVE_INTERVAL = 5 * 60 * 1000;

	protected static class ColumnData implements Serializable {
		private static final long serialVersionUID = 3548750636065811872L;

		public ColumnData(String name, boolean isParamColumn) {
			super();
			this.name = name;
			this.isParamColumn = isParamColumn;
		}

		public final String name;
		public final boolean isParamColumn;
		public int sortedIndex;
	}

	protected static class CellData implements Serializable {
		private static final long serialVersionUID = -1799390860125722470L;

		public CellData(int colIdx, Object value) {
			super();
			this.colIdx = colIdx;
			this.value = value;
		}

		public final int colIdx;
		public final Object value;
	}

	// parameters

	private String resultFileName = null;
	private boolean saveSubExperiments = true;

	// fields used during run

	protected String tmpFileName;
	private ObjectOutputStream tmpDatOut;
	private int unsavedResults;
	private long nextSaveTime;
	private ArrayList<ColumnData> columns;

	@Override
	protected void starting(Experiment e) {
		columns = new ArrayList<ColumnData>();
		unsavedResults = 0;
		nextSaveTime = 0;

		String baseName = getResultFileName();
		if (baseName == null) {
			baseName = "runResults_"
					+ (e.getName() != null ? e.getName() + "_" : "")
					+ String.format(Locale.ENGLISH, "%1$tF_%1$tH%1$tM%1$tS",
							System.currentTimeMillis());
		}

		// don't overwrite existing files, ensure unique final file name
		File f;
		int tried = 0;
		try {
			do {
				tried++;
				tmpFileName = baseName;
				if (tried > 1)
					tmpFileName += "_(" + tried + ")";
				f = new File(tmpFileName + extension());
			} while (!f.createNewFile());
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}

		// write to a file name with fixed ext. SER_EXTENSION (might be
		// different to extension() see ExcelSaver)
		f = new File(tmpFileName + SER_EXTENSION);
		if (f.exists())
			throw new RuntimeException("File '" + f + "' already exists!?");

		// create output stream
		try {
			tmpDatOut = new ObjectOutputStream(new BufferedOutputStream(
					new FileOutputStream(f)));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	protected String extension() {
		return SER_EXTENSION;
	}

	@Override
	protected void multiExperimentCompletedTask(AbstractMultiExperiment me,
			int numTasksExecuted, Experiment e, Map<String, Object> runRes) {
		if (isSaveSubExperiments())
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

	private void saveExperiment(Experiment e, Map<String, Object> runRes) {
		// save parameters
		saveExperiment("", e, true);
		// save results
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

	// getter /setter below

	public String getResultFileName() {
		return resultFileName;
	}

	/**
	 * Sets the result file name. This name is appended with the default suffix
	 * "{@code jasBinRes}". If no file name is set explicitly, this defaults to
	 * something like "{@code runResults_<timeStamp>}".
	 * 
	 * @param resultFileName
	 */
	public void setResultFileName(String resultFileName) {
		this.resultFileName = resultFileName;
	}

	public boolean isSaveSubExperiments() {
		return saveSubExperiments;
	}

	/**
	 * Whether to save parameters and results of sub-experiments.
	 * 
	 * @param saveSubExperiments
	 * @see MultipleReplicationExperiment
	 * @see FullFactorialExperiment
	 */
	public void setSaveSubExperiments(boolean saveSubExperiments) {
		this.saveSubExperiments = saveSubExperiments;
	}

}
