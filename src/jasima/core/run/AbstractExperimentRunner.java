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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

import jasima.core.expExecution.ExperimentCompletableFuture;
import jasima.core.expExecution.ExperimentExecutor;
import jasima.core.experiment.Experiment;
import jasima.core.experiment.ExperimentListener;
import jasima.core.simulation.SimulationExperiment;
import jasima.core.util.AbstractResultSaver;
import jasima.core.util.ConsolePrinter;
import jasima.core.util.ExcelSaver;
import jasima.core.util.MsgCategory;
import jasima.core.util.Pair;
import jasima.core.util.StringUtil;
import jasima.core.util.TraceFileProducer;
import jasima.core.util.TypeUtil;
import jasima.core.util.Util;
import jasima.core.util.XmlSaver;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.util.KeyValuePair;

/**
 * Base class for experiment runner utility classes.
 * 
 * @see ConsoleRunner
 * @see ExcelExperimentRunner
 * 
 * @author Robin Kreis
 * @author Torsten Hildebrandt
 */
public abstract class AbstractExperimentRunner {

	private static final String DOTS = StringUtil.repeat("*", 80);

	protected Map<Object, ExperimentListener> listeners;
	protected boolean hideResults = false;
	protected String experimentFileName = null;
	protected String[] packageSearchPath = Util.DEF_CLASS_SEARCH_PATH;
	protected ArrayList<Pair<String, Object>> manualProps;

	public AbstractExperimentRunner() {
		super();
		listeners = new HashMap<>();
		listeners.put(ConsolePrinter.class, new ConsolePrinter(MsgCategory.INFO));
		manualProps = new ArrayList<>();
	}

	protected void createGenericOptions(OptionParser p) {
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

	protected boolean createAdditionalOptions(OptionParser p) {
		return false;
	}

	public AbstractExperimentRunner parseArgs(String[] args) {
		try {
			doParseArgs(args);
		} catch (RuntimeException e1) {
			printUsage(String.valueOf(e1.getLocalizedMessage()));
		}

		return this;
	}

	protected void doParseArgs(String[] args) {
		// create parser and parse command line
		OptionParser optsParser = new OptionParser();
		createGenericOptions(optsParser);
		createAdditionalOptions(optsParser);

		OptionSet opts = optsParser.parse(args);

		processOptions(opts);

		List<?> argList = new ArrayList<>(opts.nonOptionArguments());
		handleRemainingArgs(argList);
		if (argList.size() > 0) {
			throw new RuntimeException(
					defFormat("unrecognized command line parameters: %s", Arrays.toString(argList.toArray())));
		}
	}

	protected void handleRemainingArgs(List<?> argList) {
	}

	protected void processOptions(OptionSet opts) {
		if (opts.has("help"))
			printUsage();

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
					se.sim().getRootComponent().addListener(t);
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

	protected static void printError(int exitCode, String format, Object... args) {
		System.err.printf(format, args);
	}

	protected void printUsage(String format, Object... args) {
		System.err.printf(format, args);
		System.err.println();
		System.err.println();
		printUsage();
	}

	protected void printUsage() {
		try {
			System.err.println(getHelpCmdLineText());
			System.err.println();
			System.err.println(Util.ID_STRING);

			// print additional options if any
			OptionParser op2 = new OptionParser();
			if (createAdditionalOptions(op2)) {
				System.err.println();
				System.err.println("Experiment properties (top-level):");
				System.err.println("==================================");
				op2.printHelpOn(System.err);
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
		}
	}

	protected String getHelpFooterText() {
		return lineSeparator() + "All parameter names are CASE-SENSITIVE. For detailed information see "
				+ lineSeparator() + "http://jasima.org.";
	}

	protected String getHelpCmdLineText() {
		return getClass().getName();
	}

	protected abstract Experiment createExperiment();

	protected String getResultFileNameHint() {
		return experimentFileName;
	}

	public Map<String, Object> run() {
		Experiment exp = null;
		try {
			exp = configureExperiment();
		} finally {
			System.out.println(DOTS);
			System.out.println(Util.ID_STRING);
			System.out.println();
			if (exp != null) {
				System.out.println(exp.getClass().getSimpleName() + ": " + exp.toString());
				System.out.println();
			}
			System.out.println(Util.getJavaEnvString());
			System.out.println(Util.getOsEnvString());
			System.out.println(Util.getWorkingDirString());
			System.out.println(DOTS);
			System.out.println();
		}

		try {
			return doRun(exp);
		} catch (Throwable t) {
			printError(10, "%s: %s (%s)", getResultFileNameHint(), t.getLocalizedMessage(), Util.exceptionToString(t));
			return exp.getResults();
		}
	}

	protected @Nullable Experiment configureExperiment() {
		// create and configure experiment
		Experiment exp = createExperiment();
		if (exp == null) {
			return null;
		}

		String resultFileNameHint = getResultFileNameHint();

		for (ExperimentListener lstnr : listeners.values()) {
			if (resultFileNameHint != null && lstnr instanceof AbstractResultSaver) {
				((AbstractResultSaver) lstnr).setFileNameHint(resultFileNameHint);
			}

			exp.addListener(lstnr);
		}

		exp = setPropsFromCmdLine(exp);
		return exp;
	}

	private @Nullable Map<String, Object> doRun(Experiment exp) {
		if (exp == null) {
			return null;
		}

		ExperimentCompletableFuture ef = ExperimentExecutor.runExperimentAsync(exp, null);
		Map<String, Object> res = ef.joinIgnoreExceptions();

		String msg = (String) res.get(Experiment.EXCEPTION_MESSAGE);
		String exc = (String) res.get(Experiment.EXCEPTION);
		if (msg != null || exc != null) {
			throw new RuntimeException(msg + "; detailed error: " + exc);
		}

		if (!hideResults) {
			ConsolePrinter.printResults(exp, res);
		}

		return res;
	}

	protected Experiment setPropsFromCmdLine(Experiment exp) {
		// sort props by number of segments so we set parent properties first
		Collections.sort(manualProps, Comparator.comparingInt(p -> numSegments(p.a)));

		// try to set each property
		for (Pair<String, Object> p : manualProps) {
			String name = p.a;
			Object value = p.b;

			TypeUtil.setPropertyValue(exp, name, value, getClass().getClassLoader(), packageSearchPath);
		}

		return exp;
	}

	/**
	 * Counts the number of dots '.' in a String.
	 */
	private static int numSegments(String a) {
		if (a == null || a.length() == 0)
			return 0;

		int res = 1;
		int from = -1;
		while ((from = a.indexOf('.', from + 1)) >= 0) {
			res++;
		}

		return res;
	}

}