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
package jasima.core.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jasima.core.experiment.AbstractMultiConfExperiment;
import jasima.core.experiment.Experiment;
import jasima.core.experiment.FullFactorialExperiment;
import jasima.core.experiment.MultipleReplicationExperiment;

/**
 * This class uses Java's serialization mechanism to save experiment results in
 * a binary file.
 * <p>
 * 
 * This file can later is later converted to a final output format, so usually a
 * subclass of {@code ResultSaver} is used, e.g., {@link ExcelSaver}.
 * 
 * @author Torsten Hildebrandt
 */
public class ResultSaver extends AbstractResultSaver {

	public static final String SER_EXTENSION = ".jasResBin";

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

	private boolean saveSubExperiments = true;

	// fields used during run

	private ObjectOutputStream tmpDatOut;
	private int unsavedResults;
	private long nextSaveTime;
	private ArrayList<ColumnData> columns;

	@Override
	public void starting(Experiment e) {
		columns = new ArrayList<ColumnData>();
		unsavedResults = 0;
		nextSaveTime = 0;

		String f = getActualResultBaseName() + SER_EXTENSION;

		// create output stream
		try {
			tmpDatOut = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public boolean checkBaseName(String base) {
		if (new File(base + SER_EXTENSION).exists())
			return false;
		return true;
	}

	@Override
	public void multiExperimentCompletedTask(Experiment me, Experiment e, Map<String, Object> runRes) {
		if (isSaveSubExperiments())
			saveExperiment(e, runRes);
	}

	@Override
	public void finished(Experiment e, Map<String, Object> results) {
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

		if (unsavedResults >= MAX_UNSAVED || System.currentTimeMillis() > nextSaveTime) {
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

		// write factor values used
		@SuppressWarnings("unchecked")
		List<String> factors = (List<String>) e.valueStoreGet(AbstractMultiConfExperiment.FACTORS);
		if (factors != null) {
			for (int i = 0; i < factors.size(); i++) {
				String f = factors.get(i);
				int idx = f.indexOf('=');
				assert idx >= 0;

				String name = f.substring(0, idx);
				String value = f.substring(idx + 1);

				int factIdx = getColumnIndex(prefix + AbstractMultiConfExperiment.FACTORS + "." + name, isParam);
				addCell(factIdx, value);
			}
		}

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
		} else if (v.getClass().isArray()) {
			// an array which is not Serializable, i.e., components are not
			// Serializable
			return Util.arrayToString(v);
		} else if (v instanceof Serializable) {
			return v;
		} else
			return v.toString();
	}

	public boolean isSaveSubExperiments() {
		return saveSubExperiments;
	}

	/**
	 * Whether to save parameters and results of sub-experiments.
	 * 
	 * @param saveSubExperiments
	 *            Whether or not to store results of sub-experiments.
	 * 
	 * @see MultipleReplicationExperiment
	 * @see FullFactorialExperiment
	 */
	public void setSaveSubExperiments(boolean saveSubExperiments) {
		this.saveSubExperiments = saveSubExperiments;
	}

}
