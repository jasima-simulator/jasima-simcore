package jasima.core.util;

import jasima.core.util.ArgListParser.ParseTree;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

/**
 * This class contains a collection of methods concernde with
 * reading/writing/creating/converting Bean properties.
 * 
 * @author Torsten Hildebrandt
 */
public class TypeUtil {

	/**
	 * Special String value that is recognized as {@code null} in
	 * {@link #setPropertyEx(Object, String, Object, ClassLoader, String[])}.
	 */
	public static final String NULL = "@null";

	/**
	 * A {@code TypeConversionException} is thrown, when the conversion between
	 * types in {@link #convert(Object, Class)} fails.
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
	 * <p>
	 * Sets a property named with propPath to a certain value using reflection.
	 * </p>
	 * <p>
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
			setProperty(o, propName, null);
		} else if (value instanceof String) {
			String s = (String) value;
			try {
				// try to use s as is
				setProperty(o, propName, s);
			} catch (RuntimeException e) {
				if (e.getCause() instanceof TypeConversionException) {
					ParseTree parsed = ArgListParser.parseClassAndPropDef(s);
					try {
						value = createObjectTree(parsed, loader,
								packageSearchPath, false);
					} catch (RuntimeException ex) {
						ex.printStackTrace();
						value = null;
					}
					//
					// // try to interpret 's' as class or file name
					// value = loadClassOrXmlFile(s, loader, packageSearchPath);
					if (value != null) {
						setProperty(o, propName, value);
						return;
					}
				}
				// still no luck? give up
				throw e;
			}
		} else {
			// try to use value as is
			setProperty(o, propName, value);
		}
	}

	private static Object createObjectTree(ParseTree parsed,
			ClassLoader loader, String[] packageSearchPath, boolean sub) {
		Object root = loadClassOrXmlFile(parsed.getClassOrXmlName(), loader,
				packageSearchPath);
		if (root != null && parsed.getParams() != null) {
			Map<String, ParseTree> props = parsed.getParams();
			for (Entry<String, ParseTree> e : props.entrySet()) {
				String propName = e.getKey();
				Object value = createObjectTree(e.getValue(), loader,
						packageSearchPath, true);

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
	 * <p>
	 * Load an instantiate a class using classloader {@code l}. If a class
	 * {@code className} is not found, it is searched for in the package search
	 * path.
	 * </p>
	 * <p>
	 * If, e.g., {@code className} is {@code "MultipleReplicationExperiment"}
	 * and the package search path {@code searchPath} contains an entry
	 * {@code "jasima.core.experiment"}, then the class
	 * {@code jasima.core.experiment.MultipleReplicationExperiment} will be
	 * looked up and instantiated.
	 * </p>
	 * <p>
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
	 * <p>
	 * Gets the current value of a property named with propPath using
	 * reflection.
	 * </p>
	 * <p>
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

	private static final String PROP_JASIMA_EXPERIMENT = "jasima.experiment";
	private static final String PROP_SUN_JAVA_COMMAND = "sun.java.command";

	/**
	 * <p>
	 * Tries to find the main class of a java run. This is attempted by looking
	 * up the system properties {@code jasima.experiment} and
	 * {@code sun.java.command} first. If this does not lead to a valid
	 * classname (e.g., if started with "-jar" option) an attempt is made to
	 * interpret the property as the name of a jar file. The manifest of this
	 * jar is then searched for its entry {@code Main-Class}.
	 * </p>
	 * <p>
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

}
