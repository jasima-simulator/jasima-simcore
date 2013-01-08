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
package jasima.core.util.run;

import static jasima.core.experiment.AbstractMultiConfExperiment.KEY_EXPERIMENT;
import jasima.core.experiment.AbstractMultiConfExperiment;
import jasima.core.experiment.Experiment;
import jasima.core.experiment.FullFactorialExperiment;
import jasima.core.experiment.MultipleConfigurationExperiment;
import jasima.core.util.Util;
import jasima.core.util.XmlUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import jxl.BooleanCell;
import jxl.Cell;
import jxl.DateCell;
import jxl.ErrorCell;
import jxl.LabelCell;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import com.thoughtworks.xstream.io.StreamException;

/**
 * Reads and configures an experiment from an Excel-file. This class can also be
 * used to create {@link MultipleConfigurationExperiment}s or
 * {@link FullFactorialExperiment}s from such a file.
 * 
 * @author Robin Kreis
 * @author Torsten Hildebrandt, 2013-01-08
 * @version 
 *          "$Id$"
 */
public class ExcelExperimentReader {

	protected Sheet paramSheet, cfgSheet, facSheet;

	public ExcelExperimentReader(File file) {
		try {
			Workbook wbk = Workbook.getWorkbook(file);
			paramSheet = wbk.getSheet("parameters");
			cfgSheet = wbk.getSheet("configurations");
			facSheet = wbk.getSheet("factors");
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (BiffException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Sets a parameter of a MultiConfExperiment to the value of a cell. This is
	 * needed because the desired type can not be inferred from the sheet and is
	 * only known when the parameter is set. <strong>Serializing this class only
	 * preserves the functionality of the {@link #toString()} method.</strong>
	 */
	protected static class CellFactorSetter implements
			AbstractMultiConfExperiment.ComplexFactorSetter {

		private static final long serialVersionUID = 133671480591406206L;
		protected transient final Cell cell;
		protected transient final String propPath;
		protected boolean hideErrors = false;
		protected String stringVal;

		protected CellFactorSetter(Cell cell, String propPath) {
			this.cell = cell;
			this.propPath = propPath;
			stringVal = cell.getContents();
		}

		@Override
		public void configureExperiment(Experiment e) {
			Class<?> type = Util.getPropertyType(e, propPath);
			Util.setProperty(e, propPath, evoke(cell, type));
		}

		@Override
		public String toString() {
			return stringVal;
		}
	}

	protected static Class<?> findClass(String name) {
		// fully qualified class name?
		try {
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
			// ignore
		}

		// has a dot, is no class -> probably file name
		if (name.indexOf('.') != -1) {
			return null;
		}

		// simple class name
		for (String s : new String[] { "jasima.core.experiment" }) {
			try {
				return Class.forName(s + "." + name);
			} catch (ClassNotFoundException e) {
				// ignore
			}
		}

		return null; // give up
	}

	protected static <T> T evoke(Cell cell, Class<T> type) {
		Object val = getCellValue(cell);
		try {
			return Util.convert(val, type);
		} catch (IllegalArgumentException ex) {
			// ignore
		}

		String str = val.toString();

		if (str.equals("null"))
			return null;

		try {
			// first, try loading a class
			try {
				Class<?> klass = findClass(str);
				if (klass != null) {
					return type.cast(klass.newInstance());
				}
			} catch (InstantiationException e) {
				throw new RuntimeException("Can't instantiate '" + str + "' ("
						+ position(cell) + ").");
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Can't access '" + str + "' ("
						+ position(cell) + ").");
			}

			// then, try loading a file
			File f = new File(str);
			try {
				return type.cast(XmlUtil.loadXML(f));
			} catch (StreamException e) {
				throw new RuntimeException("Can't parse '" + position(cell)
						+ "' as " + type.getSimpleName() + ".");
			}
		} catch (ClassCastException e) {
			throw new RuntimeException("Can't convert '" + position(cell)
					+ "' to " + type.getSimpleName() + ".");
		}
	}

	protected static Object getCellValue(Cell c) {
		if (c instanceof BooleanCell) {
			return ((BooleanCell) c).getValue();
		}
		if (c instanceof DateCell) {
			return ((DateCell) c).getDate().getTime() / 1000.0; // -> seconds
		}
		if (c instanceof ErrorCell) {
			return null;
		}
		if (c instanceof LabelCell) {
			String str = c.getContents().trim();
			if (str.isEmpty())
				return null;
			return str;
		}
		if (c instanceof NumberCell) {
			return ((NumberCell) c).getValue();
		}
		return null; // blank or unknown
	}

	protected static String position(Cell c) {
		String retVal = "";
		int col = c.getColumn();
		do {
			retVal = (char) ('A' + (col % 26)) + retVal;
			col /= 26;
		} while (col > 0);
		retVal += c.getRow() + 1;
		return retVal;
	}

	public Experiment createExperiment() {
		return doCreateExperiment(null);
	}

	public <T extends MultipleConfigurationExperiment> T createExperiment(
			T template) {
		doCreateExperiment(template.silentClone());
		return template;
	}

	public <T extends FullFactorialExperiment> T createExperiment(T template) {
		doCreateExperiment(template.silentClone());
		return template;
	}

	/**
	 * Must return template if it is not null.
	 */
	protected Experiment doCreateExperiment(Experiment template) {
		Experiment experiment = template;

		if (template == null) {
			if (cfgSheet != null && facSheet == null) {
				experiment = new MultipleConfigurationExperiment();
			} else if (cfgSheet == null && facSheet != null) {
				experiment = new FullFactorialExperiment();
			} else if (cfgSheet == null && facSheet == null) {
				// there will be an error later
			} else {
				// both factors and configurations exist
				// -> experiment type must be given
			}
		}

		boolean propertiesSet = false;
		if (paramSheet != null) {
			for (int i = 0; i < paramSheet.getRows(); ++i) {
				String key = paramSheet.getCell(0, i).getContents();
				key = key.replace(" ", "");
				if (key.isEmpty())
					continue;
				if (key.equals("experiment")) {
					if (propertiesSet) {
						throw new RuntimeException(
								"The experiment type can not be "
										+ "set after any properties have been set.");
					}
					if (template != null)
						continue;
					experiment = evoke(paramSheet.getCell(1, i),
							Experiment.class);
				} else {
					propertiesSet = true;
					if (experiment == null) {
						throw new RuntimeException(
								"The experiment type must be "
										+ "known before properties are set.");
					}
					Class<?> type;
					try {
						type = Util.getPropertyType(experiment, key);
					} catch (RuntimeException e) {
						throw new RuntimeException("Can't find property '"
								+ experiment.getClass().getSimpleName()
								+ "'. (" + key + ")");
					}
					Object val = evoke(paramSheet.getCell(1, i), type);
					try {
						Util.setProperty(experiment, key, val);
					} catch (RuntimeException e) {
						throw new RuntimeException("Can't write to property '"
								+ experiment.getClass().getSimpleName()
								+ "'. (" + key + ")");
					}
				}
			}
		}

		if (experiment == null) {
			throw new RuntimeException("The experiment type was not set.");
		}

		if (experiment instanceof FullFactorialExperiment) {
			addFactors((FullFactorialExperiment) experiment);
		} else if (experiment instanceof MultipleConfigurationExperiment) {
			addConfigurations((MultipleConfigurationExperiment) experiment);
		} else {
			throw new RuntimeException("Bad experiment type: "
					+ experiment.getClass().getSimpleName());
		}
		return experiment;
	}

	protected void addFactors(FullFactorialExperiment ffe) {
		if (facSheet == null) {
			throw new RuntimeException("Missing sheet 'factors'.");
		}

		for (int col = 0; col < facSheet.getColumns(); ++col) {
			String varName = facSheet.getCell(col, 0).getContents();
			if (varName.isEmpty()) {
				continue;
			}

			if (varName.equals("experiment")) {
				for (int row = 1; row < facSheet.getRows(); ++row) {
					final Cell valCell = facSheet.getCell(col, row);
					if (valCell.getContents().isEmpty())
						continue;
					ffe.addFactor(KEY_EXPERIMENT,
							evoke(valCell, Experiment.class));
				}
				continue;
			}

			for (int row = 1; row < facSheet.getRows(); ++row) {
				final Cell valCell = facSheet.getCell(col, row);
				if (valCell.getContents().isEmpty())
					continue;
				ffe.addFactor(varName, new CellFactorSetter(valCell, varName));
			}
		}

	}

	protected void addConfigurations(MultipleConfigurationExperiment ce) {
		if (cfgSheet == null) {
			throw new RuntimeException("Missing sheet 'configurations'.");
		}
		int row = 0;
		while (++row < cfgSheet.getRows()) {
			HashMap<String, Object> cb = new HashMap<String, Object>();

			for (int col = 0; col < cfgSheet.getColumns(); ++col) {
				String varName = cfgSheet.getCell(col, 0).getContents();
				if (varName.isEmpty()) {
					continue;
				}

				final Cell valCell = cfgSheet.getCell(col, row);
				if (valCell.getContents().isEmpty())
					continue;

				if (varName.equals("experiment")) {
					cb.put(KEY_EXPERIMENT, evoke(valCell, Experiment.class));
					continue;
				}

				cb.put(varName, new CellFactorSetter(valCell, varName));
			}

			ce.addConfiguration(cb);
		}
	}
}
