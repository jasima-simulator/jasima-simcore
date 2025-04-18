/*
This file is part of jasima, the Java simulator for manufacturing and logistics.
 
Copyright 2010-2022 jasima contributors (see license.txt)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package jasima.core.util;

import static jasima.core.util.i18n.I18n.defFormat;

import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import javax.annotation.Nullable;

import jasima.core.simulation.SimComponent;
import jasima.core.statistics.SummaryStat;

/**
 * Some static utility methods that don't really fit anywhere else.
 * 
 * @author Torsten Hildebrandt
 */
public class Util {

	private static final Properties buildProps;

	static {
		buildProps = new Properties();
		try (Reader r = new InputStreamReader(Util.class.getResourceAsStream("/jasima/core/util/version.properties"),
				StandardCharsets.UTF_8)) {
			buildProps.load(r);// TODO: maybe lazy loading on first use?
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * A default thread pool without an upper limit. Used instead of
	 * {@link ForkJoinPool#commonPool()} but without the potential problems the work
	 * stealing algorithm can create.
	 */
	public static final ExecutorService DEF_POOL = Executors.newCachedThreadPool(r -> {
		Thread t = Executors.defaultThreadFactory().newThread(r);
		t.setDaemon(true);
		return t;
	});

	// TODO: make this list expandable using the plugin mechanism
	/**
	 * Class search path containing all packaged in jasima-main.
	 */
	public static final String[] DEF_CLASS_SEARCH_PATH = { //
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
	 * 
	 * @param t The {@link Throwable} to convert to a String.
	 * @return A String representation of {@code t}.
	 */
	public static String exceptionToString(@Nullable Throwable t) {
		if (t == null) {
			return "null";
		}

		// convert exception to string
		Writer sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		String s = sw.toString();
		return s.replace(System.lineSeparator() + '\t', " \\\\ ").trim();
	}

	/**
	 * Returns a new array with a certain number of new objects of a certain type.
	 * 
	 * @param numElements   Number of elements in the result array.
	 * @param componentType Class of the array elements.
	 * @return The new array with all elements initialized with new objects.
	 * @param <T> The component type.
	 */
	public static <T> T[] initializedArray(int numElements, Class<T> componentType) {
		T[] res = newGenericArray(componentType, numElements);
		for (int i = 0; i < numElements; i++) {
			res[i] = create(componentType);
		}
		return res;
	}

	public static <T extends SimComponent> T[] initializedCompArray(int numElements, Class<T> componentType,
			String namePrefix) {
		T[] res = newGenericArray(componentType, numElements);
		for (int i = 0; i < numElements; i++) {
			res[i] = create(componentType);
			res[i].setName(namePrefix + (i + 1));
		}
		return res;
	}

	@SuppressWarnings("unchecked")
	private static <T> T[] newGenericArray(Class<T> componentType, int n) {
		return (T[]) Array.newInstance(componentType, n);
	}

	private static <T> T create(Class<T> componentType) {
		try {
			return componentType.newInstance();
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Generic method to remove the first occurrence of an element from an array. A
	 * new array without the given element is returned (or the old array if element
	 * was not found).
	 * 
	 * @param a               The array to work with.
	 * @param elementToRemove The element to remove from {@code a}.
	 * @return The array {@code a} with the first occurrence of
	 *         {@code elementToRemove} removed. If no such element cound be found,
	 *         {@code a} is returned unchanged.
	 * @param <T> Type of the array components.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] removeFromArray(T[] a, T elementToRemove) {
		ArrayList<T> l = new ArrayList<T>(Arrays.asList(a));
		if (l.remove(elementToRemove)) {
			return l.toArray((T[]) Array.newInstance(a.getClass().getComponentType(), l.size()));
		} else {
			return a;
		}
	}

	/**
	 * Generic method to add an element to an array. A new array additionally
	 * containing the given elements is returned.
	 */
	@SafeVarargs
	public static <T> T[] addToArray(T[] a, Class<T> compType, T... newElement) {
		if (newElement == null || newElement.length == 0)
			throw new IllegalArgumentException();

		int newLength = newElement.length + (a == null ? 0 : a.length);
		ArrayList<T> l = new ArrayList<T>(newLength);
		if (a != null) {
			l.addAll(Arrays.asList(a));
		}
		for (T t : newElement) {
			l.add(t);
		}

		@SuppressWarnings("unchecked")
		T[] resArray = (T[]) Array.newInstance(compType, l.size());

		return l.toArray(resArray);
	}

	/**
	 * @return the next non-empty line (everything after '#' is a comment and
	 *         ignored unless it is escaped with a preceding back slash)
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

	/**
	 * 
	 * @return An array containing all entries of "ss" not starting with "prefix".
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

	public static String[][] read2DimStrings(BufferedReader r, int numRows) throws IOException {
		String[][] ss = new String[numRows][];

		for (int i = 0; i < numRows; i++) {
			ss[i] = nextNonEmptyLine(r).trim().split("\\s+");
		}

		return ss;
	}

	public static double deleteArrayElement(double[] prios, int elemIdx, double fillWith) {
		double res = prios[elemIdx];
		System.arraycopy(prios, elemIdx + 1, prios, elemIdx, prios.length - elemIdx - 1);
		prios[prios.length - 1] = fillWith;
		return res;
	}

	public static <T> T deleteArrayElement(T[] prios, int elemIdx, T fillWith) {
		T res = prios[elemIdx];
		System.arraycopy(prios, elemIdx + 1, prios, elemIdx, prios.length - elemIdx - 1);
		prios[prios.length - 1] = fillWith;
		return res;
	}

	public static <T> T moveArrayElementToBack(T[] prios, int elemIdx) {
		T res = prios[elemIdx];
		System.arraycopy(prios, elemIdx + 1, prios, elemIdx, prios.length - elemIdx - 1);
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
	 * Randomly permute the given double array.
	 * 
	 * @param arr The array to shuffle.
	 * @param rnd The randomness source to use.
	 */
	public static void shuffle(double[] arr, Random rnd) {
		// Shuffle array
		for (int i = arr.length - 1; i > 0; i--) {
			int j = rnd.nextInt(i + 1);

			double tmp = arr[i];
			arr[i] = arr[j];
			arr[j] = tmp;
		}
	}

	/**
	 * Randomly permute the given int array.
	 * 
	 * @param arr The array to shuffle.
	 * @param rnd The randomness source to use.
	 */
	public static void shuffle(int[] arr, Random rnd) {
		// Shuffle array
		for (int i = arr.length - 1; i > 0; i--) {
			int j = rnd.nextInt(i + 1);

			int tmp = arr[i];
			arr[i] = arr[j];
			arr[j] = tmp;
		}
	}

	/**
	 * Rounds the given double value to a certain number of decimal places.
	 * {@code decimals} can be positive or negative.
	 * 
	 * @see #round(double[], int)
	 * @param val      The value to round.
	 * @param decimals The number of decimals to round to.
	 * @return The rounded values.
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
	 * @param vs       An array of doubles to round.
	 * @param decimals The number of decimals to round the values.
	 * @return An array with rounded values.
	 */
	public static double[] round(final double[] vs, final int decimals) {
		for (int i = 0; i < vs.length; i++) {
			vs[i] = round(vs[i], decimals);
		}
		return vs;
	}

	/**
	 * Converts an array (either Object[] or of a primitive type) to a String
	 * containing it's elements in square brackets.
	 * 
	 * @param arbitraryArray The array to convert to a String.
	 * @return A String representation of the array {@code arbitraryArray}.
	 * @throws IllegalArgumentException If {@code arbitraryArray} if not an array.
	 */
	public static String arrayToString(Object arbitraryArray) throws IllegalArgumentException {
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
	 * Convenience method to put mean, max and variance of a ValueStat object in a
	 * result map.
	 * 
	 * @param vs     the statistic
	 * @param prefix name prefix
	 * @param res    result map where keys should be added
	 */
	public static void putMeanMaxVar(SummaryStat vs, String prefix, Map<String, Object> res) {
		res.put(prefix + "Mean", vs);
		if (vs.numObs() > 0)
			res.put(prefix + "Max", vs.max());
		if (vs.numObs() >= 2)
			res.put(prefix + "Variance", vs.variance());
	}

	/**
	 * Utility method to get the current version (obtained from git/Maven during the
	 * build).
	 * 
	 * @return The current jasima version as a String.
	 */
	public static String getVersion() {
		return getBuildProperty("jasima.version");
	}

	/**
	 * Returns a descriptive String showing name, current version and project URL.
	 */
	public static String getIdString() {
		return "JASIMA, v" + getVersion() + "; https://jasima-simulator.github.io/";
	}

	/**
	 * Returns a string that characterizes the current Java environment by using
	 * various system properties.
	 * 
	 * @return The execution environment.
	 */
	public static String getJavaEnvString() {
		String javaVersion = System.getProperty("java.version");
		String javaVendor = System.getProperty("java.vendor");
		String javaVmName = System.getProperty("java.vm.name");
		// String javaRuntimeName = System.getProperty("java.runtime.name");

		return defFormat("java: v%s, %s (%s)", javaVersion, javaVmName, javaVendor);
	}

	/**
	 * Returns a string that characterizes the host operating system by using
	 * various system properties.
	 * 
	 * @return The OS environment.
	 */
	public static String getOsEnvString() {
		String osName = System.getProperty("os.name");
		String osArch = System.getProperty("os.arch");
		String osVersion = System.getProperty("os.version");

		return defFormat("os: %s (%s, v%s)", osName, osArch, osVersion);
	}

	/**
	 * @return the current working directory.
	 */
	public static String getWorkingDirString() {
		String userDir = System.getProperty("user.dir");
		return defFormat("dir: %s", userDir);
	}

	private static synchronized String getBuildProperty(String prop) {
		return buildProps.getProperty(prop, "!!!" + prop + "!!!");
	}

	// prevent instantiation
	private Util() {
	}

}
