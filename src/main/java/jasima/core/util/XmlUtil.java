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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.converters.collections.MapConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;
import com.thoughtworks.xstream.security.NoTypePermission;

import jasima.core.experiment.Experiment.UniqueNamesCheckingHashMap;

/**
 * Provides utility methods to read and write arbitrary Java objects as xml
 * (xml-Serialization using the xstream library).
 * 
 * @author Torsten Hildebrandt
 */
public class XmlUtil {

	private static final String JASIMA_BEAN_PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<?jasima bean?>\n";
	private static final String XML_PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

	/**
	 * Loads an object from a String containing xml.
	 * 
	 * @param xmlString A String containing xml data.
	 * @see #saveXML(FileFormat,Object)
	 * 
	 * @return The object contained in {@code xmlString}.
	 */
	public static Object loadXML(FileFormat format, String xmlString) {
		XStream xstream = getXStream(format);
		Object o = xstream.fromXML(xmlString);
		return o;
	}

	/**
	 * Loads an object from a String containing xml. Format defaults to
	 * {@code FileFormat.XSTREAM}.
	 * 
	 * @param xmlString A String containing xml data.
	 * @see #loadXML(FileFormat,String)
	 * @see #saveXML(Object)
	 * 
	 * @return The object contained in {@code xmlString}.
	 */
	public static Object loadXML(String xmlString) {
		return loadXML(FileFormat.XSTREAM, xmlString);
	}

	/**
	 * Loads an object from a file.
	 * 
	 * @param f The file to load.
	 * @return The object contained in {@code f}.
	 */
	public static Object loadXML(FileFormat format, File f) {
		XStream xstream = getXStream(format);
		Object o = xstream.fromXML(f);
		return o;
	}

	/**
	 * Loads an object from a file. Format is assumed to be the XML bean format
	 * produced by jasima gui.
	 * 
	 * @param f The file to load.
	 * @return The object contained in {@code f}.
	 */
	public static Object loadXML(File f) {
		return loadXML(FileFormat.JASIMA_BEAN, f);
	}

	/**
	 * Loads an object from a {@link Reader}.
	 * 
	 * @param r Source of the xml.
	 * @return The object contained in {@code r}.
	 */
	public static Object loadXML(FileFormat format, Reader r) {
		XStream xstream = getXStream(format);
		Object o = xstream.fromXML(r);
		return o;
	}

	/**
	 * Loads an object from a {@link Reader}. Format is assumed to be the XML bean
	 * format produced by jasima gui.
	 * 
	 * @param r Source of the xml.
	 * @return The object contained in {@code r}.
	 */
	public static Object loadXML(Reader r) {
		return loadXML(FileFormat.JASIMA_BEAN, r);
	}

	/**
	 * Converts an object into a xml String.
	 * 
	 * @param o The object to convert.
	 * @return The object serialized to xml.
	 */
	public static String saveXML(FileFormat format, Object o) {
		XStream xstream = getXStream(format);
		return xstream.toXML(o);
	}

	/**
	 * Converts an object into xml and writes the result in {@code w}.
	 * 
	 * @param o The object to convert.
	 * @param w The output writer.
	 */
	public static void saveXML(FileFormat format, Object o, Writer w) {
		XStream xstream = getXStream(format);
		xstream.toXML(o, w);
	}

	/**
	 * Converts an object into xml and saves the result in a file {@code f}.
	 * 
	 * @param o The object to convert.
	 * @param f The output file. This file is overwritten if it already exists.
	 */
	public static void saveXML(FileFormat format, Object o, File f) {
		XStream xstream = getXStream(format);
		try (BufferedWriter fw = new BufferedWriter(new FileWriter(f))) {
			xstream.toXML(o, fw);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Saves the given object in the file {@code f}. This is the same as calling
	 * {@link #saveXML(FileFormat, Object, File)} with {@code FileFormat.XSTREAM} as
	 * the file format.
	 */
	public static void saveXML(Object o, File f) {
		saveXML(FileFormat.XSTREAM, o, f);
	}

	/**
	 * Saves the given object in the writer {@code w}. This is the same as calling
	 * {@link #saveXML(FileFormat, Object, Writer)} with {@code FileFormat.XSTREAM}
	 * as the file format.
	 */
	public static void saveXML(Object o, Writer w) {
		saveXML(FileFormat.XSTREAM, o, w);
	}

	/**
	 * Converts the given object to a String in xml format. This is the same as
	 * calling {@link #saveXML(FileFormat, Object)} with {@code FileFormat.XSTREAM}
	 * as the file format.
	 */
	public static String saveXML(Object o) {
		return saveXML(FileFormat.XSTREAM, o);
	}

	private static XStream getXStream(final FileFormat format) {
		XStream xstream;
		if (format == FileFormat.JSON) {
			xstream = new XStream(new JettisonMappedXmlDriver());
			xstream.registerConverter(new MapConverter(xstream.getMapper()));
		} else {
			xstream = new XStream(new DomDriver() {
				@Override
				public HierarchicalStreamWriter createWriter(Writer out) {
					try {
						switch (format) {
						case JASIMA_BEAN:
							out.append(JASIMA_BEAN_PREFIX);
							break;
						case XSTREAM:
							out.append(XML_PREFIX);
							break;
						default:
							// do nothing
						}
						return super.createWriter(out);
					} catch (IOException e) {
						throw new XStreamException(e);
					}
				}
			});
		}
		xstream.registerConverter(new MapConverter(xstream.getMapper()) {
			@SuppressWarnings("rawtypes")
			@Override
			public boolean canConvert(Class type) {
				if (type.equals(UniqueNamesCheckingHashMap.class))
					return true;
				else
					return super.canConvert(type);
			}
		});

		if (format == FileFormat.RESULTS_MAP) {
			xstream.setMode(XStream.NO_REFERENCES);
		}

		if (format == FileFormat.JASIMA_BEAN) {
			xstream.registerConverter(new JasimaBeanConverter(xstream.getMapper(), true), -10);
		} else if (format == FileFormat.JSON) {
			xstream.registerConverter(new JasimaBeanConverter(xstream.getMapper(), false), -10);
		}

		// clear out existing permissions and set own ones; this prevent the warning
		// message but is not really secure!
		// TODO: revise when xstream 1.5 is out
		xstream.addPermission(NoTypePermission.NONE);
		xstream.addPermission(AnyTypePermission.ANY);
		xstream.denyTypeHierarchy(ProcessBuilder.class);

		return xstream;
	}

	/**
	 * Loads an object from a String containing JSON data suitable for XSTREAM.
	 * 
	 * @param jsonString A String containing JSON data.
	 * @see #loadXML(FileFormat,String)
	 * 
	 * @return The object contained in {@code jsonString}.
	 */
	public static Object loadJSON(String jsonString) {
		return loadXML(FileFormat.JSON, jsonString);
	}

	/**
	 * Converts the given object to a String in JSON format. This is the same as
	 * calling {@link #saveXML(FileFormat, Object)} with {@code FileFormat.} as the
	 * file format.
	 */
	public static String saveJSON(Object o) {
		return saveXML(FileFormat.JSON, o);
	}

}
