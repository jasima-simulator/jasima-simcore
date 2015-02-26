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
package jasima.core.util;

import jasima.core.statistics.SummaryStat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Some static utility methods that don't really fit anywhere else.
 * 
 * @author Torsten Hildebrandt
 * @version "$Id$"
 */
public class Util {

	/**
	 * The current jasima version.
	 */
	public static final String VERSION = getVersion();

	/**
	 * Default VERSION used when no version information is available from
	 * package (e.g., during a run from within Eclipse).
	 */
	public static final String DEFAULT_VERSION = "1.2.1-DEVELOP";

	/**
	 * Descriptive String showing name, current version and project URL.
	 */
	public static final String ID_STRING = "JASIMA, v" + VERSION
			+ "; http://jasima.googlecode.com/";

	/**
	 * The default locale used, e.g., to format strings.
	 */
	public static Locale DEF_LOCALE = Locale.US;

	/**
	 * Class search path containing all packaged in jasima-main.
	 */
	public static final String[] DEF_CLASS_SEARCH_PATH = {
			"jasima.core.experiment", //
			"jasima.core.expExecution", //
			"jasima.core.random", //
			"jasima.core.random.continuous", //
			"jasima.core.random.discrete", //
			"jasima.core.simulation", //
			"jasima.core.simulation.arrivalprocess", //
			"jasima.core.statistics", //
			"jasima.core.util", //
			"jasima.core.util.observer", //
			"jasima.core.util.run", //
			"jasima.shopSim.core", //
			"jasima.shopSim.core.batchForming", //
			"jasima.shopSim.models.dynamicShop", //
			"jasima.shopSim.models.mimac", //
			"jasima.shopSim.models.staticShop", //
			"jasima.shopSim.prioRules.basic", //
			"jasima.shopSim.prioRules.batch", //
			"jasima.shopSim.prioRules.gp", //
			"jasima.shopSim.prioRules.meta", //
			"jasima.shopSim.prioRules.setup", //
			"jasima.shopSim.prioRules.upDownStream", //
			"jasima.shopSim.prioRules.weighted", //
			"jasima.shopSim.util", //
			"jasima.shopSim.util.modelDef", //
			"jasima.shopSim.util.modelDef.streams", //
	};

	/**
	 * Converts an exception's stack trace to a single line string.
	 */
	public static String exceptionToString(Throwable t) {
		// convert exception to string
		Writer sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		String s = sw.toString();
		return s.replace(System.lineSeparator() + '\t', " \\\\ ").trim();
	}

	/**
	 * Returns a new array with a certain number of new objects of a certain
	 * type.
	 * 
	 * @param numElements
	 *            Number of elements in the result array.
	 * @param componentType
	 *            Class of the array elements.
	 * @return The new array with all elements initialized with new objects.
	 */
	public static <T> T[] initializedArray(int numElements,
			Class<T> componentType) {
		try {
			@SuppressWarnings("unchecked")
			T[] res = (T[]) Array.newInstance(componentType, numElements);
			for (int i = 0; i < numElements; i++) {
				res[i] = componentType.newInstance();
			}
			return res;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Generic method to remove the first occurrence of an element from an
	 * array. A new array without the given element is returned (or the old
	 * array if element was not found).
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] removeFromArray(T[] a, T elementToRemove) {
		ArrayList<T> l = new ArrayList<T>(Arrays.asList(a));
		if (l.remove(elementToRemove)) {
			return l.toArray((T[]) Array.newInstance(a.getClass()
					.getComponentType(), l.size()));
		} else {
			return a;
		}
	}

	/**
	 * Generic method to add an element to an array. A new array containing the
	 * given element is returned.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] addToArray(T[] a, T newElement, Class<T> compType) {
		if (a == null || a.length == 0) {
			T[] res = (T[]) Array.newInstance(compType, 1);
			res[0] = newElement;
			return res;
		} else {
			ArrayList<T> l = new ArrayList<T>(Arrays.asList(a));
			l.add(newElement);
			return l.toArray((T[]) Array.newInstance(compType, l.size()));
		}
	}

	/**
	 * Two double values are considered equal if the absolute value of their
	 * differences is smaller than this constant.
	 */
	public static final double EPSILON = 1e-10;

	/*
	 * @return the next non-empty line (everything after '#' is a comment and
	 * ignored unless it is escaped with a preceding back slash)
	 */
	public static String nextNonEmptyLine(BufferedReader r) throws IOException {
		String s = r.readLine();
		while (s != null) {
			boolean foundHash = false;

			// find part which is not a comment
			int hashPos = s.indexOf('#');
			if (hashPos >= 0) {
				foundHash = true;
				s = s.replace("\\#", "\u02AD"); // escaped?, temporarily replace
												// with something safe
				hashPos = s.indexOf('#');
				if (hashPos >= 0) {
					s = s.substring(0, hashPos);
				}
			}

			s = s.trim();
			if (s.length() > 0) {
				if (foundHash)
					s = s.replace('\u02AD', '#');
				return s;
			}

			s = r.readLine();
		}

		return null;
	}

	public static String[] lines(File f) throws IOException {
		return lines(new FileReader(f));
	}

	public static String[] lines(Reader r) throws IOException {
		BufferedReader br = new BufferedReader(r);
		ArrayList<String> ss = new ArrayList<String>();
		String s;
		while ((s = br.readLine()) != null) {
			ss.add(s);
		}
		return ss.toArray(new String[ss.size()]);
	}

	/**
	 * 
	 * @return An array containing all entries of "ss" not starting with
	 *         "prefix".
	 */
	public static String[] filter(String[] ss, String prefix) {
		ArrayList<String> list = new ArrayList<String>(Arrays.asList(ss));
		for (Iterator<String> i = list.iterator(); i.hasNext();) {
			if (i.next().startsWith(prefix))
				i.remove();
		}
		return list.toArray(new String[list.size()]);
	}

	/**
	 * Examples: "5" -&gt; {5}; "23,5..10,3" -%gt; {23,5,6,7,8,9,10,3}; "1,2,3"
	 * -%gt; {1,2,3}
	 */
	public static int[] parseIntList(String list) {
		ArrayList<Integer> res = new ArrayList<Integer>();
		for (String s : list.split(",")) {
			if (s.contains("..")) {
				String[] sp = s.split("\\.\\.");
				int i1 = Integer.parseInt(sp[0]);
				int i2 = Integer.parseInt(sp[1]);
				for (int i = i1; i <= i2; i++)
					res.add(i);
			} else
				res.add(Integer.parseInt(s));
		}

		// convert Integer[] to int[]
		int[] is = new int[res.size()];
		for (int i = 0; i < res.size(); i++)
			is[i] = res.get(i);
		return is;
	}

	/**
	 * Converts a list of comma-separated double values (with dot as decimal
	 * separator) to a double-array. Example: parseDblList("1.23,4.56") -&gt;
	 * {1.23,4.56}
	 */
	public static double[] parseDblList(String s) {
		ArrayList<Double> ll = new ArrayList<Double>();
		StringTokenizer st = new StringTokenizer(s, ",");
		while (st.hasMoreElements()) {
			double v = Double.parseDouble(st.nextToken().trim());
			ll.add(v);
		}

		double[] res = new double[ll.size()];
		for (int i = 0; i < res.length; i++) {
			res[i] = ll.get(i);
		}
		return res;
	}

	public static String[][] read2DimStrings(BufferedReader r, int numRows)
			throws IOException {
		String[][] ss = new String[numRows][];

		for (int i = 0; i < numRows; i++) {
			ss[i] = nextNonEmptyLine(r).trim().split("\\s+");
		}

		return ss;
	}

	public static double deleteArrayElement(double[] prios, int elemIdx,
			double fillWith) {
		double res = prios[elemIdx];
		System.arraycopy(prios, elemIdx + 1, prios, elemIdx, prios.length
				- elemIdx - 1);
		prios[prios.length - 1] = fillWith;
		return res;
	}

	public static <T> T deleteArrayElement(T[] prios, int elemIdx, T fillWith) {
		T res = prios[elemIdx];
		System.arraycopy(prios, elemIdx + 1, prios, elemIdx, prios.length
				- elemIdx - 1);
		prios[prios.length - 1] = fillWith;
		return res;
	}

	public static <T> T moveArrayElementToBack(T[] prios, int elemIdx) {
		T res = prios[elemIdx];
		System.arraycopy(prios, elemIdx + 1, prios, elemIdx, prios.length
				- elemIdx - 1);
		prios[prios.length - 1] = res;
		return res;
	}

	public static double mean(Collection<? extends Number> coll) {
		if (coll == null || coll.size() < 1)
			throw new IllegalArgumentException();
		double res = 0;
		for (Number n : coll) {
			res += n.doubleValue();
		}
		return res / coll.size();
	}

	public static double stdDev(Collection<? extends Number> coll) {
		if (coll == null || coll.size() < 2)
			throw new IllegalArgumentException();
		double mean = mean(coll);
		double res = 0;
		for (Number n : coll) {
			double d = n.doubleValue() - mean;
			res += d * d;
		}
		return res / (coll.size() - 1);
	}

	public static double sum(final double[] productMix) {
		if (productMix == null || productMix.length == 0)
			throw new IllegalArgumentException(Arrays.toString(productMix));

		double res = productMix[0];
		for (int i = 1; i < productMix.length; i++) {
			res += productMix[i];
		}
		return res;
	}

	public static int sum(final int[] is) {
		if (is == null || is.length == 0)
			throw new IllegalArgumentException(Arrays.toString(is));

		int res = is[0];
		for (int i = 1; i < is.length; i++) {
			res += is[i];
		}
		return res;
	}

	public static int min(int[] vs) {
		if (vs == null || vs.length == 0)
			throw new IllegalArgumentException(Arrays.toString(vs));

		int min = vs[0];
		for (int i = 1; i < vs.length; i++) {
			if (vs[i] < min)
				min = vs[i];
		}
		return min;
	}

	public static int minIdx(int[] vs) {
		return minIdx(vs, 0);
	}

	public static int minIdx(int[] vs, int startIdx) {
		if (vs == null || vs.length == 0)
			throw new IllegalArgumentException(Arrays.toString(vs));

		int min = vs[startIdx];
		int minIdx = startIdx;
		for (int i = startIdx + 1; i < vs.length; i++) {
			if (vs[i] < min) {
				min = vs[i];
				minIdx = i;
			}
		}
		return minIdx;
	}

	public static double min(double[] vs) {
		if (vs == null || vs.length == 0)
			throw new IllegalArgumentException(Arrays.toString(vs));

		double min = vs[0];
		for (int i = 1; i < vs.length; i++) {
			if (vs[i] < min)
				min = vs[i];
		}

		return min;
	}

	public static int minIdx(double[] vs) {
		return minIdx(vs, 0);
	}

	public static int minIdx(double[] vs, int startIdx) {
		if (vs == null || vs.length == 0)
			throw new IllegalArgumentException(Arrays.toString(vs));

		double min = vs[startIdx];
		int minIdx = startIdx;
		for (int i = startIdx + 1; i < vs.length; i++) {
			if (vs[i] < min) {
				min = vs[i];
				minIdx = i;
			}
		}
		return minIdx;
	}

	public static int max(int[] vs) {
		if (vs == null || vs.length == 0)
			throw new IllegalArgumentException(Arrays.toString(vs));

		int max = vs[0];
		for (int i = 1; i < vs.length; i++) {
			if (vs[i] > max)
				max = vs[i];
		}
		return max;
	}

	public static double max(double[] vs) {
		if (vs == null || vs.length == 0)
			throw new IllegalArgumentException(Arrays.toString(vs));

		double max = vs[0];
		for (int i = 1; i < vs.length; i++) {
			if (vs[i] > max)
				max = vs[i];
		}
		return max;
	}

	public static int maxIdx(double[] vs) {
		if (vs == null || vs.length == 0)
			throw new IllegalArgumentException(Arrays.toString(vs));

		double max = vs[0];
		int maxIdx = 0;
		for (int i = 1; i < vs.length; i++) {
			if (vs[i] > max) {
				max = vs[i];
				maxIdx = i;
			}
		}
		return maxIdx;
	}

	/**
	 * Rounds the given double value to a certain number of decimal places.
	 * {@code decimals} can be positive or negative.
	 * 
	 * @see #round(double[], int)
	 */
	public static double round(final double val, final int decimals) {
		if (decimals >= 0) {
			long fact = powerOfTen(decimals);
			return Math.round(val * fact) / ((double) fact);
		} else {
			long fact = powerOfTen(-decimals);
			return Math.round(val / fact) * ((double) fact);
		}
	}

	private static long powerOfTen(int exp) {
		assert exp >= 0;

		long fact = 1;
		for (int i = 0; i < exp; i++) {
			fact *= 10;
		}
		return fact;
	}

	/**
	 * Rounds all values in the double array {@code vs} to a certain number of
	 * decimal places. This method does not create a copy of {@code vs}, but
	 * modifies its contents.
	 * 
	 * @return the parameter {@code vs} to allow easy chaining of method calls.
	 * 
	 * @see #round(double, int)
	 */
	public static double[] round(final double[] vs, final int decimals) {
		for (int i = 0; i < vs.length; i++) {
			vs[i] = round(vs[i], decimals);
		}
		return vs;
	}

	/**
	 * @return True, if the absolute value of their differences of two double
	 *         values is smaller than the constant {@link #EPSILON} (default:
	 *         {@value #EPSILON}).
	 */
	public static boolean equals(final double v1, final double v2) {
		return Math.abs(v1 - v2) < EPSILON;
	}

	/**
	 * Converts an array (either Object[] or of a primitive type) to a String
	 * containing it's elements in square brackets.
	 */
	public static String arrayToString(Object arbitraryArray) {
		Class<?> compType = arbitraryArray.getClass().getComponentType();
		if (compType == null)
			throw new IllegalArgumentException();

		if (compType.isPrimitive()) {
			if (compType == Integer.TYPE)
				return Arrays.toString((int[]) arbitraryArray);
			else if (compType == Long.TYPE)
				return Arrays.toString((long[]) arbitraryArray);
			else if (compType == Short.TYPE)
				return Arrays.toString((short[]) arbitraryArray);
			else if (compType == Byte.TYPE)
				return Arrays.toString((byte[]) arbitraryArray);
			else if (compType == Boolean.TYPE)
				return Arrays.toString((boolean[]) arbitraryArray);
			else if (compType == Double.TYPE)
				return Arrays.toString((double[]) arbitraryArray);
			else if (compType == Float.TYPE)
				return Arrays.toString((float[]) arbitraryArray);
			else if (compType == Character.TYPE)
				return Arrays.toString((char[]) arbitraryArray);
			else
				throw new AssertionError();
		} else
			return Arrays.deepToString((Object[]) arbitraryArray);
	}

	/**
	 * Convenience method to put mean, max and variance of a ValueStat object in
	 * a result map.
	 * 
	 * @param vs
	 *            the statistic
	 * @param prefix
	 *            name prefix
	 * @param res
	 *            result map where keys should be added
	 */
	public static void putMeanMaxVar(SummaryStat vs, String prefix,
			Map<String, Object> res) {
		res.put(prefix + "Mean", vs);
		if (vs.numObs() > 0)
			res.put(prefix + "Max", vs.max());
		if (vs.numObs() >= 2)
			res.put(prefix + "Variance", vs.variance());
	}

	/**
	 * Utility method to get the current package's version.
	 * 
	 * @return The current jasima version as a String.
	 */
	private static String getVersion() {
		String v = null;
		if (Util.class.getPackage() != null)
			v = Util.class.getPackage().getImplementationVersion();
		return v == null ? DEFAULT_VERSION : v;
	}

	/**
	 * Returns a string that characterizes the current execution environment
	 * (Java and OS) by using various system properties.
	 * 
	 * @return The execution environment.
	 */
	public static String getEnvString() {
		String javaVersion = System.getProperty("java.version");
		String javaVendor = System.getProperty("java.vendor");
		String javaVmName = System.getProperty("java.vm.name");
		// String javaRuntimeName = System.getProperty("java.runtime.name");

		String osName = System.getProperty("os.name");
		String osArch = System.getProperty("os.arch");
		String osVersion = System.getProperty("os.version");

		return String.format(DEF_LOCALE, "Java v%s, %s (%s); OS: %s (%s, v%s)",
				javaVersion, javaVmName, javaVendor, osName, osArch, osVersion);
	}

}
