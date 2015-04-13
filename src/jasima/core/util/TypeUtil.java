package jasima.core.util;

import static jasima.core.util.converter.TypeConverterJavaBean.exceptionMessage;
import static jasima.core.util.converter.TypeToStringConverter.convertToString;
import jasima.core.util.converter.TypeToStringConverter;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.WeakHashMap;
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
	 * A {@code TypeConversionException} is thrown, when the conversion between
	 * types fails.
	 */
	public static class TypeConversionException extends RuntimeException {
		private static final long serialVersionUID = -7073321632899508315L;

		public TypeConversionException(String s) {
			this(s, null);
		}

		public TypeConversionException(String s, Throwable cause) {
			super(s, cause);
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
				PropertyDescriptor match = getPropertyDescriptor(o, segments[i]);
				if (match == null)
					throw new IllegalArgumentException(String.format(
							Util.DEF_LOCALE,
							"segment '%s' not found of property path '%s'.",
							segments[i], propPath));

				if (i == segments.length - 1) {
					// return property type
					return match.getPropertyType();
				} else {
					// call getter and continue
					o = match.getReadMethod().invoke(o);
				}
			}

			throw new AssertionError(); // should never be reached
		} catch (ReflectiveOperationException e1) {
			throw new RuntimeException(String.format(Util.DEF_LOCALE,
					"Can't determine type of property '%s': %s", propPath,
					e1.toString()), e1);
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
				PropertyDescriptor match = getPropertyDescriptor(o, segments[i]);
				if (match == null)
					throw new IllegalArgumentException(String.format(
							Util.DEF_LOCALE,
							"segment '%s' not found of property path '%s'.",
							segments[i], propPath));

				// call getter and continue
				Method m = match.getReadMethod();
				o = m.invoke(o);
			}

			return o;
		} catch (ReflectiveOperationException e1) {
			throw new RuntimeException(String.format(Util.DEF_LOCALE,
					"Can't get property '%s'.", propPath), e1);
		}
	}

	/**
	 * Calls
	 * {@link #setPropertyValue(Object, String, Object, ClassLoader, String[])}
	 * using the ClassLoader that was used to load {@code TypeUtil} and the
	 * default package search path {@link Util#DEF_CLASS_SEARCH_PATH}.
	 * 
	 * @param o
	 *            The object with a property to set.
	 * @param propPath
	 *            The property path and name of the property to set.
	 * @param value
	 *            The value to set the property to.
	 * @see #setPropertyValue(Object, String, Object, ClassLoader, String[])
	 */
	public static void setPropertyValue(Object o, String propPath, Object value) {
		setPropertyValue(o, propPath, value, TypeUtil.class.getClassLoader(),
				Util.DEF_CLASS_SEARCH_PATH);
	}

	/**
	 * Sets a property named with propPath to a certain value using reflection.
	 * <p>
	 * Example: setProperty( obj, "a.b.c", 5 ); is equivalent to a direct call
	 * obj.getA().getB().setC(5)
	 * 
	 * @param o
	 *            The object with a property to set.
	 * @param propPath
	 *            The property path and name of the property to set.
	 * @param value
	 *            The value to set the property to.
	 * @param loader
	 *            The {@link ClassLoader} to use when new classes have to be
	 *            loaded.
	 * @param packageSearchPath
	 *            A list of package names that are used to complete abbreviated
	 *            class names.
	 */
	public static void setPropertyValue(Object o, String propPath,
			Object value, ClassLoader loader, String[] packageSearchPath) {
		String getPart;
		String setPart;
		int i = propPath.lastIndexOf('.');
		if (i >= 0) {
			getPart = propPath.substring(0, i);
			setPart = propPath.substring(i + 1);
		} else {
			getPart = "";
			setPart = propPath;
		}

		if (getPart.length() > 0)
			o = getPropertyValue(o, getPart);

		// 'o' now contains the object for which to call the setter
		//
		// find property descriptor
		PropertyDescriptor desc = getPropertyDescriptor(o, setPart);
		if (desc == null)
			throw new IllegalArgumentException(String.format(Util.DEF_LOCALE,
					"Segment '%s' not found of property path '%s'.", setPart,
					propPath));

		value = convert(value, desc.getPropertyType(), getPart, loader,
				packageSearchPath);

		try {
			desc.getWriteMethod().invoke(o, value);
		} catch (ReflectiveOperationException e1) {
			throw new RuntimeException(String.format(
					"Can't set property '%s' to value '%s': %s", propPath,
					value, exceptionMessage(e1)), e1);
		}
	}

	/**
	 * Converts an object {@code o} (which usually is a {@code String}) to
	 * another type {@code requiredType}.
	 * 
	 * @param o
	 *            The object to convert.
	 * @param requiredType
	 *            The desired type {@code o} should be converted to.
	 * @param context
	 *            A String describing the context of {@code o}. This is used to
	 *            produce more meaningful error messages.
	 * @param l
	 *            The {@link ClassLoader} to use.
	 * @param packageSearchPath
	 *            Search path when looking up classes.
	 * @param <T>
	 *            Type of returned object.
	 * @return {@code o} converted to {@code requiredType}.
	 */
	public static <T> T convert(Object o, Class<T> requiredType,
			String context, ClassLoader l, String[] packageSearchPath)
			throws TypeConversionException {
		T value;
		if (o instanceof String) {
			value = TypeToStringConverter.convertFromString((String) o,
					requiredType, context, l, packageSearchPath);
		} else {
			value = basicConversions(o, requiredType);
		}
		return value;
	}

	/**
	 * Computes an array of all super-classes and interfaces of
	 * {@code requiredType}. This method performs a breadth first traversal of
	 * the class/interface hierarchy. Consider the following example:
	 * 
	 * <pre>
	 *     interface A extends M, N
	 *     interface B extends O
	 *     class Y implements C, D
	 *     class X extends Y implements A, B
	 * </pre>
	 * 
	 * This will produce the following result for {@code x} as
	 * {@code requiredType}:
	 * 
	 * <pre>
	 * { X, Y, A, B, C, D, M, N, O, Object }
	 * </pre>
	 * 
	 * @param requiredType
	 *            The class for which to compute the type hierarchy.
	 * @return A list of super classes/interfaces from most to least specific.
	 */
	public static Class<?>[] computeClasses(Class<?> requiredType) {
		ArrayList<Class<?>> resList = new ArrayList<>();
		ArrayDeque<Class<?>> currStage = new ArrayDeque<>();
		HashSet<Class<?>> seen = new HashSet<Class<?>>();
		currStage.addLast(requiredType);
		seen.add(requiredType);

		while (!currStage.isEmpty()) {
			Class<?> c = currStage.removeFirst();
			resList.add(c);

			Class<?> s = c.getSuperclass();
			if (s != null && !seen.contains(s)) {
				currStage.addLast(s);
				seen.add(s);
			}

			for (Class<?> i : c.getInterfaces()) {
				if (!seen.contains(i)) {
					currStage.addLast(i);
					seen.add(i);
				}
			}
		}

		// ensure Object is last in list (if present)
		boolean hasObject = resList.remove(Object.class);
		if (hasObject) {
			resList.add(Object.class);
		}

		return resList.toArray(new Class<?>[resList.size()]);
	}

	/**
	 * Finds (bean) properties of {@code o} which have both getter and setter
	 * methods.
	 * 
	 * @param o
	 *            An arbitrary object.
	 * @return An array containing a {@link PropertyDescriptor} for each
	 *         property of {@code o}.
	 * @see #findWritableProperties(Class)
	 */
	public static PropertyDescriptor[] findWritableProperties(Object o) {
		return findWritableProperties(o.getClass());
	}

	/**
	 * Finds (bean) properties of {@code c} which have both getter and setter
	 * methods. If an {@link IntrospectionException} is raised during when
	 * executing the method, then this exception is raised again as an unchecked
	 * exception (wrapped in a {@link RuntimeException}).
	 * 
	 * @param c
	 *            An arbitrary class.
	 * @return An array containing a {@link PropertyDescriptor} for each
	 *         property of {@code c}.
	 */
	public static PropertyDescriptor[] findWritableProperties(Class<?> c) {
		try {
			BeanInfo bi = Introspector.getBeanInfo(c);

			PropertyDescriptor[] pds = bi.getPropertyDescriptors();
			ArrayList<PropertyDescriptor> list = new ArrayList<PropertyDescriptor>(
					pds.length);
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
	 * Utility method to return a {@code PropertyDescriptor}. Name matching is
	 * case in-sensitive.
	 * 
	 * @param o
	 *            The object for which to get the property.
	 * @param propName
	 *            Name of the bean property.
	 * @return A {@code PropertyDescriptor} matching {@code propName}, otherwise
	 *         {@code null}.
	 */
	private static PropertyDescriptor getPropertyDescriptor(Object o,
			String propName) {
		Map<String, PropertyDescriptor> props = writableProperties(o.getClass());
		PropertyDescriptor desc = props.get(propName
				.toLowerCase(Util.DEF_LOCALE));
		return desc;
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
	 * @param <T>
	 *            Type of returned object.
	 * @return the converted object
	 * @throws TypeConversionException
	 *             if the conversion is not supported
	 * @throws NumberFormatException
	 *             if the input object is not assignable to {@link Number} and
	 *             the return value of its {@link Object#toString()} method
	 *             can't be converted to the numeric target type
	 */
	@SuppressWarnings("unchecked")
	private static <T> T basicConversions(Object o, Class<T> klass)
			throws TypeConversionException, NumberFormatException {
		if (o == null)
			return null;

		assert !(o instanceof String);

		if (klass.isAssignableFrom(o.getClass())) {
			return (T) o;
		}

		if (klass == String.class) {
			return (T) convertToString(o);
		}

		if (klass == int.class || klass == Integer.class) {
			if (o instanceof Number)
				return (T) (Integer) ((Number) o).intValue();
			return (T) Integer.valueOf(convertToString(o));
		}

		if (klass == long.class || klass == Long.class) {
			if (o instanceof Number)
				return (T) (Long) ((Number) o).longValue();
			return (T) Long.valueOf(convertToString(o));
		}

		if (klass == double.class || klass == Double.class) {
			if (o instanceof Number)
				return (T) (Double) ((Number) o).doubleValue();
			return (T) Double.valueOf(convertToString(o));
		}

		if (klass == boolean.class || klass == Boolean.class) {
			String str = convertToString(o);
			if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("yes")
					|| str.equalsIgnoreCase("1"))
				return (T) Boolean.TRUE;
			if (str.equalsIgnoreCase("false") || str.equalsIgnoreCase("no")
					|| str.equalsIgnoreCase("0"))
				return (T) Boolean.FALSE;
			throw new TypeConversionException(String.format(Util.DEF_LOCALE,
					"Can't convert '%s' to bool.", o));
		}

		if (klass.isEnum()) {
			return (T) Enum.valueOf(klass.asSubclass(Enum.class),
					convertToString(o));
		}

		if (klass == byte.class || klass == Byte.class) {
			if (o instanceof Number)
				return (T) (Byte) ((Number) o).byteValue();
			return (T) Byte.valueOf(convertToString(o));
		}

		if (klass == short.class || klass == Short.class) {
			if (o instanceof Number)
				return (T) (Short) ((Number) o).shortValue();
			return (T) Short.valueOf(convertToString(o));
		}

		if (klass == float.class || klass == Float.class) {
			if (o instanceof Number)
				return (T) (Float) ((Number) o).floatValue();
			return (T) Float.valueOf(convertToString(o));
		}

		if (klass == char.class || klass == Character.class) {
			if (o instanceof Character)
				return (T) (Character) o;
			String s = convertToString(o);
			if (s.length() == 1)
				return (T) new Character(s.charAt(0));
		}

		throw new TypeConversionException(String.format(Util.DEF_LOCALE,
				"Can't convert from '%s' to '%s'.", o.getClass().getName(),
				klass.getName()));
	}

	private static WeakHashMap<Class<?>, Map<String, PropertyDescriptor>> propCache = null;

	/**
	 * Returns a map of property descriptors. Keys in this map are the property
	 * names converted to lower case.
	 * 
	 * @param c
	 *            The class for which to find the properties.
	 * @return A map associating a property name (converted to lower case) with
	 *         a {@link PropertyDescriptor}.
	 */
	public static Map<String, PropertyDescriptor> writableProperties(Class<?> c) {
		if (propCache == null)
			propCache = new WeakHashMap<>();

		Map<String, PropertyDescriptor> beanProps = propCache.get(c);

		if (beanProps == null) {
			PropertyDescriptor[] props = findWritableProperties(c);
			beanProps = new HashMap<>();
			for (PropertyDescriptor p : props) {
				beanProps.put(p.getName().toLowerCase(Util.DEF_LOCALE), p);
			}
			propCache.put(c, beanProps);
		}
		return beanProps;
	}

	/**
	 * This method returns a clone of an object, if this object is cloneable.
	 * The clone is created by calling <code>clone()</code> using Java
	 * reflection, therefore <code>clone()</code> not necessarily has to be
	 * public.
	 * 
	 * @param o
	 *            The object to be cloned.
	 * @param <T>
	 *            Type of returned object.
	 * @return A clone of {@code o} if it was {@link Cloneable}, or otherwise
	 *         the original object.
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
				// clonable, but no public clone-method, return "o" as is
			} catch (ReflectiveOperationException e) {
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
	 * @param <T>
	 *            Component type of the array.
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
