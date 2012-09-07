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
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableCell;
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

	private static final String[] sheetNames = { "summary", "min", "max",
			"stdDev", "count", "sum" };

	public static final int MAX_ROWS = 65536;
	public static final int MAX_COLUMNS = 256;

	private boolean keepDataFile = false;
	private boolean transpose = false;

	private WritableWorkbook workbook;

	private Map<String, SortedSet<String>> paramValues;

	public ExcelSaver() {
		super();
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

		// read data and add to workbook
		try {
			writeColumnHeaders();

			try {
				int row = 2;
				while (true) {
					CellData cd = (CellData) is.readObject();
					if (cd.colIdx >= 0) {
						ColumnData col = columns.get(cd.colIdx);
						if (col.sortedIndex >= 0) {
							// if (col.isParamColumn)
							// addCellEachSheet(row, col.sortedIndex, cd.value);
							// else
							addCell(sheetNames[0], row, col.sortedIndex,
									cd.value);
						}
					} else if (cd.colIdx == -2) {
						// column header, ignore
					} else if (cd.colIdx == -3) {
						// begin of main results, ignore
					} else {
						// end of row marker
						assert cd.colIdx == -1;
						row++;
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

	/**
	 * Reads column names and values of parameter columns.
	 * 
	 * @param is
	 *            The input file which is read till the end .
	 */
	protected void readColumns(ObjectInputStream is) {
		paramValues = new HashMap<String, SortedSet<String>>();
		columns.clear();

		try {
			try {
				while (true) {
					CellData cd = (CellData) is.readObject();
					if (cd.colIdx == -1) {// ignore end of record marker
					} else if (cd.colIdx == -3) { // ignore marker for begin of
													// main results
						break; // while
					} else if (cd.colIdx == -2) { // new column
						ColumnData col = (ColumnData) cd.value;
						// column header
						columns.add(col);
					} else {
						ColumnData col = columns.get(cd.colIdx);
						if (col.isParamColumn) {
							SortedSet<String> values = (SortedSet<String>) paramValues
									.get(col.name);
							if (values == null) {
								values = new TreeSet<String>();
								paramValues.put(col.name, values);
							}

							String value = String.valueOf(cd.value);
							values.add(value);
						} else {
							// ignore values
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

	private void writeColumnHeaders() throws Exception {
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

		addCell("overview", 0, 0,
				"parameters used (constant parameters are not shown on other sheets)");

		addCell("overview", 2, 0, "name");
		addCell("overview", 2, 1, "distinct values");
		addCell("overview", 2, 2, "value 1");
		addCell("overview", 2, 3, "value 2");
		addCell("overview", 2, 4, "...");

		int n = 0;
		for (int i = 0; i < sortedColumns.size(); i++) {
			ColumnData cd = sortedColumns.get(i);
			SortedSet<String> values = paramValues.get(cd.name);

			// is parameter not varied in experiments?
			if (values != null && values.size() == 1) {
				cd.sortedIndex = -1;
			} else {
				cd.sortedIndex = n++;
			}

			if (cd.isParamColumn) {
				addCell("overview", i + 3, 0, cd.name);
				addCell("overview", i + 3, 1, values.size());
				int j = 2;
				for (String v : values) {
					addCell("overview", i + 3, j++, v);
				}
			}
		}

		boolean params = true;
		addCellEachSheet(0, 0, "parameters (only shown on sheet '"
				+ sheetNames[0] + "')");
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
				addCell(sheetNames[0], 1, cd.sortedIndex, cd.name);
		}
	}

	private void addCellEachSheet(int row0, int col0, Object value)
			throws Exception {
		// write to each sheet
		for (String sheet : sheetNames) {
			addCell(sheet, row0, col0, value);
		}
	}

	protected void addCell(String sheetBaseName, int row0, int col0,
			Object value) throws Exception {
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
			valSheet.addCell(new Label(col, row, "null"));
		} else if (value instanceof java.lang.Number) {
			valSheet.addCell(createCell4Number(col, row,
					(java.lang.Number) value));
		} else if (value instanceof SummaryStat) {
			SummaryStat n = (SummaryStat) value;
			valSheet.addCell(createCell4Number(col, row, n.mean()));
			addCell("min", row0, col0, n.min());
			addCell("max", row0, col0, n.max());
			addCell("stdDev", row0, col0, n.stdDev());
			addCell("count", row0, col0, n.numObs());
			addCell("sum", row0, col0, n.sum());
		} else if (value.getClass().isArray()) {
			valSheet.addCell(new Label(col, row, Util.arrayToString(value)));
		} else {
			valSheet.addCell(new Label(col, row, String.valueOf(value)));
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

		return new jxl.write.Number(col, row, v);
	}

	private WritableSheet getOrCreateSheet(String sheetBaseName) {
		WritableSheet s = workbook.getSheet(sheetBaseName);

		if (s == null) {
			s = workbook.createSheet(sheetBaseName, Integer.MAX_VALUE);
		}

		return s;
	}

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
