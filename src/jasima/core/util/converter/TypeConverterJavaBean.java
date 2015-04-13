package jasima.core.util.converter;

import jasima.core.util.ArgListTokenizer;
import jasima.core.util.ArgListTokenizer.TokenType;
import jasima.core.util.TypeUtil;
import jasima.core.util.TypeUtil.TypeConversionException;
import jasima.core.util.Util;
import jasima.core.util.XmlUtil;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;

import com.thoughtworks.xstream.XStreamException;

public class TypeConverterJavaBean extends TypeToStringConverter {

	/**
	 * Special String value that is recognized as {@code null} in
	 * {@link #setPropertyEx(Object, String, Object, ClassLoader, String[])}.
	 */
	public static final String NULL = "@null";

	/**
	 * Internal class used to indicate problems reading a file.
	 */
	private static class FileReadException extends Exception {
		private static final long serialVersionUID = -1763832991537196846L;

		private final String fileName;

		public FileReadException(String fileName, Throwable cause) {
			super(null, cause);
			this.fileName = fileName;
		}

		public String getFileName() {
			return fileName;
		}
	}

	/**
	 * Internal class to indicate problems with class loading.
	 */
	private static class NoTypeFoundException extends Exception {

		private static final long serialVersionUID = -7271169253051939902L;

		public NoTypeFoundException(String msg) {
			super(msg);
		}

	}

	@Override
	public Class<?>[] handledTypes() {
		return new Class<?>[] { Object.class };
	}

	@Override
	public <T> T fromString(ArgListTokenizer tk, Class<T> requiredType,
			String context, ClassLoader loader, String[] packageSearchPath)
			throws TypeConversionException {
		// read required class name
		tk.assureTokenTypes(tk.nextTokenNoWhitespace(), TokenType.STRING);
		String className = tk.currTokenText();

		// try to create/load main type
		T root;
		try {
			root = loadClassOrXml(className, requiredType, loader,
					packageSearchPath);
		} catch (ReflectiveOperationException | FileReadException
				| NoTypeFoundException e) {
			// this can only happen for the top level object, otherwise it
			// is already caught and wrapped in a TypeConversionException
			throw new TypeConversionException(
					String.format(
							Util.DEF_LOCALE,
							"Can't create object for value '%s' (property path: '%s'): %s",
							className, context, exceptionMessage(e)), e);
		}

		// read and set parameters of complex objects (optional parameter
		// list in round parenthesis)
		if (tk.nextTokenNoWhitespace() == TokenType.PARENS_OPEN) {
			Map<String, PropertyDescriptor> beanProps = TypeUtil
					.writableProperties(root.getClass());
			while (true) {
				// name
				TokenType token = tk.nextTokenNoWhitespace();
				tk.assureTokenTypes(token, TokenType.STRING,
						TokenType.PARENS_CLOSE);
				if (token == TokenType.PARENS_CLOSE)
					break; // end of parameter list
				String propName = tk.currTokenText();

				// equals
				tk.assureTokenTypes(tk.nextTokenNoWhitespace(),
						TokenType.EQUALS);

				// find property type
				PropertyDescriptor prop = beanProps.get(propName
						.toLowerCase(Util.DEF_LOCALE));
				if (prop == null)
					throw new RuntimeException(
							String.format(
									Util.DEF_LOCALE,
									"Can't find property '%s' in type '%s', property path: '%s'",
									propName, root.getClass().getName(),
									context));

				// read and set value
				try {
					String propPath;
					if (context != null && context.length() > 0)
						propPath = context + "." + propName;
					else
						propPath = propName;

					// recursively create object for property value
					Object value = convertFromString(tk,
							prop.getPropertyType(), propPath, loader,
							packageSearchPath);

					// call setter to finally check compatibility
					prop.getWriteMethod().invoke(root, value);
				} catch (ReflectiveOperationException | TypeConversionException e1) {
					throw new TypeConversionException(
							String.format(
									Util.DEF_LOCALE,
									"Can't set property '%s' in type %s (property path: '%s'): %s",
									propName, root.getClass().getName(),
									context, exceptionMessage(e1)), e1);
				}

				// more parameters?
				token = tk.nextTokenNoWhitespace();
				tk.assureTokenTypes(token, TokenType.SEMICOLON,
						TokenType.PARENS_CLOSE);
				if (token == TokenType.SEMICOLON) {
					// nothing special, start next iteration
				} else if (token == TokenType.PARENS_CLOSE) {
					break; // found end of list
				}
			}
		} else {
			// let parent handle it
			tk.pushBackToken();
		}

		return root;
	}

	/**
	 * Converts an object given as a String to the original type. This tries to
	 * convert primitive types (like numbers) first. If this is not successful,
	 * {@code asString} is interpreted as a class name and loading the
	 * appropriate class is attempted (potentially prefixed with entries in
	 * {@code packageSearchPath}). If conversion is still not successful, then
	 * loading an xml file with the name {@code asString} is attempted.
	 * 
	 * @param asString
	 *            String representation of an object.
	 * @param requiredType
	 *            The desired target type.
	 * @param l
	 *            The class loader to use.
	 * @param packageSearchPath
	 *            A package search path.
	 * @param <T>
	 *            Type of returned object.
	 * @return The object converted/compatible with {@code requiredType}.
	 */
	private static <T> T loadClassOrXml(String asString, Class<T> requiredType,
			ClassLoader l, String[] packageSearchPath)
			throws ReflectiveOperationException, FileReadException,
			NoTypeFoundException {
		if (NULL.equalsIgnoreCase(asString))
			return null;

		try {
			// try to load from class (interpret 'asString' as class name)
			T o = requiredType.cast(searchAndInstanciateClass(asString, l,
					packageSearchPath));
			if (o != null) {
				return o;
			}

			// try to load from file
			o = requiredType.cast(loadXmlFile(asString));
			if (o != null) {
				return o;
			}
		} catch (ClassCastException ignore) {

		}

		// give up
		throw new NoTypeFoundException(String.format(Util.DEF_LOCALE,
				"Can't load/convert '%s', required type: %s", asString,
				requiredType));
	}

	/**
	 * Load an instantiate a class using classloader {@code l}. If a class
	 * {@code className} is not found, it is searched for in the package search
	 * path.
	 * <p>
	 * If, e.g., {@code className} is {@code "MultipleReplicationExperiment"}
	 * and the package search path {@code searchPath} contains an entry
	 * {@code "jasima.core.experiment"} , then the class
	 * {@code jasima.core.experiment.MultipleReplicationExperiment} will be
	 * looked up and instantiated.
	 * </p>
	 * <p>
	 * If no matching class could be found, {@code null} will be returned.
	 */
	private static Object searchAndInstanciateClass(String className,
			ClassLoader l, String[] searchPath)
			throws ReflectiveOperationException, IllegalAccessException {
		Class<?> klazz = null;

		// try direct match
		try {
			klazz = l.loadClass(className);
		} catch (ClassNotFoundException ignore) {
		}

		// try matches from the class search path
		if (klazz == null) {
			for (String packageName : searchPath) {
				try {
					klazz = l.loadClass(packageName + "." + className);
					break; // for; we found a matching class
				} catch (ClassNotFoundException ignore) {
				}
			}
		}

		if (klazz != null) {
			if (Modifier.isAbstract(klazz.getModifiers()))
				throw new InstantiationException(
						"Can't instantiate an abstract class.");
			return klazz.getConstructor().newInstance();
		} else {
			return null;
		}
	}

	/**
	 * Attempts to load an object from a xml-file {@code fileName}. If such a
	 * file does not exist or is not readable, {@code null} will be returned.
	 * 
	 * @see XmlUtil#loadXML(File)
	 */
	private static Object loadXmlFile(String fileName) throws FileReadException {
		File f = new File(fileName);
		if (!f.canRead() || f.isDirectory())
			return null;

		try {
			return XmlUtil.loadXML(f);
		} catch (XStreamException x) {
			throw new FileReadException(fileName, x);
		}
	}

	@Override
	public String toString(Object o) {
		if (o == null)
			return NULL;

		Map<String, PropertyDescriptor> props = TypeUtil.writableProperties(o
				.getClass());
		if (!props.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append(o.getClass().getName());
			sb.append('(');

			for (PropertyDescriptor p : props.values()) {
				String name = p.getName();
				Object value = TypeUtil.getPropertyValue(o, name);
				sb.append(name).append('=').append(convertToString(value));
				sb.append(';');
			}

			if (props.size() != 0) {
				sb.setCharAt(sb.length() - 1, ')');
			} else {
				sb.append(')');
			}
			return sb.toString();
		} else {
			// for objects without any bean properties, simply use "toString"
			return o.toString();
		}
	}

	public static String exceptionMessage(Throwable t) {
		String msg = t.getMessage();
		if (t instanceof InvocationTargetException) {
			msg = String.format(Util.DEF_LOCALE,
					"Error invoking method or constructor: %s", t.getCause()
							.toString());
		} else if (t instanceof NoSuchMethodException) {
			msg = String.format(Util.DEF_LOCALE,
					"Method or constructor not found: %s", t.getMessage());
		} else if (t instanceof FileReadException) {
			FileReadException e = (FileReadException) t;
			msg = String.format(Util.DEF_LOCALE, "Error reading file '%s': %s",
					e.getFileName(), e.getCause().getMessage());
		}
		return msg;
	}

}