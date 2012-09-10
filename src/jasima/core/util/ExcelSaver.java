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

import jasima.core.experiment.Experiment;
import jasima.core.statistics.SummaryStat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.NumberFormats;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 * Saves results of an experiment in a handy excel file. The data is stored in a
 * file named like "runResults_2009-08-27_164340.xls". The timestamp in this
 * name is the time the method saveAsExcel() was first called.
 * <p />
 * This class supports more than 256 columns per sheet (Excel-Limit) by
 * splitting data on multiple sheets.
 * <p />
 * Data can be transposed when stored, i.e., rows and columns swapped.
 * 
 * @author Torsten Hildebrandt, 2009-08-27
 * @version $Id$
 */
public class ExcelSaver extends ResultSaver {

	private static final String SHEET_NAME_MAIN = "main experiment";
	private static final String SHEET_NAME_OVERVIEW = "sub-exp. overview";
	private static final String SHEET_NAME_MEAN = "sub-exp. value|mean";
	private static final String SHEET_NAME_MIN = "sub-exp. min";
	private static final String SHEET_NAME_MAX = "sub-exp. max";
	private static final String SHEET_NAME_SD = "sub-exp. stdDev";
	private static final String SHEET_NAME_COUNT = "sub-exp. count";
	private static final String SHEET_NAME_SUM = "sub-exp. sum";

	private static final String[] SUB_RES_SHEETS = { SHEET_NAME_MEAN, SHEET_NAME_MIN, SHEET_NAME_MAX,
		SHEET_NAME_SD, SHEET_NAME_COUNT, SHEET_NAME_SUM };

	private static final long serialVersionUID = 342144249972918192L;

	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("usage: " + ExcelSaver.class.getName()
					+ " <file1ToConvert> [<file2ToConvert> ...]");
			return;
		}

		for (String a : args) {
			File in = new File(a);
			File out = new File(a + ".xls");

			System.out.println("reading '" + in.toString() + "', writing to '"
					+ out.toString() + "'...");

			if (out.exists()) {
				System.out.println("  skipping '" + out
						+ "', file already exists.");
			} else {
				try {
					convertFile(in, out);
					System.out.println("  done.");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void convertFile(File in, File out) throws IOException {
		ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(
				new FileInputStream(in)));
		OutputStream os = null;
		try {
			ExcelSaver es = new ExcelSaver();

			// recover column names by reading file once
			es.readColumns(is);
			is.close();

			// now read a second time and produce Excel file
			is = new ObjectInputStream(new BufferedInputStream(
					new FileInputStream(in)));
			os = new BufferedOutputStream(new FileOutputStream(out));
			es.convertToExcelFile(is, os);
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException ignore) {
				}
			if (os != null)
				try {
					os.close();
				} catch (IOException ignore) {
				}
		}
	}

	public static final int MAX_ROWS = 65536;
	public static final int MAX_COLUMNS = 256;

	private boolean keepDataFile = false;
	private boolean transpose = false;

	private WritableWorkbook workbook;

	private Map<String, Set<Object>> paramValues;

	private ArrayList<ColumnData> columns;
	private ArrayList<ColumnData> mainExpColumns;

	private final WritableCellFormat headerCellFormat;
	private final WritableCellFormat defFormat;
	private final WritableCellFormat intFormat;
	private final WritableCellFormat floatFormat;

	public ExcelSaver() {
		super();

		WritableFont arial10ptBold = new WritableFont(WritableFont.ARIAL, 10,
				WritableFont.BOLD);
		headerCellFormat = new WritableCellFormat(arial10ptBold);

		defFormat = new WritableCellFormat(NumberFormats.DEFAULT);
		intFormat = new WritableCellFormat(NumberFormats.INTEGER);
		floatFormat = new WritableCellFormat(NumberFormats.FLOAT);
	}

	@Override
	protected void finished(Experiment e, Map<String, Object> results) {
		super.finished(e, results);

		// convert data to Excel file
		File tmp = new File(tmpFileName);

		int idx = tmpFileName.lastIndexOf('.');
		String name = idx >= 0 ? tmpFileName.substring(0, idx) : tmpFileName;

		File out = new File(name + ".xls");

		try {
			convertFile(tmp, out);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

		if (!isKeepDataFile())
			tmp.delete();
	}

	protected void convertToExcelFile(ObjectInputStream is, OutputStream os) {
		// create workbook
		try {
			WorkbookSettings ws = new WorkbookSettings();
			// ws.setLocale(new Locale("de", "DE"));
			workbook = Workbook.createWorkbook(os, ws);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// read data a second time and add to workbook
		try {
			writeMainExpHeader();

			if (columns.size() > 0)
				writeSubExpColumnHeaders();

			boolean isSubExp = true;
			try {
				int row = 2;
				while (true) {
					CellData cd = (CellData) is.readObject();
					if (cd.colIdx == -3) {
						isSubExp = false;
					} else {
						if (isSubExp) {
							if (cd.colIdx == -1) {
								row++;
							} else {
								handleSubExpData(row, cd);
							}

						} else
							handleMainExpData(cd);
					}
				}
			} catch (EOFException ignore) {
				// System.out.println("Finished reading objects");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// All sheets and cells added. Now write out the workbook
		try {
			workbook.write();
			workbook.close();
			workbook = null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void writeMainExpHeader() throws Exception {
		ArrayList<ColumnData> sortedColumns = new ArrayList<ColumnData>(
				mainExpColumns);
		Collections.sort(sortedColumns, new Comparator<ColumnData>() {

			@Override
			public int compare(ColumnData cd1, ColumnData cd2) {
				if (cd1.isParamColumn && !cd2.isParamColumn)
					return -1;
				else if (!cd1.isParamColumn && cd2.isParamColumn)
					return +1;
				else {
					return cd1.name.compareToIgnoreCase(cd2.name);
				}
			}
		});

		addHeaderCell(SHEET_NAME_MAIN, 0, 0, "parameters:");
		addHeaderCell(SHEET_NAME_MAIN, 0, 1, "name");
		addHeaderCell(SHEET_NAME_MAIN, 0, 2, "value");

		boolean params = true;
		int n = 1;
		for (int i = 0; i < sortedColumns.size(); i++) {
			ColumnData cd = sortedColumns.get(i);

			// first result column?
			if (!cd.isParamColumn && params) {
				n++;
				addHeaderCell(SHEET_NAME_MAIN, n, 0, "results:");
				addHeaderCell(SHEET_NAME_MAIN, n, 1, "name");
				addHeaderCell(SHEET_NAME_MAIN, n, 2, "value/mean");
				addHeaderCell(SHEET_NAME_MAIN, n, 3, "min");
				addHeaderCell(SHEET_NAME_MAIN, n, 4, "max");
				addHeaderCell(SHEET_NAME_MAIN, n, 5, "stdDev");
				addHeaderCell(SHEET_NAME_MAIN, n, 6, "count");
				addHeaderCell(SHEET_NAME_MAIN, n, 7, "sum");
				n++;
				params = false;
			}
			cd.sortedIndex = n++;

			// write parameter/result name
			addCell(SHEET_NAME_MAIN, cd.sortedIndex, 1, cd.name);
		}
	}

	private void handleMainExpData(CellData cd) throws Exception {
		if (cd.colIdx >= 0) {
			// parameter/result values
			ColumnData col = mainExpColumns.get(cd.colIdx);
			if (col.sortedIndex >= 0) {
				if (cd.value instanceof SummaryStat) {
					SummaryStat s = (SummaryStat) cd.value;
					addCell(SHEET_NAME_MAIN, col.sortedIndex, 2, s.mean());
					addCell(SHEET_NAME_MAIN, col.sortedIndex, 3, s.min());
					addCell(SHEET_NAME_MAIN, col.sortedIndex, 4, s.max());
					addCell(SHEET_NAME_MAIN, col.sortedIndex, 5, s.stdDev());
					addCell(SHEET_NAME_MAIN, col.sortedIndex, 6, s.numObs());
					addCell(SHEET_NAME_MAIN, col.sortedIndex, 7, s.sum());
				} else {
					addCell(SHEET_NAME_MAIN, col.sortedIndex, 2, cd.value);
				}
			}
		} else if (cd.colIdx == -2) {
			// column header, ignore
		} else {
			// end of row marker
			assert cd.colIdx == -1;
		}
	}

	/**
	 * Reads column names and values of parameter columns.
	 * 
	 * @param is
	 *            The input file which is read till the end.
	 */
	protected void readColumns(ObjectInputStream is) {
		paramValues = new HashMap<String, Set<Object>>();
		columns = new ArrayList<ColumnData>();
		mainExpColumns = new ArrayList<ColumnData>();

		boolean isSubExp = true;
		try {
			try {
				while (true) {
					CellData cd = (CellData) is.readObject();
					if (cd.colIdx == -1) {// ignore end of record marker
					} else if (cd.colIdx == -3) { // ignore marker for begin of
													// main results
						isSubExp = false;
					} else if (cd.colIdx == -2) { // new column
						// column header
						ColumnData col = (ColumnData) cd.value;
						if (isSubExp)
							columns.add(col);
						else
							mainExpColumns.add(col);
					} else {
						if (isSubExp) {
							ColumnData col = columns.get(cd.colIdx);
							if (col.isParamColumn) {
								Set<Object> values = (Set<Object>) paramValues
										.get(col.name);
								if (values == null) {
									values = new HashSet<Object>();
									paramValues.put(col.name, values);
								}
								values.add(cd.value);
							} else {
								// ignore values
							}
						}
					}
				}
			} catch (EOFException ignore) {
				// finished reading objects
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void writeSubExpColumnHeaders() throws Exception {
		ArrayList<ColumnData> sortedColumns = new ArrayList<ColumnData>(columns);
		Collections.sort(sortedColumns, new Comparator<ColumnData>() {

			@Override
			public int compare(ColumnData cd1, ColumnData cd2) {
				if (cd1.isParamColumn && !cd2.isParamColumn)
					return -1;
				else if (!cd1.isParamColumn && cd2.isParamColumn)
					return +1;
				else {
					return cd1.name.compareToIgnoreCase(cd2.name);
				}
			}
		});

		addHeaderCell(SHEET_NAME_OVERVIEW, 0, 0,
				"parameters used (constant parameters are not shown on other sheets)");

		addHeaderCell(SHEET_NAME_OVERVIEW, 2, 0, "name");
		addHeaderCell(SHEET_NAME_OVERVIEW, 2, 1, "distinct values");
		addHeaderCell(SHEET_NAME_OVERVIEW, 2, 2, "value 1");
		addHeaderCell(SHEET_NAME_OVERVIEW, 2, 3, "value 2");
		addHeaderCell(SHEET_NAME_OVERVIEW, 2, 4, "...");

		int n = 0;
		for (int i = 0; i < sortedColumns.size(); i++) {
			ColumnData cd = sortedColumns.get(i);
			Set<Object> values = paramValues.get(cd.name);

			// is parameter not varied in experiments?
			if (values != null && values.size() == 1) {
				cd.sortedIndex = -1;
			} else {
				cd.sortedIndex = n++;
			}

			if (cd.isParamColumn) {
				addCell(SHEET_NAME_OVERVIEW, i + 3, 0, cd.name);
				addCell(SHEET_NAME_OVERVIEW, i + 3, 1, values.size());
				int j = 2;
				for (Object v : values) {
					addCell(SHEET_NAME_OVERVIEW, i + 3, j++, v);
				}
			}
		}

		boolean params = true;
		addCellEachSheet(0, 0, "parameters (only shown on sheet '"
				+ SHEET_NAME_MEAN + "')");
		for (ColumnData cd : sortedColumns) {
			if (cd.sortedIndex == -1)
				continue;

			if (!cd.isParamColumn && params) {
				addCellEachSheet(0, cd.sortedIndex, "results start here");
				params = false;
			}

			if (!cd.isParamColumn)
				addCellEachSheet(1, cd.sortedIndex, cd.name);
			else
				addHeaderCell(SHEET_NAME_MEAN, 1, cd.sortedIndex, cd.name);
		}
	}

	private void handleSubExpData(int row, CellData cd) throws Exception {
		if (cd.colIdx >= 0) {
			ColumnData col = columns.get(cd.colIdx);
			if (col.sortedIndex >= 0) {
				if (cd.value instanceof SummaryStat) {
					SummaryStat s = (SummaryStat) cd.value;
					addCell(SHEET_NAME_MEAN, row, col.sortedIndex, s.mean());
					addCell(SHEET_NAME_MIN, row, col.sortedIndex, s.min());
					addCell(SHEET_NAME_MAX, row, col.sortedIndex, s.max());
					addCell(SHEET_NAME_SD, row, col.sortedIndex, s.stdDev());
					addCell(SHEET_NAME_COUNT, row, col.sortedIndex, s.numObs());
					addCell(SHEET_NAME_SUM, row, col.sortedIndex, s.sum());
				} else
					addCell(SHEET_NAME_MEAN, row, col.sortedIndex, cd.value);
			}
		} else {
			// column header, ignore
			assert cd.colIdx == -2;
		}
	}

	private void addHeaderCell(String sheetName, int row, int column,
			String string) throws Exception {
		addCell0(sheetName, row, column, string, headerCellFormat);
	}

	private void addCell(String sheetName, int row, int column, Object o)
			throws Exception {
		addCell0(sheetName, row, column, o, defFormat);
	}

	private void addCellEachSheet(int row0, int col0, String value)
			throws Exception {
		for (String sheet : SUB_RES_SHEETS) {
			addHeaderCell(sheet, row0, col0, value);
		}
	}

	private void addCell0(String sheetBaseName, int row0, int col0,
			Object value, WritableCellFormat format) throws Exception {
		int row = row0;
		int col = col0;

		if (isTranspose()) {
			// swap row and column
			int tmp = row;
			row = col;
			col = tmp;
		}

		int sheetNum = col / MAX_COLUMNS;
		col = col % MAX_COLUMNS;
		if (sheetNum > 0)
			sheetBaseName += " - c" + sheetNum;

		int sheetNum2 = row / MAX_ROWS;
		row = row % MAX_ROWS;
		if (sheetNum2 > 0)
			sheetBaseName += " - r" + sheetNum2;

		WritableSheet valSheet = getOrCreateSheet(sheetBaseName);

		if (value == null) {
			valSheet.addCell(new Label(col, row, "null", format));
		} else if (value instanceof java.lang.Number) {
			valSheet.addCell(createCell4Number(col, row,
					(java.lang.Number) value));
		} else if (value.getClass().isArray()) {
			valSheet.addCell(new Label(col, row, Util.arrayToString(value),
					format));
		} else {
			valSheet.addCell(new Label(col, row, String.valueOf(value), format));
		}
	}

	/**
	 * Excel does not know NaN and INF, so use appropriate Strings instead.
	 * 
	 * http://support.microsoft.com/kb/78113/EN-US/, 2009-01-19
	 */
	private WritableCell createCell4Number(int col, int row, java.lang.Number n) {
		double v = n.doubleValue();

		if (Double.isNaN(v))
			return new Label(col, row, "NaN");
		if (Double.POSITIVE_INFINITY == v)
			return new Label(col, row, "+INF");
		if (Double.NEGATIVE_INFINITY == v)
			return new Label(col, row, "-INF");

		// return long as text to avoid rounding problems
		if (n.getClass() == Long.class)
			return new Label(col, row, n.toString());
		
		// determine cell format
		WritableCellFormat f = defFormat;
		if (n.getClass() == Double.class || n.getClass() == Float.class) {
			f = floatFormat;
		} else if (n.getClass() == Integer.class || n.getClass() == Long.class
				|| n.getClass() == Short.class || n.getClass() == Byte.class) {
			f = intFormat;
		}

		// create number cell
		return new jxl.write.Number(col, row, v, f);
	}

	private WritableSheet getOrCreateSheet(String sheetBaseName) {
		WritableSheet s = workbook.getSheet(sheetBaseName);

		if (s == null) {
			s = workbook.createSheet(sheetBaseName, Integer.MAX_VALUE);
		}

		return s;
	}

	// getter / setter for parameters below

	public void setTranspose(boolean transpose) {
		this.transpose = transpose;
	}

	public boolean isTranspose() {
		return transpose;
	}

	public boolean isKeepDataFile() {
		return keepDataFile;
	}

	public void setKeepDataFile(boolean keepDataFile) {
		this.keepDataFile = keepDataFile;
	}

}
