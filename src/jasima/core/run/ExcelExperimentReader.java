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
package jasima.core.run;

import jasima.core.experiment.AbstractMultiConfExperiment.ComplexFactorSetter;
import jasima.core.experiment.Experiment;
import jasima.core.experiment.FullFactorialExperiment;
import jasima.core.experiment.MultipleConfigurationExperiment;
import jasima.core.util.TypeUtil;
import jasima.core.util.TypeUtil.TypeConversionException;
import jasima.core.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;

import jxl.BooleanCell;
import jxl.Cell;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

/**
 * Reads and configures an experiment from an Excel-file. This class can also be
 * used to create {@link MultipleConfigurationExperiment}s or
 * {@link FullFactorialExperiment}s from such a file.
 * 
 * @author Robin Kreis
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 */
public class ExcelExperimentReader {

	private static final String COMMENT_PREFIX = "#";
	private static final String SHEET_CONFIGURATIONS = "configurations";
	private static final String SHEET_FACTORS = "factors";
	private static final String SHEET_MAIN = "jasima";

	private static final String SECT_MAIN = "jasima";
	private static final String SECT_CONFIGS = "configurations";
	private static final String SECT_FACTORS = "factors";

	private Sheet jasimaSheet, cfgSheet, factSheet;
	private int numConfigSections;

	private final ClassLoader classLoader;
	private final String[] packageSearchPath;
	private Experiment mainExp;
	private Map<String, List<Object>> factors;

	public class MultValueSetter implements ComplexFactorSetter {

		private static final long serialVersionUID = 5653125435835596532L;

		private final String[] propNames;
		private final Object[] propValues;
		private final String sheetName;
		private final int row;
		private final int col;

		public MultValueSetter(String[] propNames, Object[] propValues,
				String sheetName, int row, int col) {
			super();
			if (propNames.length != propValues.length)
				throw new IllegalArgumentException(
						String.format(
								Util.DEF_LOCALE,
								"Number of property names (%d) and values (%d) do not match.",
								propNames.length, propValues.length));

			this.propNames = propNames;
			this.propValues = propValues;
			this.sheetName = sheetName;
			this.row = row;
			this.col = col;
		}

		@Override
		public void configureExperiment(Experiment e) {
			for (int i = 0; i < propNames.length; i++) {
				String name = propNames[i];
				Object value = propValues[i];

				try {
					TypeUtil.setPropertyValue(e, name, value, classLoader,
							packageSearchPath);
				} catch (RuntimeException t) {
					throw new RuntimeException(String.format(Util.DEF_LOCALE,
							"Problem with value in cell '%s': %s",
							position(sheetName, row, col + i), t.getMessage()),
							t);
				}
			}
		}

	}

	public ExcelExperimentReader(File file, ClassLoader loader,
			String[] packageSearchPath) {
		this.classLoader = loader;
		this.packageSearchPath = packageSearchPath;
		jasimaSheet = cfgSheet = factSheet = null;
		numConfigSections = 0;
		try {
			WorkbookSettings settings = new WorkbookSettings();
			settings.setSuppressWarnings(true);
			Workbook wbk = Workbook.getWorkbook(file, settings);

			for (String s : wbk.getSheetNames()) {
				if (SHEET_MAIN.equalsIgnoreCase(s)) {
					jasimaSheet = wbk.getSheet(s);
				} else if (SHEET_FACTORS.equalsIgnoreCase(s)) {
					factSheet = wbk.getSheet(s);
				} else if (SHEET_CONFIGURATIONS.equalsIgnoreCase(s)) {
					cfgSheet = wbk.getSheet(s);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (BiffException e) {
			throw new RuntimeException(e);
		}

		if (jasimaSheet == null) {
			throw new IllegalArgumentException("Can't find a sheet named '"
					+ SHEET_MAIN + "'");
		}
	}

	public Experiment createExperiment() {
		parseMainSheet();
		jasimaSheet = null;

		// parse configurations sheet (if any)
		if (cfgSheet != null) {
			int row = parseCfgSection(cfgSheet, -1);
			if (row < cfgSheet.getRows())
				throw new RuntimeException(
						"Unknown data on sheet 'configurations' after row "
								+ (row + 1));
			cfgSheet = null;
		}

		// parse factors sheet (if any)
		if (factSheet != null) {
			int row = parseFactSection(factSheet, -1);
			if (row < factSheet.getRows())
				throw new RuntimeException(
						"Unknown data on sheet 'factors' after row "
								+ (row + 1));
			factSheet = null;
		}

		// create experiments
		if (factors != null) {
			FullFactorialExperiment ffe = new FullFactorialExperiment();
			ffe.setBaseExperiment(mainExp);
			for (Entry<String, List<Object>> e : factors.entrySet()) {
				ffe.addFactors(e.getKey(), e.getValue());
			}
			return ffe;
		} else {
			return mainExp;
		}
	}

	private void parseMainSheet() {
		int row = -1;
		// there has to be a main section in the beginning
		row = readMain(row);
		assert mainExp != null;

		while ((row = nextRowNonEmpty(jasimaSheet, row)) < jasimaSheet
				.getRows()) {
			Cell c = jasimaSheet.getCell(0, row);
			String s = String.valueOf(getCellValue(c)).trim()
					.toLowerCase(Util.DEF_LOCALE);
			if (isSectPrefix(s)) {
				if (s.startsWith(SECT_MAIN)) {
					if (mainExp != null) {
						throw new RuntimeException(String.format(
								Util.DEF_LOCALE,
								"There can only be one section '" + SECT_MAIN
										+ "' (cell %s).",
								position(jasimaSheet, c)));
					}
				} else if (s.startsWith(SECT_FACTORS)) {
					row = parseFactSection(jasimaSheet, row);
				} else if (s.startsWith(SECT_CONFIGS)) {
					row = parseCfgSection(jasimaSheet, row);
				} else {
					throw new RuntimeException(String.format(
							"Don't know how to handle section '%s'.", s));
				}

				// set row before start of new section
				if (row < jasimaSheet.getRows())
					row--;
			}
		}

		if (mainExp == null) {
			throw new RuntimeException("Could not find section starting with '"
					+ SECT_MAIN + "'.");
		}
		assert row >= jasimaSheet.getRows();
	}

	private int readMain(int row) {
		row = nextRowNonEmpty(jasimaSheet, row);

		Cell c = jasimaSheet.getCell(0, row);
		String s = String.valueOf(getCellValue(c)).trim();
		if (!"experiment".equalsIgnoreCase(s)) {
			throw new RuntimeException(
					String.format(
							Util.DEF_LOCALE,
							"First value in section '%s' has to be 'experiment' (found '%s' in cell '%s').",
							SECT_MAIN, s, position(jasimaSheet, c)));
		}

		c = jasimaSheet.getCell(1, row);
		Object o = getCellValue(c);
		try {
			mainExp = TypeUtil.convert(o, Experiment.class, "", classLoader,
					packageSearchPath);
		} catch (TypeConversionException t) {
			throw new RuntimeException(String.format(Util.DEF_LOCALE,
					"There is a problem with cell '%s': %s",
					position(jasimaSheet, c), t.getMessage()));
		}

		// are there further properties to be set?
		while ((row = nextRowNonEmpty(jasimaSheet, row)) < jasimaSheet
				.getRows()) {
			// read name
			c = jasimaSheet.getCell(0, row);
			String propName = String.valueOf(getCellValue(c)).trim();

			// end of main section?
			if (isSectPrefix(propName))
				return row;

			// read value
			c = jasimaSheet.getCell(1, row);
			Object propValue = getCellValue(c);

			// try to set property
			try {
				TypeUtil.setPropertyValue(mainExp, propName, propValue,
						classLoader, packageSearchPath);
			} catch (RuntimeException t) {
				throw new RuntimeException(String.format(Util.DEF_LOCALE,
						"There is a problem with cell '%s': %s",
						position(jasimaSheet, c), t.getMessage()));
			}
		}

		// end of sheet
		return row;
	}

	private boolean isSectPrefix(String propName) {
		propName = propName.toLowerCase(Util.DEF_LOCALE);
		return propName.equals(SECT_MAIN) || propName.equals(SECT_CONFIGS)
				|| propName.equals(SECT_FACTORS);
	}

	private int parseFactSection(Sheet sheet, int row) {
		if (mainExp == null) {
			throw new RuntimeException(
					"Can't read factors without an experiment. "
							+ "Define it first in the '" + SECT_MAIN
							+ "'-section on sheet '" + SHEET_MAIN + "'.");
		}

		// find header row
		row = nextRowNonEmpty(sheet, row);
		if (row >= sheet.getRows()) {
			return row;
		}

		if (factors == null)
			factors = new LinkedHashMap<>();

		// read factor names, create value lists
		ArrayList<String> propNames = new ArrayList<>();
		ArrayList<List<Object>> propValues = new ArrayList<>();
		for (int col = 0; col < sheet.getColumns(); col++) {
			Cell c = sheet.getCell(col, row);
			String propName = String.valueOf(getCellValue(c)).trim();
			if (propName.length() == 0)
				break; // for
			ArrayList<Object> valueList = new ArrayList<Object>();
			propValues.add(valueList);

			propNames.add(propName);

			factors.put(propName, valueList);
		}

		// read values until end of sheet or new section
		while ((row = nextRowNonEmpty(sheet, row)) < sheet.getRows()) {
			// check for new section
			Cell c1 = sheet.getCell(0, row);
			Object v = getCellValue(c1);
			if (v instanceof String && isSectPrefix((String) v)) {
				return row;
			}

			// read values
			for (int col = 0; col < propValues.size(); col++) {
				Cell c = sheet.getCell(col, row);
				Object value = getCellValue(c);
				if (value instanceof String) {
					String s = (String) value;
					if (s.length() == 0)
						continue;
				}

				propValues.get(col).add(
						new MultValueSetter(
								new String[] { propNames.get(col) },
								new Object[] { value }, sheet.getName(), row,
								col));
			}
		}

		// reached end of sheet
		return row;
	}

	private int parseCfgSection(Sheet sheet, int row) {
		if (mainExp == null) {
			throw new RuntimeException(
					"Can't read configurations without an experiment. "
							+ "Define it first in the '" + SECT_MAIN
							+ "'-section on sheet '" + SHEET_MAIN + "'.");
		}

		if (factors == null)
			factors = new LinkedHashMap<>();

		// find header row
		row = nextRowNonEmpty(sheet, row);
		if (row >= sheet.getRows()) {
			return row;
		}

		// read property names
		ArrayList<String> propNameList = new ArrayList<>();
		for (int col = 0; col < sheet.getColumns(); col++) {
			Cell c = sheet.getCell(col, row);
			String s = String.valueOf(getCellValue(c)).trim();
			if (s.length() == 0)
				break; // for

			propNameList.add(s);
		}
		String[] propNames = propNameList.toArray(new String[propNameList
				.size()]);

		ArrayList<Object> configurations = new ArrayList<>();
		factors.put("@Conf" + (++numConfigSections), configurations);
		// read values until end of sheet or new section
		while ((row = nextRowNonEmpty(sheet, row)) < sheet.getRows()) {
			// check for new section
			Cell c1 = sheet.getCell(0, row);
			Object v = getCellValue(c1);
			if (v instanceof String && isSectPrefix((String) v)) {
				return row;
			}

			ArrayList<Object> values = new ArrayList<>(propNames.length);
			// read values
			for (int col = 0; col < propNames.length; col++) {
				Cell c = sheet.getCell(col, row);
				Object value = getCellValue(c);

				values.add(value);
			}

			configurations.add(new MultValueSetter(propNames, values.toArray(),
					sheet.getName(), row, 0));
		}

		return row;
	}

	// ********************* static utility methods below *********************

	private static int nextRowNonEmpty(Sheet sheet, int row) {
		while (++row < sheet.getRows()) {
			Cell cell = sheet.getCell(0, row);
			Object v = getCellValue(cell);
			if (v != null) {
				if (v instanceof String) {
					String s = (String) v;
					s = s.trim();
					// not empty or comment?
					if (s.length() > 0 && !s.startsWith(COMMENT_PREFIX))
						return row;
				} else {
					return row;
				}
			}
		}
		return row;
	}

	private static Object getCellValue(Cell c) {
		if (c instanceof BooleanCell) {
			return ((BooleanCell) c).getValue();
		}
		// if (c instanceof DateCell) {
		// return ((DateCell) c).getDate().getTime() / 1000.0; // -> seconds
		// }
		// if (c instanceof LabelCell) {
		// String str = c.getContents().trim();
		// return str;
		// }
		if (c instanceof NumberCell) {
			return ((NumberCell) c).getValue();
		}

		return c.getContents().trim();
	}

	private static String position(Sheet sheet, Cell c) {
		return position(sheet.getName(), c.getRow(), c.getColumn());
	}

	private static String position(String sheetName, int row, int col) {
		String retVal = "";
		do {
			retVal = (char) ('A' + (col % 26)) + retVal;
			col /= 26;
		} while (col > 0);
		retVal += row + 1;

		return sheetName + "!" + retVal;
	}
}
