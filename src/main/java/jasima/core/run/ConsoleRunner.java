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
package jasima.core.run;

import static jasima.core.util.i18n.I18n.defFormat;
import static java.lang.System.lineSeparator;
import static java.util.Arrays.asList;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import jasima.core.experiment.Experiment;
import jasima.core.experiment.ExperimentListener;
import jasima.core.simulation.SimulationExperiment;
import jasima.core.util.ConsolePrinter;
import jasima.core.util.ExcelSaver;
import jasima.core.util.MsgCategory;
import jasima.core.util.Pair;
import jasima.core.util.StringUtil;
import jasima.core.util.TraceFileProducer;
import jasima.core.util.TypeUtil;
import jasima.core.util.Util;
import jasima.core.util.XmlSaver;
import jasima.core.util.i18n.I18n;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.util.KeyValuePair;

/**
 * Main class to load and run an experiment from the command line. The
 * experiment can be specified by either the class name (in this case a new
 * experiment of this type will be created) or by specifying the name of an xml
 * file or Excel file containing an experiment (e.g., created with the gui).
 * <p>
 * Furthermore this class is used indirectly by Experiments to configure and run
 * them.
 * 
 * @author Torsten Hildebrandt
 * 
 * @see Experiment#main(String[])
 */
public class ConsoleRunner extends AbstractExperimentRunner {

	private static final String DOTS = StringUtil.repeat("*", 80);

	private String expSpec;
	private Experiment expTemplate;
	private PropertyDescriptor[] beanProps;
	private boolean hideResults = false;

	public ConsoleRunner() {
		this(null);
	}

	public ConsoleRunner(@Nullable Experiment expTemplate) {
		super();
		this.expTemplate = expTemplate;
		if (expTemplate != null) {
			this.experimentFileName = expTemplate.getClass().getSimpleName();
		}
	}

	private void handleGenericOptions(OptionSet opts) {
		if (opts.has("help")) {
			printUsageAndExit();
		}

		if (opts.has("log")) {
			String vs = (String) opts.valueOf("log");

			MsgCategory cat = Enum.valueOf(MsgCategory.class, vs.toUpperCase(Locale.US));
			if (cat == MsgCategory.OFF) {
				listeners.remove(ConsolePrinter.class);
			} else {
				listeners.put(ConsolePrinter.class, new ConsolePrinter(cat));
			}
		}

		if (opts.has("trace")) {
			String traceFileName = (String) opts.valueOf("trace");
			TraceFileProducer t;

			if (traceFileName == null) {
				t = new TraceFileProducer();
			} else {
				t = new TraceFileProducer(traceFileName);
			}

			listeners.put(TraceFileProducer.class, new ExperimentListener() {
				@Override
				public void beforeRun(Experiment e) {
					ExperimentListener.super.beforeRun(e);

					SimulationExperiment se = (SimulationExperiment) e;
					se.getSim().getRootComponent().addListener(t);
				}
			});
		}

		if (opts.has("nores")) {
			hideResults = true;
		}

		if (opts.has("xmlres")) {
			String xmlFileName = (String) opts.valueOf("xmlres");
			if (xmlFileName == null) {
				listeners.put(XmlSaver.class, new XmlSaver());
			} else {
				XmlSaver xs = new XmlSaver();
				xs.setResultFileName(xmlFileName);
				listeners.put(XmlSaver.class, xs);
			}
		}

		if (opts.has("xlsres")) {
			String xlsFileName = (String) opts.valueOf("xlsres");
			if (xlsFileName == null) {
				listeners.put(ExcelSaver.class, new ExcelSaver());
			} else {
				ExcelSaver es = new ExcelSaver();
				es.setResultFileName(xlsFileName);
				listeners.put(ExcelSaver.class, es);
			}
		}

		for (Object o : opts.valuesOf("D")) {
			String s = (String) o;
			KeyValuePair v = KeyValuePair.valueOf(s);
			manualProps.add(new Pair<String, Object>(v.key, v.value));
		}

		for (Object o : opts.valuesOf("p")) {
			packageSearchPath = Util.addToArray(packageSearchPath, String.class, (String) o);
		}
	}

	private void handleExperimentOptions(OptionSet opts) {
		for (PropertyDescriptor prop : beanProps) {
			String name = prop.getName();
			if (opts.has(name)) {
				String value = (String) opts.valueOf(name);
				manualProps.add(new Pair<String, Object>(name, value));
			}
		}
	}

	private void createGenericOptions(OptionParser p) {
		p.acceptsAll(asList("h", "?", "help"), "Display this help text.").forHelp();

		MsgCategory[] values = MsgCategory.values();
		String logLevels = Arrays.toString(values).replaceAll("[\\[\\]]", "");
		p.acceptsAll(asList("l", "log"), defFormat("Set log level to one of %s. Default: INFO.", logLevels))
				.withRequiredArg().describedAs("level");

		p.accepts("trace", "Produce a detailed event trace (only works for SimulationExperiments).").withOptionalArg()
				.describedAs("filename");

		p.accepts("xmlres", "Save results in XML format.").withOptionalArg().describedAs("filename");
		p.accepts("xlsres", "Save results in Excel format.").withOptionalArg().describedAs("filename");

		p.accepts("nores", "Does not print results to console.");

		p.accepts("D", "Sets a property to a certain value.").withRequiredArg().describedAs("property=value");

		p.accepts("p", "Add an entry to the package search path.").withRequiredArg().describedAs("packageName");
	}

	private void createAdditionalOptions(OptionParser p) {
		Objects.requireNonNull(expToRun);

		beanProps = TypeUtil.findWritableProperties(expToRun);

		// create a command line option for each top level property
		for (PropertyDescriptor prop : beanProps) {
			Class<?> type = prop.getPropertyType();

			String description = "";

			if (type.isEnum()) {
				String enumValues = Arrays.toString(type.getEnumConstants()).replaceAll("[\\[\\]]", "");
				description = defFormat("enum with values: %s", enumValues);
			} else if (type.isArray()) {
				Class<?> elemType = type.getComponentType();
				description = defFormat("array of '%s'", elemType.getSimpleName());
			} else if (!(type.isPrimitive() || type == String.class)) {
				description = defFormat("property of type '%s'", type.getSimpleName());
			}

			if (!"".equals(description)) {
				description += "; ";
			}
			description += "defined in class " + prop.getReadMethod().getDeclaringClass().getSimpleName();

			p.accepts(prop.getName(), description).withRequiredArg().describedAs(type.getSimpleName().toUpperCase());
		}
	}

	private @Nullable Experiment createExperiment() {
		if (expTemplate == null && expSpec == null) {
			return null;
		}

		Experiment e;
		if (expTemplate != null) {
			e = expTemplate;
		} else if (expSpec.toLowerCase(I18n.DEF_LOCALE).endsWith(".xls")) {
			// is it an Excel experiment?
			experimentFileName = expSpec;
			e = new ExcelExperimentReader(new File(experimentFileName), getClass().getClassLoader(), packageSearchPath)
					.createExperiment();
		} else {
			// normal Experiment (class name) or loaded from xml file
			e = TypeUtil.convert(expSpec, Experiment.class, "", getClass().getClassLoader(), packageSearchPath);
		}

		return e;
	}

	public Map<String, Object> runWith(String... args) {
		if (expTemplate == null && args.length > 0) {
			if (!args[0].startsWith("-")) {
				expSpec = args[0];
				args = Arrays.copyOfRange(args, 1, args.length);
			}
		}

		try {
			expToRun = null;
			try {
				expToRun = createExperiment();
			} finally {
				printBanner(expToRun); // print banner even when there are exceptions
			}

			parseArgs(args);

			if (expToRun == null) {
				if (expTemplate == null && expSpec == null) {
					printErrorThenExit(1, "No valid experiment file name/class name given.");
				} else {
					printErrorThenExit(1, "Couldn't load experiment using: '%s'", expSpec);
				}
				throw new AssertionError(); // never reached
			}

			configureExperiment(); // configure using whatever we read from args
		} catch (Exception e) {
			printErrorThenExit(1, "Problem configuring experiment: %s (%s)", e.getLocalizedMessage(),
					Util.exceptionToString(e));
			throw new AssertionError(); // never reached
		}

		try {
			Map<String, Object> res = run();

			if (!hideResults) {
				ConsolePrinter.printResults(expToRun, res);
			}

			return res;
		} catch (Exception e) {
			printErrorThenExit(2, "Unhandled exception during run: %s (%s)", e.getLocalizedMessage(),
					Util.exceptionToString(e));
			throw new AssertionError(); // never reached
		}
	}

	private void parseArgs(String[] args) {
		// create parser and parse command line
		OptionParser optsParser = new OptionParser();
		optsParser.allowsUnrecognizedOptions();
		createGenericOptions(optsParser);
		if (expToRun != null) {
			createAdditionalOptions(optsParser);
		}
		OptionSet opts = optsParser.parse(args);

		handleGenericOptions(opts);
		if (expToRun != null) {
			handleExperimentOptions(opts);
		}

		List<?> remainingArgs = new ArrayList<>(opts.nonOptionArguments());
		if (remainingArgs.size() > 0) {
			throw new RuntimeException(
					defFormat("unrecognized command line parameters: %s", Arrays.toString(remainingArgs.toArray())));
		}
	}

	void exit(int errorCode) {
		System.exit(errorCode);
	}

	private void printErrorThenExit(int errorCode, String format, Object... args) {
		System.err.printf(format, args);
		System.err.println();
		System.err.println();
		System.err.println("Run with parameter --help for valid command line parameters.");

		if (errorCode != 0) {
			exit(errorCode);
		}
	}

	public static void printBanner(Experiment exp) {
		PrintWriter pw = new PrintWriter(System.out, true);
		try {
			printBanner(pw, exp);
		} finally {
			pw.flush();
		}
	}

	public static void printBanner(PrintWriter out, Experiment exp) {
		out.println(DOTS);
		out.println(Util.getIdString());
		out.println();
		if (exp != null) {
			out.println(exp.getClass().getSimpleName() + ": " + exp.toString());
			out.println();
		}
		out.println(Util.getJavaEnvString());
		out.println(Util.getOsEnvString());
		out.println(Util.getWorkingDirString());
		out.println(DOTS);
		out.println();
	}

	private void printUsageAndExit() {
		try {
			System.err.println(getHelpCmdLineText());
			System.err.println();
			System.err.println(Util.getIdString());

			// print additional options if any
			if (expToRun != null) {
				OptionParser op2 = new OptionParser();
				createAdditionalOptions(op2);
				System.err.println();
				System.err.println("Experiment properties (top-level):");
				System.err.println("==================================");
				op2.printHelpOn(System.err);
			} else {
				System.err.println();
				System.err.println("(no experiment options available)");
			}

			// print generic options
			System.err.println();
			System.err.println("Generic options:");
			System.err.println("================");
			OptionParser op1 = new OptionParser();
			createGenericOptions(op1);
			op1.printHelpOn(System.err);

			System.err.println(getHelpFooterText());
		} catch (IOException ignore) {
		} finally {
			exit(0);
		}
	}

	private String getHelpCmdLineText() {
		if (expTemplate != null) {
			return "usage: " + expTemplate.getClass().getName() + " [options]";
		} else {
			return "usage: " + getClass().getName() + " <expSpec> [options]";
		}
	}

	private String getHelpFooterText() {
		String res = lineSeparator() + "All parameter names are CASE-SENSITIVE. For detailed information see "
				+ lineSeparator() + "http://jasima.org.";
		if (expTemplate == null) {
			res = "<expSpec>                    Class name of an Experiment or file name " + lineSeparator()
					+ "                             of an XML-serialized Experiment or" + lineSeparator()
					+ "                             name of an xls file." + lineSeparator() //
					+ res;
		}

		return res;
	}

	// static members below

	public static Map<String, Object> run(@Nullable Experiment template, String... args) {
		ConsoleRunner cr = new ConsoleRunner(template);
		return cr.runWith(args);
	}

	public static Map<String, Object> run(String... args) {
		return run(null, args);
	}

	public static void main(String... args) {
		run(args);
	}

}
