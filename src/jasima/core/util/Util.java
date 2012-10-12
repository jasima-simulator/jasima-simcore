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

import jasima.core.statistics.SummaryStat;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Some static utility methods that don't really fit anywhere else.
 * 
 * @author Torsten Hildebrandt
 * @version "$Id$"
 */
public class Util {

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
	 * Attempts trivial type conversion. This methods supports all cating
	 * conversions (JLS 5.5) and always returns null when the input object is
	 * null. If the target type is {@link String}, the result is the return
	 * value of the input object's {@link Object#toString()} method. Any object
	 * can be converted to {@link Integer}, {@link Double} and {@link Boolean},
	 * but those conversions can throw exceptions.
	 * 
	 * @param o
	 *            the object to be converted
	 * @param klass
	 *            the target type
	 * @return the converted object
	 * @throws IllegalArgumentException
	 *             if the conversion is not supported
	 * @throws NumberFormatException
	 *             if the input object is not assignable to {@link Number} and
	 *             the return value of its {@link Object#toString()} method
	 *             can't be converted to the numeric target type
	 */
	@SuppressWarnings("unchecked")
	public static <E> E convert(Object o, Class<E> klass)
			throws IllegalArgumentException {

		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6456930
		// klass.cast will not unbox and can't accept primitive values

		if (o == null)
			return null;

		if (klass.isAssignableFrom(o.getClass())) {
			return (E) o;
		}

		if (klass == String.class) {
			return (E) o.toString();
		}

		if (klass == int.class || klass == Integer.class) {
			if (o instanceof Number)
				return (E) (Integer) ((Number) o).intValue();
			return (E) Integer.valueOf(o.toString());
		}

		if (klass == double.class || klass == Double.class) {
			if (o instanceof Number)
				return (E) (Double) ((Number) o).doubleValue();
			return (E) Double.valueOf(o.toString());
		}

		if (klass == boolean.class || klass == Boolean.class) {
			String str = o.toString();
			if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("yes")
					|| str.equalsIgnoreCase("1"))
				return (E) Boolean.TRUE;
			if (str.equalsIgnoreCase("false") || str.equalsIgnoreCase("no")
					|| str.equalsIgnoreCase("0"))
				return (E) Boolean.FALSE;
			throw new IllegalArgumentException(String.format(
					"Can't convert %s to bool.", o));
		}

		throw new IllegalArgumentException(String.format(
				"Can't convert from %s to %s.",
				o.getClass().getCanonicalName(), klass.getCanonicalName()));
	}

	/**
	 * Sets a property named with propPath to a certain value using reflection.
	 * 
	 * Example: setProperty( obj, "a.b.c", 5 ); is equivalent to a direct call
	 * obj.getA().getB().setC(5)
	 */
	public static void setProperty(Object o, String propPath, Object value) {
		try {
			String[] segments = propPath.split("\\.");
			// call getters until we finally arrive where we can call the
			// setter-method
			for (int i = 0; i < segments.length; i++) {
				BeanInfo bi = Introspector.getBeanInfo(o.getClass());
				PropertyDescriptor[] pds = bi.getPropertyDescriptors();

				PropertyDescriptor match = null;
				for (PropertyDescriptor pd : pds) {
					if (pd.getName().equals(segments[i])) {
						match = pd;
						break; // for
					}
				}
				if (match == null)
					throw new IllegalArgumentException("segment '"
							+ segments[i] + "' not found of property path '"
							+ propPath + "'.");
				Method m;
				if (i == segments.length - 1) {
					// call setter
					m = match.getWriteMethod();
					o = m.invoke(o, convert(value, match.getPropertyType()));
				} else {
					// call getter and continue
					m = match.getReadMethod();
					o = m.invoke(o);
				}
			}
		} catch (Exception e1) {
			throw new RuntimeException("Can't set property '" + propPath
					+ "' to value '" + value + "'.", e1);
		}
	}

	/**
	 * Gets a property named with propPath using reflection.
	 * 
	 * Example: getProperty( obj, "a.b.c" ); is equivalent to a direct call
	 * obj.getA().getB().getC()
	 */
	public static Object getProperty(Object o, String propPath) {
		try {
			String[] segments = propPath.split("\\.");
			// call getters until we finally arrive where we can call the
			// final get-method
			for (int i = 0; i < segments.length; i++) {
				BeanInfo bi = Introspector.getBeanInfo(o.getClass());
				PropertyDescriptor[] pds = bi.getPropertyDescriptors();

				PropertyDescriptor match = null;
				for (PropertyDescriptor pd : pds) {
					if (pd.getName().equals(segments[i])) {
						match = pd;
						break; // for
					}
				}
				if (match == null)
					throw new IllegalArgumentException("segment '"
							+ segments[i] + "' not found of property path '"
							+ propPath + "'.");

				// call getter and continue
				Method m = match.getReadMethod();
				o = m.invoke(o);
			}

			return o;
		} catch (Exception e1) {
			throw new RuntimeException(
					"Can't get property '" + propPath + "'.", e1);
		}
	}

	/**
	 * Finds (bean) properties of <code>o</code> which have both getter and
	 * setter methods.
	 */
	public static PropertyDescriptor[] findWritableProperties(Object o) {
		try {
			BeanInfo bi = Introspector.getBeanInfo(o.getClass());

			PropertyDescriptor[] pds = bi.getPropertyDescriptors();

			ArrayList<PropertyDescriptor> list = new ArrayList<PropertyDescriptor>();

			for (PropertyDescriptor pd : pds) {
				if (pd.getWriteMethod() != null)
					list.add(pd);
			}

			return list.toArray(new PropertyDescriptor[list.size()]);
		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * This method returns a clone of an object, if this object is cloneable.
	 * The clone is created by calling <code>clone()</code> using Java
	 * reflection, therefore clone not necessaritly has to be public.
	 * 
	 * @param o
	 *            The object to be cloned.
	 * @return A clone of o was cloneable, or otherwise the original object.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T cloneIfPossible(T o) {
		// array?
		Class<?> o2 = o.getClass().getComponentType();
		if (o2 == null)
			o2 = o.getClass();

		if (Cloneable.class.isAssignableFrom(o2)) {
			// o or an array's components are clonable
			try {
				Method cloneMethod = o.getClass().getMethod("clone",
						new Class[] {});
				return (T) cloneMethod.invoke(o, new Object[] {});
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else
			return o;
	}

	/**
	 * Produces a deep clone of {@code array}, i.e., for each element of
	 * {@code array} creating a clone is attempted using
	 * {@link #cloneIfPossible(Object)}.
	 * 
	 * @param array
	 *            The array to be cloned.
	 * @return A clone of {@code array} with each element also cloned.
	 */
	public static <T> T[] deepCloneArrayIfPossible(T[] array) {
		if (array == null)
			return null;

		T[] clone = array.clone();
		for (int i = 0; i < clone.length; i++) {
			clone[i] = cloneIfPossible(array[i]);
		}

		return clone;
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
	 * Examples: "5" -> {5}; "23,5..10,3" -> {23,5,6,7,8,9,10,3}; "1,2,3" ->
	 * {1,2,3}
	 */
	public static int[] parseIntList(String list) {
		ArrayList<Integer> res = new ArrayList<Integer>();
		String[] ss = list.split(",");
		for (String s : ss) {
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

	public static double round(double val, int decimals) {
		if (decimals < 0)
			throw new IllegalArgumentException("" + decimals);

		double fact = Math.pow(10, decimals);
		return Math.round(val * fact) / fact;
	}

	/**
	 * @return True, if the absolute value of their differences of two double
	 *         values is smaller than the constant EPSILON (default: 10^-10).
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

}
