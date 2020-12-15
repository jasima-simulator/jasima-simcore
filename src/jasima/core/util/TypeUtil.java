/*******************************************************************************
 * This file is part of jasima, v1.3, the Java simulator for manufacturing and 
 * logistics.
 *  
 * Copyright (c) 2015 		jasima solutions UG
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package jasima.core.util;

import static jasima.core.util.converter.TypeConverterJavaBean.exceptionMessage;
import static jasima.core.util.converter.TypeToStringConverter.convertToString;
import static jasima.core.util.i18n.I18n.defFormat;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.WeakHashMap;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import jasima.core.util.converter.TypeToStringConverter;
import jasima.core.util.i18n.I18n;

/**
 * This class contains a collection of methods concerned with
 * reading/writing/creating/converting Bean properties and loading classes using
 * Java reflection.
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
	 * <p>
	 * This method interprets propPath the same way as
	 * {@link #getPropertyValue(Object, String)} does.
	 * 
	 * @param o        The Object from which to get a property value.
	 * @param propPath A String containing the path to the required property.
	 * @return The property's type.
	 * 
	 * @throws RuntimeException If there was a problem getting the value. The cause
	 *                          of this exception (of type
	 *                          {@link ReflectiveOperationException}) gives a more
	 *                          detailed indication why the operation failed.
	 */
	public static Class<?> getPropertyType(Object o, String propPath) throws RuntimeException {
		try {
			String[] segments = propPath.split("\\.");

			for (int i = 0; i < segments.length - 1; i++) {
				o = handleSegmentGet(o, propPath, segments[i]);
			}

			// last segment handled differently
			String last = segments[segments.length - 1];

			// do we have an array access?
			int arrayElement = -1;
			if (last.contains("[")) {
				int i1 = propPath.indexOf('[');
				int i2 = propPath.indexOf(']');
				arrayElement = Integer.parseInt(propPath.substring(i1 + 1, i2));
				if (arrayElement < 0)
					throw new IllegalArgumentException("invalid array/list access");

				if (last.length() == i2 + 1) {
					last = last.substring(0, i1);
				}
			}

			PropertyDescriptor match = getPropertyDescriptor(o, last);
			if (match == null)
				throw new IllegalArgumentException(
						defFormat("segment '%s' not found of property path '%s'.", last, propPath));

			if (arrayElement == -1) {
				// return property type
				return match.getPropertyType();
			} else {
				if (match.getPropertyType().isArray()) {
					return match.getPropertyType().getComponentType();
				} else if (List.class.isAssignableFrom(match.getPropertyType())) {
					// TODO: find more specific component-type (via generics)
					return Object.class;
				} else {
					throw new IllegalArgumentException("not an array/list type");
				}
			}
		} catch (ReflectiveOperationException | IllegalArgumentException e1) {
			throw new RuntimeException(defFormat("Can't determine type of property '%s': %s", propPath, e1.toString()),
					e1);
		}
	}

	/**
	 * Gets the current value of a property named with propPath using reflection.
	 * <p>
	 * Example: getProperty( obj, "a.b.c" ); is equivalent to a direct call
	 * obj.getA().getB().getC()
	 * 
	 * @param o        The Object from which to get a property value.
	 * @param propPath A String containing the path to the required property.
	 * @return The property value.
	 * 
	 * @throws RuntimeException If there was a problem getting the value. The cause
	 *                          of this exception (of type
	 *                          {@link ReflectiveOperationException}) gives a more
	 *                          detailed indication why the operation failed.
	 */
	public static Object getPropertyValue(Object o, String propPath) throws RuntimeException {
		try {
			String[] segments = propPath.split("\\.");
			// call getters until we finally arrive where we can call the
			// final get-method
			for (int i = 0; i < segments.length; i++) {
				o = handleSegmentGet(o, propPath, segments[i]);
			}

			return o;
		} catch (ReflectiveOperationException | IllegalArgumentException e1) {
			throw new RuntimeException(defFormat("Can't get property '%s'.", propPath), e1);
		}
	}

	private static Object handleSegmentGet(Object o, String propPath, String currSegment)
			throws IllegalAccessException, InvocationTargetException {
		// do we have an array access?
		int arrayElement = -1;
		if (currSegment.contains("[")) {
			int i1 = currSegment.indexOf('[');
			int i2 = currSegment.indexOf(']');
			arrayElement = Integer.parseInt(currSegment.substring(i1 + 1, i2));
			if (arrayElement < 0)
				throw new IllegalArgumentException("invalid array/list access");

			if (currSegment.length() == i2 + 1) {
				currSegment = currSegment.substring(0, i1);
			}
		}

		PropertyDescriptor match = getPropertyDescriptor(o, currSegment);
		if (match == null)
			throw new IllegalArgumentException(
					defFormat("segment '%s' not found of property path '%s'.", currSegment, propPath));

		// call getter and continue
		Method m = match.getReadMethod();
		o = m.invoke(o);

		// optionally access array/list element
		if (arrayElement >= 0) {
			if (o instanceof List) {
				o = ((List<?>) o).get(arrayElement);
			} else if (o.getClass().isArray()) {
				o = Array.get(o, arrayElement);
			} else {
				throw new IllegalArgumentException(
						String.format("Can't get property '%s': '%s' is not an array or list.", propPath, o));
			}
		}

		return o;
	}

	/**
	 * Calls
	 * {@link #setPropertyValue(Object, String, Object, ClassLoader, String[])}
	 * using the ClassLoader that was used to load {@code TypeUtil} and the default
	 * package search path {@link Util#DEF_CLASS_SEARCH_PATH}.
	 * 
	 * @param o        The object with a property to set.
	 * @param propPath The property path and name of the property to set.
	 * @param value    The value to set the property to.
	 * @see #setPropertyValue(Object, String, Object, ClassLoader, String[])
	 */
	public static void setPropertyValue(Object o, String propPath, Object value) {
		setPropertyValue(o, propPath, value, TypeUtil.class.getClassLoader(), Util.DEF_CLASS_SEARCH_PATH);
	}

	/**
	 * Sets a property named with propPath to a certain value using reflection.
	 * <p>
	 * Example: setProperty( obj, "a.b.c", 5 ); is equivalent to a direct call
	 * obj.getA().getB().setC(5)
	 * 
	 * @param o                 The object with a property to set.
	 * @param propPath          The property path and name of the property to set.
	 * @param value             The value to set the property to.
	 * @param loader            The {@link ClassLoader} to use when new classes have
	 *                          to be loaded.
	 * @param packageSearchPath A list of package names that are used to complete
	 *                          abbreviated class names.
	 */
	public static void setPropertyValue(Object o, String propPath, Object value, ClassLoader loader,
			String[] packageSearchPath) throws IllegalArgumentException {
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

		// do we have an array access?
		int arrayElement = -1;
		if (setPart.contains("[")) {
			int i1 = propPath.indexOf('[');
			int i2 = propPath.indexOf(']');
			arrayElement = Integer.parseInt(propPath.substring(i1 + 1, i2));
			if (arrayElement < 0)
				throw new IllegalArgumentException("invalid array/list access");

			if (setPart.length() > i2 + 1)
				throw new IllegalArgumentException(
						String.format("Can't set property '%s' to value '%s': invalid array access.", propPath, value));

			if (getPart.length() > 0)
				getPart += '.';
			getPart = propPath.substring(0, i1);
			setPart = null;
		}

		Object target = o;

		if (getPart.length() > 0)
			target = getPropertyValue(o, getPart);

		// 'target' now contains the object for which to call the setter
		//
		if (arrayElement == -1) {
			// handle normal property set
			PropertyDescriptor desc = getPropertyDescriptor(target, setPart);
			if (desc == null)
				throw new IllegalArgumentException(
						defFormat("Segment '%s' not found of property path '%s'.", setPart, propPath));

			value = convert(value, desc.getPropertyType(), getPart, loader, packageSearchPath);
			try {
				desc.getWriteMethod().invoke(target, value);
			} catch (ReflectiveOperationException e1) {
				throw new IllegalArgumentException(String.format("Can't set property '%s' to value '%s': %s", propPath,
						value, exceptionMessage(e1)), e1);
			}
		} else {
			// handle array/list set
			Class<?> arrayType = target.getClass();
			assert getPropertyType(o, getPart) == target.getClass();

			if (arrayType.isArray()) {
				value = convert(value, arrayType.getComponentType(), getPart, loader, packageSearchPath);
				Array.set(target, arrayElement, value);
			} else if (List.class.isAssignableFrom(arrayType)) {
				// TODO: find generic component type via reflection
				value = convert(value, Object.class, getPart, loader, packageSearchPath);

				@SuppressWarnings("unchecked")
				List<Object> l = (List<Object>) target;
				l.set(arrayElement, value);
			} else {
				throw new IllegalArgumentException(
						String.format("Can't set property '%s' to value '%s': '%s' is not an array or list.", propPath,
								value, getPart));
			}
		}
	}

	/**
	 * Converts an object {@code o} (which usually is a {@code String}) to another
	 * type {@code requiredType}.
	 * 
	 * @param o                 The object to convert.
	 * @param requiredType      The desired type {@code o} should be converted to.
	 * @param context           A String describing the context of {@code o}. This
	 *                          is used to produce more meaningful error messages.
	 * @param l                 The {@link ClassLoader} to use.
	 * @param packageSearchPath Search path when looking up classes.
	 * @param <T>               Type of returned object.
	 * @return {@code o} converted to {@code requiredType}.
	 */
	public static <T> T convert(Object o, Class<T> requiredType, String context, ClassLoader l,
			String[] packageSearchPath) throws TypeConversionException {
		T value;
		if (o instanceof String && requiredType != String.class) {
			value = TypeToStringConverter.convertFromString((String) o, requiredType, context, l, packageSearchPath);
		} else {
			value = basicConversions(o, requiredType);
		}
		return value;
	}

	/**
	 * Computes an array of all super-classes and interfaces of
	 * {@code requiredType}. This method performs a breadth first traversal of the
	 * class/interface hierarchy. Consider the following example:
	 * 
	 * <pre>
	 *     interface A extends M, N
	 *     interface B extends O
	 *     class Y implements C, D
	 *     class X extends Y implements A, B
	 * </pre>
	 * 
	 * This will produce the following result for {@code x} as {@code requiredType}:
	 * 
	 * <pre>
	 * { X, Y, A, B, C, D, M, N, O, Object }
	 * </pre>
	 * 
	 * @param requiredType The class for which to compute the type hierarchy.
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
	 * @param o An arbitrary object.
	 * @return An array containing a {@link PropertyDescriptor} for each property of
	 *         {@code o}.
	 * @see #findWritableProperties(Class)
	 */
	public static PropertyDescriptor[] findWritableProperties(Object o) {
		return findWritableProperties(o.getClass());
	}

	/**
	 * Finds (bean) properties of {@code c} which have both getter and setter
	 * methods. If an {@link IntrospectionException} is raised when executing the
	 * method, then this exception is raised again as an unchecked exception
	 * (wrapped in a {@link RuntimeException}).
	 * 
	 * @param c An arbitrary class.
	 * @return An array containing a {@link PropertyDescriptor} for each property of
	 *         {@code c}.
	 */
	public static PropertyDescriptor[] findWritableProperties(Class<?> c) {
		try {
			BeanInfo bi = Introspector.getBeanInfo(c);

			PropertyDescriptor[] pds = bi.getPropertyDescriptors();
			ArrayList<PropertyDescriptor> list = new ArrayList<PropertyDescriptor>(pds.length);
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
	 * Utility method to return a {@code PropertyDescriptor}. Name matching is case
	 * in-sensitive.
	 * 
	 * @param o        The object for which to get the property.
	 * @param propName Name of the bean property.
	 * @return A {@code PropertyDescriptor} matching {@code propName}, otherwise
	 *         {@code null}.
	 */
	private static PropertyDescriptor getPropertyDescriptor(Object o, String propName) {
		Map<String, PropertyDescriptor> props = writableProperties(o.getClass());
		PropertyDescriptor desc = props.get(propName.toLowerCase(I18n.DEF_LOCALE));
		return desc;
	}

	/**
	 * Attempts trivial type conversion. This methods supports all casting
	 * conversions (JLS 5.5) and always returns null when the input object is null.
	 * If the target type is {@link String}, the result is the return value of the
	 * input object's {@link Object#toString()} method. Any object can be converted
	 * to {@link Integer}, {@link Double} and {@link Boolean}, but those conversions
	 * can throw exceptions.
	 * 
	 * @param o     the object to be converted
	 * @param klass the target type
	 * @param <T>   Type of returned object.
	 * @return the converted object
	 * @throws TypeConversionException if the conversion is not supported
	 * @throws NumberFormatException   if the input object is not assignable to
	 *                                 {@link Number} and the return value of its
	 *                                 {@link Object#toString()} method can't be
	 *                                 converted to the numeric target type
	 */
	@SuppressWarnings("unchecked")
	private static <T> T basicConversions(Object o, Class<T> klass)
			throws TypeConversionException, NumberFormatException {
		if (o == null)
			return null;

		if (klass.isAssignableFrom(o.getClass())) {
			return (T) o;
		}

		assert !(o instanceof String);

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
			if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("yes") || str.equalsIgnoreCase("1"))
				return (T) Boolean.TRUE;
			if (str.equalsIgnoreCase("false") || str.equalsIgnoreCase("no") || str.equalsIgnoreCase("0"))
				return (T) Boolean.FALSE;
			throw new TypeConversionException(defFormat("Can't convert '%s' to bool.", o));
		}

		if (klass.isEnum()) {
			return (T) Enum.valueOf(klass.asSubclass(Enum.class), convertToString(o));
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
				return (T) Character.valueOf(s.charAt(0));
		}

		throw new TypeConversionException(
				defFormat("Can't convert from '%s' to '%s'.", o.getClass().getName(), klass.getName()));
	}

	private static WeakHashMap<Class<?>, Map<String, PropertyDescriptor>> propCache = null;

	/**
	 * Returns a map of property descriptors. Keys in this map are the property
	 * names converted to lower case.
	 * 
	 * @param c The class for which to find the properties.
	 * @return A map associating a property name (converted to lower case) with a
	 *         {@link PropertyDescriptor}.
	 */
	public static Map<String, PropertyDescriptor> writableProperties(Class<?> c) {
		if (propCache == null)
			propCache = new WeakHashMap<>();

		Map<String, PropertyDescriptor> beanProps = propCache.get(c);

		if (beanProps == null) {
			PropertyDescriptor[] props = findWritableProperties(c);
			beanProps = new HashMap<>();
			for (PropertyDescriptor p : props) {
				beanProps.put(p.getName().toLowerCase(I18n.DEF_LOCALE), p);
			}
			propCache.put(c, beanProps);
		}
		return beanProps;
	}

	/**
	 * This method returns a clone of an object, if this object is cloneable. The
	 * clone is created by calling <code>clone()</code> using Java reflection,
	 * therefore <code>clone()</code> not necessarily has to be public.
	 * 
	 * @param o   The object to be cloned.
	 * @param <T> Type of returned object.
	 * @return A clone of {@code o} if it was {@link Cloneable}, or otherwise the
	 *         original object.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T cloneIfPossible(T o) {
		if (o == null)
			return null;

		// array or normal class?
		Class<?> ct = o.getClass().getComponentType();
		if (ct == null) {
			// normal class
			o = callClone(o);
		} else if (ct.isPrimitive()) {
			// array of primitive type
			if (ct == Integer.TYPE) {
				o = (T) deepCloneArrayIfPossible((int[]) o);
			} else if (ct == Double.TYPE) {
				o = (T) deepCloneArrayIfPossible((double[]) o);
			} else if (ct == Long.TYPE) {
				o = (T) deepCloneArrayIfPossible((long[]) o);
			} else if (ct == Boolean.TYPE) {
				o = (T) deepCloneArrayIfPossible((boolean[]) o);
			} else if (ct == Character.TYPE) {
				o = (T) deepCloneArrayIfPossible((char[]) o);
			} else if (ct == Float.TYPE) {
				o = (T) deepCloneArrayIfPossible((float[]) o);
			} else if (ct == Byte.TYPE) {
				o = (T) deepCloneArrayIfPossible((byte[]) o);
			} else if (ct == Short.TYPE) {
				o = (T) deepCloneArrayIfPossible((short[]) o);
			} else {
				throw new AssertionError(); // can't occur
			}
		} else {
			// array containing Objects
			o = (T) deepCloneArrayIfPossible((Object[]) o);
		}

		return o;
	}

	@SuppressWarnings("unchecked")
	private static <T> T callClone(T o) {
		// o or an array's components are clonable
		try {
			Method cloneMethod = o.getClass().getMethod("clone", new Class[] {});
			return (T) cloneMethod.invoke(o);
		} catch (NoSuchMethodException ignore) {
			// not cloneable, or no public clone-method, return "o" as is
			return o;
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Produces a deep clone of {@code array}, i.e., for each element of
	 * {@code array} creating a clone is attempted using
	 * {@link #cloneIfPossible(Object)}.
	 * 
	 * @param array The array to be cloned.
	 * @param <T>   Component type of the array.
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
	 * @see #deepCloneArrayIfPossible(Object[])
	 */
	public static int[] deepCloneArrayIfPossible(int[] array) {
		return array.clone();
	}

	/**
	 * @see #deepCloneArrayIfPossible(Object[])
	 */
	public static long[] deepCloneArrayIfPossible(long[] array) {
		return array.clone();
	}

	/**
	 * @see #deepCloneArrayIfPossible(Object[])
	 */
	public static byte[] deepCloneArrayIfPossible(byte[] array) {
		return array.clone();
	}

	/**
	 * @see #deepCloneArrayIfPossible(Object[])
	 */
	public static short[] deepCloneArrayIfPossible(short[] array) {
		return array.clone();
	}

	/**
	 * @see #deepCloneArrayIfPossible(Object[])
	 */
	public static char[] deepCloneArrayIfPossible(char[] array) {
		return array.clone();
	}

	/**
	 * @see #deepCloneArrayIfPossible(Object[])
	 */
	public static boolean[] deepCloneArrayIfPossible(boolean[] array) {
		return array.clone();
	}

	/**
	 * @see #deepCloneArrayIfPossible(Object[])
	 */
	public static float[] deepCloneArrayIfPossible(float[] array) {
		return array.clone();
	}

	/**
	 * @see #deepCloneArrayIfPossible(Object[])
	 */
	public static double[] deepCloneArrayIfPossible(double[] array) {
		return array.clone();
	}

	private static final String PROP_JASIMA_EXPERIMENT = "jasima.experiment";
	private static final String PROP_SUN_JAVA_COMMAND = "sun.java.command";

	/**
	 * Tries to find the main class of a java run. This is attempted by looking up
	 * the system properties {@code jasima.experiment} and {@code sun.java.command}
	 * first. If this does not lead to a valid classname (e.g., if started with
	 * "-jar" option) an attempt is made to interpret the property as the name of a
	 * jar file. The manifest of this jar is then searched for its entry
	 * {@code Main-Class}.
	 * <p>
	 * This code is necessary because Java has no virtual static methods and
	 * therefore there is no equivalent to the keyword {@code this} in a static
	 * method.
	 * 
	 * @throws ClassNotFoundException If there were problems locating the main
	 *                                class. Should not occur.
	 */
	public static Class<?> getMainClass() throws ClassNotFoundException {
		Properties props = System.getProperties();

		String main = (String) findEntryCaseInsensitive(props, PROP_JASIMA_EXPERIMENT);
		if (main == null) {
			main = (String) findEntryCaseInsensitive(props, PROP_SUN_JAVA_COMMAND);
		}
		if (main == null) {
			throw new RuntimeException(
					defFormat("Couldn't find properties '%s' or '%s'.", PROP_SUN_JAVA_COMMAND, PROP_JASIMA_EXPERIMENT));
		}

		// strip any arguments, if present
		String classOrJar;
		try (Scanner s = new Scanner(main)) {
			classOrJar = s.next();
		}

		try {
			// try to find as class directly
			Class<?> klazz = TypeUtil.class.getClassLoader().loadClass(classOrJar);
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

	private static Class<?> loadFromJar(String classOrJar) throws IOException, ClassNotFoundException {
		try (JarFile jar = new JarFile(classOrJar)) {
			Manifest manifest = jar.getManifest();
			if (manifest == null) {
				throw new RuntimeException("MANIFEST.MF not found in jar.");
			}

			Attributes attributes = manifest.getMainAttributes();

			// is app using "jar in jar" export from eclipse? in this case
			// main-class is JarRsrcLoader
			String mainClass = attributes.getValue("Rsrc-Main-Class");
			if (mainClass == null) {
				// regular main class
				mainClass = attributes.getValue("Main-Class");
			}
			if (mainClass == null) {
				throw new RuntimeException("Couldn't determine main class.");
			}

			// try to load cName
			Class<?> klazz = TypeUtil.class.getClassLoader().loadClass(mainClass);
			return klazz;
		}
	}

	private static Object findEntryCaseInsensitive(Map<?, ?> jarEntries, String entry) {
		entry = entry.toLowerCase();
		for (Object o : jarEntries.keySet()) {
			String s = ((String) o).toLowerCase();
			if (entry.equals(s)) {
				return jarEntries.get(o);
			}
		}
		return null;
	}

	public static <E> Class<? extends E> getClassFromSystemProperty(String propertyName, Class<E> clazz,
			Class<? extends E> defaultClass) {
		ClassLoader cl = clazz.getClassLoader();
		Class<? extends E> result;

		String classname = System.getProperty(propertyName);
		if (classname == null) {
			result = defaultClass;
		} else {
			try {
				Class<?> class1 = Class.forName(classname, false, cl); // for security reasons it is important to use an
																		// uninitialized class here.
				result = class1.asSubclass(clazz);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}

		return result;
	}

	/**
	 * Creates an instance of the class passed as a parameter using its
	 * parameter-less constructor. Should any Exception be triggered creating the
	 * instance ({@code Class#newInstance()} can throw the checked Exceptions
	 * {@link InstantiationException} and an {@code IllegalAccessException}), then
	 * it is rethrown as an unchecked exception wrapped in a RuntimeException.
	 * 
	 * @param <T>   Type of the object to create
	 * @param clazz Class of the object to create (mustn't be null)
	 * @return The new instance
	 * @throws RuntimeException If any Exception occurs when creating the instance,
	 *                          it will be rethrown wrapped in a
	 *                          {@code RuntimeException}
	 */
	public static <T> T createInstance(Class<T> clazz) {
		try {
			return clazz.getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException | IllegalArgumentException | SecurityException e) {
			throw new RuntimeException("error instantiating class " + clazz, e);
		}
	}

	/**
	 * Throws a throwable in the current Thread (can be either a
	 * {@link RuntimeException} or an {@link Error}).
	 * 
	 * @param t The unchecked exception to throw.
	 */
	public static void rethrowUncheckedException(Throwable t) {
		if (t instanceof RuntimeException) {
			throw (RuntimeException) t;
		} else if (t instanceof Error) {
			throw (Error) t;
		} else {
			// can't occur
			throw new AssertionError();
		}
	}

	/**
	 * Loads a class of the given name using the context class loader.
	 * 
	 * @param <T>                 Required type of the class.
	 * 
	 * @param className           Name of the class.
	 * @param requiredParentClass Type token for the class or interface that has to
	 *                            be implemented.
	 * @return The loaded class.
	 * 
	 * @throws ClassNotFoundException If no class of the given name could be found.
	 * @throws ClassCastException     If a matching class was found, but was not
	 *                                compatible with {@code requiredParentClass}.
	 */
	public static <T> Class<? extends T> loadClass(String className, Class<T> requiredParentClass)
			throws ClassNotFoundException, ClassCastException {
		Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
		return clazz.asSubclass(requiredParentClass);
	}

	/**
	 * Prevent instantiation
	 */
	private TypeUtil() {
	}
}
