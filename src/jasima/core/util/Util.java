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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

/**
 * Some static utility methods that don't really fit anywhere else.
 * 
 * @author Torsten Hildebrandt
 * @version "$Id$"
 */
public class Util {

	/**
	 * Special String value that is recognized as {@code null} in
	 * {@link #setPropertyEx(Object, String, Object, ClassLoader, String[])}.
	 */
	public static final String NULL = "@null";

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
	 * A {@code TypeConversionException} is thrown, when the conversion between
	 * types in {@link Util#convert(Object, Class)} fails.
	 */
	public static class TypeConversionException extends
			IllegalArgumentException {
		private static final long serialVersionUID = -6958941745746368647L;

		public TypeConversionException(String s) {
			super(s);
		}

	}

	/**
	 * Attempts trivial type conversion. This methods supports all casting
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
	 * @throws TypeConversionException
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

		if (klass == long.class || klass == Long.class) {
			if (o instanceof Number)
				return (E) (Long) ((Number) o).longValue();
			return (E) Long.valueOf(o.toString());
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
			throw new TypeConversionException(String.format(Util.DEF_LOCALE,
					"Can't convert %s to bool.", o));
		}

		if (klass.isEnum()) {
			return (E) Enum.valueOf(klass.asSubclass(Enum.class), o.toString());
		}

		if (klass == byte.class || klass == Byte.class) {
			if (o instanceof Number)
				return (E) (Byte) ((Number) o).byteValue();
			return (E) Byte.valueOf(o.toString());
		}

		if (klass == short.class || klass == Short.class) {
			if (o instanceof Number)
				return (E) (Short) ((Number) o).shortValue();
			return (E) Short.valueOf(o.toString());
		}

		if (klass == float.class || klass == Float.class) {
			if (o instanceof Number)
				return (E) (Float) ((Number) o).floatValue();
			return (E) Float.valueOf(o.toString());
		}

		if (klass == char.class || klass == Character.class) {
			if (o instanceof Character)
				return (E) (Character) o;
			String s = o.toString();
			if (s.length() == 1)
				return (E) new Character(s.charAt(0));
		}

		throw new TypeConversionException(String.format(Util.DEF_LOCALE,
				"Can't convert from '%s' to '%s'.", o.getClass().getName(),
				klass.getName()));
	}

	/**
	 * Sets a property named with propPath to a certain value using reflection.
	 * <p />
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
					if (m == null)
						throw new RuntimeException("Property '" + segments[i]
								+ "' is not writable.");
					o = m.invoke(o, convert(value, match.getPropertyType()));
				} else {
					// call getter and continue
					m = match.getReadMethod();
					o = m.invoke(o);
				}
			}
		} catch (Exception e1) {
			throw new RuntimeException("Can't set property '" + propPath
					+ "' to value '" + value + "'. " + e1.getMessage(), e1);
		}
	}

	/**
	 * Attempts to set an object's property to a certain value. In addition to
	 * the method {@link #setProperty(Object, String, Object)}, this method
	 * tries to instantiate classes given by classname or load xml-serialized
	 * objects, if a simple {@link #setProperty(Object, String, Object)} is not
	 * successful.
	 * 
	 * @param o
	 *            The object whose property has to be set.
	 * @param propName
	 *            Name of the property to be set.
	 * @param value
	 *            The value to set this property to. If this is a String and
	 *            setting the value by ffff fails, this is interpreted as a
	 *            class name of file name.
	 * @param loader
	 *            The classloader to use when new classes have to be
	 *            initialized.
	 * @param packageSearchPath
	 *            The package search path to use for looking up abbreviated
	 *            class names.
	 * 
	 * @see #loadClassOrXmlFile(String, ClassLoader, String[])
	 * @see #searchAndInstanciateClass(String, ClassLoader, String[])
	 * @see #loadXmlFile(String)
	 */
	public static void setPropertyEx(Object o, String propName, Object value,
			ClassLoader loader, String[] packageSearchPath) {
		if (NULL.equals(value)) {
			Util.setProperty(o, propName, null);
		} else if (value instanceof String) {
			String s = (String) value;
			try {
				// try to use s as is
				Util.setProperty(o, propName, s);
			} catch (RuntimeException e) {
				if (e.getCause() instanceof TypeConversionException) {
					Pair<String, Map<String, Object>> parsed = ListTokenizer
							.parseClassAndPropDef(s);
					try {
						value = createObjectTree(parsed, loader,
								packageSearchPath, false);
					} catch (RuntimeException ex) {
						ex.printStackTrace();
						value = null;
					}
//
//					// try to interpret 's' as class or file name
//					value = loadClassOrXmlFile(s, loader, packageSearchPath);
					if (value != null) {
						Util.setProperty(o, propName, value);
						return;
					}
				}
				// still no luck? give up
				throw e;
			}
		} else {
			// try to use value as is
			Util.setProperty(o, propName, value);
		}
	}

	private static Object createObjectTree(
			Pair<String, Map<String, Object>> parsed, ClassLoader loader,
			String[] packageSearchPath, boolean sub) {
		Object root = loadClassOrXmlFile(parsed.a, loader, packageSearchPath);
		if (root != null && parsed.b != null) {
			Map<String, Object> props = parsed.b;
			for (Entry<String, Object> e : props.entrySet()) {
				String propName = e.getKey();
				Object value = e.getValue();

				if (value instanceof Pair) {
					value = createObjectTree(
							(Pair<String, Map<String, Object>>) value, loader,
							packageSearchPath, true);
				}

				// plain value
				setPropertyEx(root, propName, value, loader, packageSearchPath);
			}
		}
		return root;
	}

	/**
	 * Attempts to load an object from a xml-file {@code fileName}. If such a
	 * file does not exist or is not readable, {@code null} will be returned.
	 * 
	 * @see XmlUtil#loadXML(File)
	 */
	public static Object loadXmlFile(String fileName) {
		File f = new File(fileName);
		if (!f.canRead())
			return null;

		Object o = XmlUtil.loadXML(f);
		return o;
	}

	/**
	 * Tries to instantiate a class given by {@code classOrFilename}. If this
	 * does not succeed, {@code classOrFilename} is interpreted as a file name
	 * of an xml-serialized object and attempted to be loaded. If this is also
	 * not successful, {@code null} will be returned.
	 * 
	 * @see #searchAndInstanciateClass(String, ClassLoader, String[])
	 * @see #loadXmlFile(String)
	 */
	public static Object loadClassOrXmlFile(String classOrFilename,
			ClassLoader l, String[] packageSearchPath) {
		// try to find a class of the given name first
		Object o = searchAndInstanciateClass(classOrFilename, l,
				packageSearchPath);

		if (o == null) {
			// try to load from file
			o = loadXmlFile(classOrFilename);
		}

		return o;
	}

	/**
	 * Load an instantiate a class using classloader {@code l}. If a class
	 * {@code className} is not found, it is searched for in the package search
	 * path.
	 * <p />
	 * If, e.g., {@code className} is {@code "MultipleReplicationExperiment"}
	 * and the package search path {@code searchPath} contains an entry
	 * {@code "jasima.core.experiment"}, then the class
	 * {@code jasima.core.experiment.MultipleReplicationExperiment} will be
	 * looked up and instantiated.
	 * <p />
	 * If no matching class could be found, {@code null} will be returned.
	 */
	public static Object searchAndInstanciateClass(String className,
			ClassLoader l, String[] searchPath) {
		// // does it look like containing a list of parameters we have to
		// parse?
		// if (className.contains("(")) {
		// ListTokenizer t = new ListTokenizer(className);
		//
		// if (t.nextTokenNoWhitespace()!=STRING)
		// throw new RuntimeException("Input looks strange: '%s'"+className);
		// className = t.n
		// }

		// try direct match first
		Class<?> klazz = load(className, l);

		// try matches from the class search path
		if (klazz == null)
			for (String packageName : searchPath) {
				klazz = load(packageName + "." + className, l);
				if (klazz != null)
					break; // for loop
			}

		if (klazz != null) {
			try {
				return klazz.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		} else {
			return null;
		}
	}

	/**
	 * Loads a class named {@code classname} using the classloader {@code l}.
	 * Instead of raising a {@code ClassNotFoundException}, {@code null} will be
	 * returned.
	 */
	private static Class<?> load(String classname, ClassLoader l) {
		try {
			return l.loadClass(classname);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	/**
	 * Determines the type of a property named with propPath using reflection.
	 * 
	 * This method interprets propPath the same way as
	 * {@link #getPropertyValue(Object, String)} does.
	 */
	public static Class<?> getPropertyType(Object o, String propPath) {
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
				if (i == segments.length - 1) {
					// return property type
					return match.getPropertyType();
				} else {
					// call getter and continue
					o = match.getReadMethod().invoke(o);
				}
			}
			throw new AssertionError();
		} catch (Exception e1) {
			throw new RuntimeException("Can't determine type of property "
					+ propPath, e1);
		}
	}

	/**
	 * Gets the current value of a property named with propPath using
	 * reflection.
	 * <p />
	 * Example: getProperty( obj, "a.b.c" ); is equivalent to a direct call
	 * obj.getA().getB().getC()
	 */
	public static Object getPropertyValue(Object o, String propPath) {
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
	 * Finds (bean) properties of {@code o} which have both getter and setter
	 * methods.
	 */
	public static PropertyDescriptor[] findWritableProperties(Object o) {
		try {
			BeanInfo bi = Introspector.getBeanInfo(o.getClass());

			PropertyDescriptor[] pds = bi.getPropertyDescriptors();

			ArrayList<PropertyDescriptor> list = new ArrayList<PropertyDescriptor>();

			for (PropertyDescriptor pd : pds) {
				if (pd.getWriteMethod() != null && pd.getReadMethod() != null)
					list.add(pd);
			}

			return list.toArray(new PropertyDescriptor[list.size()]);
		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		}
	}

	private static final String PROP_JASIMA_EXPERIMENT = "jasima.experiment";
	private static final String PROP_SUN_JAVA_COMMAND = "sun.java.command";

	/**
	 * Tries to find the main class of a java run. This is attempted by looking
	 * up the system properties {@code jasima.experiment} and
	 * {@code sun.java.command} first. If this does not lead to a valid
	 * classname (e.g., if started with "-jar" option) an attempt is made to
	 * interpret the property as the name of a jar file. The manifest of this
	 * jar is then searched for its entry {@code Main-Class}.
	 * <p />
	 * This code is necessary because Java has no virtual static methods and
	 * therefore there is no equivalent to the keyword {@code this} in a static
	 * method.
	 */
	public static Class<?> getMainClass() throws ClassNotFoundException {
		Properties props = System.getProperties();

		String main = (String) findEntryCaseInsensitive(props,
				PROP_JASIMA_EXPERIMENT);
		if (main == null) {
			main = (String) findEntryCaseInsensitive(props,
					PROP_SUN_JAVA_COMMAND);
		}
		if (main == null) {
			throw new RuntimeException(String.format(Util.DEF_LOCALE,
					"Couldn't find properties '%s' or '%s'.",
					PROP_SUN_JAVA_COMMAND, PROP_JASIMA_EXPERIMENT));
		}

		// strip any arguments, if present
		String classOrJar;
		try (Scanner s = new Scanner(main)) {
			classOrJar = s.next();
		}

		try {
			// try to find as class directly
			Class<?> klazz = Util.class.getClassLoader().loadClass(classOrJar);
			return klazz;
		} catch (ClassNotFoundException e) {
			// try to interpret as jar and load main class name from manifest.mf
			try {
				return loadFromJar(classOrJar);
			} catch (IOException ignore) {
				// re-throw e;
				throw e;
			}
		}
	}

	private static Class<?> loadFromJar(String classOrJar) throws IOException,
			ClassNotFoundException {
		try (JarFile jar = new JarFile(classOrJar)) {
			Map<String, Attributes> jarEntries = jar.getManifest().getEntries();
			// is app using "jar in jar" export from eclipse? in this case
			// main-class is JarRsrcLoader
			Attributes o = (Attributes) findEntryCaseInsensitive(jarEntries,
					"Rsrc-Main-Class");
			if (o == null || o.size() == 0) {
				// regular main class
				o = (Attributes) findEntryCaseInsensitive(jarEntries,
						"Main-Class");
			}
			assert o.size() == 1;

			// get first entry from 'o'
			String cName = (String) o.values().iterator().next();

			// try to load cName
			Class<?> klazz = Util.class.getClassLoader().loadClass(cName);
			return klazz;
		}
	}

	private static Object findEntryCaseInsensitive(Map<?, ?> jarEntries,
			String entry) {
		entry = entry.toLowerCase();
		for (Object o : jarEntries.keySet()) {
			String s = ((String) o).toLowerCase();
			if (entry.equals(s)) {
				return jarEntries.get(o);
			}
		}
		return null;
	}

	/**
	 * This method returns a clone of an object, if this object is cloneable.
	 * The clone is created by calling <code>clone()</code> using Java
	 * reflection, therefore <code>clone()</code> not necessarily has to be
	 * public.
	 * 
	 * @param o
	 *            The object to be cloned.
	 * @return A clone of {@code o} if it was cloneable, or otherwise the
	 *         original object.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T cloneIfPossible(T o) {
		if (o == null)
			return null;

		// array?
		Class<?> o2 = o.getClass().getComponentType();
		if (o2 == null)
			o2 = o.getClass();

		if (Cloneable.class.isAssignableFrom(o2)) {
			// TODO: deep clone arrays?
			// o or an array's components are clonable
			try {
				Method cloneMethod = o.getClass().getMethod("clone",
						new Class[] {});
				return (T) cloneMethod.invoke(o);
			} catch (NoSuchMethodException ignore) {
				// clonable, but no public clone-method, return o as is
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

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
	 * separator) to a double-array. Example: parseDblList("1.23,4.56") ->
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
	 */
	private static String getVersion() {
		String v = Util.class.getPackage().getImplementationVersion();
		return v == null ? DEFAULT_VERSION : v;
	}

}
