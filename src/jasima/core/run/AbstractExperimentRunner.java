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
package jasima.core.run;

import static java.lang.System.lineSeparator;
import static java.util.Arrays.asList;
import jasima.core.expExecution.ExperimentExecutor;
import jasima.core.expExecution.ExperimentFuture;
import jasima.core.experiment.Experiment;
import jasima.core.experiment.Experiment.ExpMsgCategory;
import jasima.core.experiment.Experiment.ExperimentEvent;
import jasima.core.util.AbstractResultSaver;
import jasima.core.util.ConsolePrinter;
import jasima.core.util.ExcelSaver;
import jasima.core.util.Pair;
import jasima.core.util.TypeUtil;
import jasima.core.util.Util;
import jasima.core.util.XmlSaver;
import jasima.core.util.observer.NotifierListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
 * @version 
 *          "$Id$"
 */
public abstract class AbstractExperimentRunner {

	protected Map<Object, NotifierListener<Experiment, ExperimentEvent>> listeners;
	protected boolean hideResults = false;
	protected String experimentFileName = null;
	protected String[] packageSearchPath = Util.DEF_CLASS_SEARCH_PATH;
	protected ArrayList<Pair<String, Object>> manualProps;

	public AbstractExperimentRunner() {
		super();
		listeners = new HashMap<Object, NotifierListener<Experiment, ExperimentEvent>>();
		listeners.put(ConsolePrinter.class, new ConsolePrinter(
				ExpMsgCategory.INFO));
		manualProps = new ArrayList<>();
	}

	protected void createGenericOptions(OptionParser p) {
		p.acceptsAll(asList("h", "?", "help"), "Display this help text.")
				.forHelp();

		ExpMsgCategory[] values = ExpMsgCategory.values();
		String logLevels = Arrays.toString(values).replaceAll("[\\[\\]]", "");
		p.acceptsAll(
				asList("l", "log"),
				String.format(Util.DEF_LOCALE,
						"Set log level to one of %s. Default: INFO.", logLevels))
				.withRequiredArg().describedAs("level");

		p.accepts("xmlres", "Save results in XML format.").withOptionalArg()
				.describedAs("filename");
		p.accepts("xlsres", "Save results in Excel format.").withOptionalArg()
				.describedAs("filename");

		p.accepts("nores", "Does not print results to console.");

		p.accepts("D", "Sets a property to a certain value.").withRequiredArg()
				.describedAs("property=value");

		p.accepts("p", "Add an entry to the package search path.")
				.withRequiredArg().describedAs("packageName");
	}

	protected boolean createAdditionalOptions(OptionParser p) {
		return false;
	}

	public AbstractExperimentRunner parseArgs(String[] args) {
		try {
			doParseArgs(args);
		} catch (RuntimeException e1) {
			printUsageAndExit(String.valueOf(e1.getLocalizedMessage()));
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
			throw new RuntimeException(String.format(Util.DEF_LOCALE,
					"unrecognized command line parameters: %s",
					Arrays.toString(argList.toArray())));
		}
	}

	protected void handleRemainingArgs(List<?> argList) {
	}

	protected void processOptions(OptionSet opts) {
		if (opts.has("help"))
			printUsageAndExit();

		if (opts.has("log")) {
			String vs = (String) opts.valueOf("log");

			ExpMsgCategory cat = Enum.valueOf(ExpMsgCategory.class,
					vs.toUpperCase(Locale.US));
			if (cat == ExpMsgCategory.OFF) {
				listeners.remove(ConsolePrinter.class);
			} else {
				listeners.put(ConsolePrinter.class, new ConsolePrinter(cat));
			}
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
			packageSearchPath = Util.addToArray(packageSearchPath, (String) o,
					String.class);
		}
	}

	protected void printErrorAndExit(int exitCode, String format,
			Object... args) {
		System.err.printf(format, args);
		System.exit(exitCode);
	}

	protected void printUsageAndExit(String format, Object... args) {
		System.err.printf(format, args);
		System.err.println();
		System.err.println();
		printUsageAndExit();
	}

	protected void printUsageAndExit() {
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
		System.exit(1);
	}

	protected String getHelpFooterText() {
		return lineSeparator()
				+ "All parameter names are CASE-SENSITIVE. For detailed information see "
				+ lineSeparator() + "http://jasima.googlecode.com.";
	}

	protected String getHelpCmdLineText() {
		return getClass().getName();
	}

	protected abstract Experiment createExperiment();

	protected String getResultFileNameHint() {
		return experimentFileName;
	}

	public void run() {
		System.out
				.println("**********************************************************************");
		System.out.println(Util.ID_STRING);
		System.out
				.println("**********************************************************************");
		System.out.println();
		try {
			Experiment exp = configureExperiment();
			doRun(exp);
		} catch (Throwable t) {
			printErrorAndExit(10, "%s: %s", getResultFileNameHint(),
					t.getLocalizedMessage());
		}
	}

	protected Experiment configureExperiment() {
		// create and configure experiment
		Experiment exp = createExperiment();
		String resultFileNameHint = getResultFileNameHint();

		for (NotifierListener<Experiment, ExperimentEvent> lstnr : listeners
				.values()) {
			if (resultFileNameHint != null
					&& lstnr instanceof AbstractResultSaver) {
				((AbstractResultSaver) lstnr)
						.setFileNameHint(resultFileNameHint);
			}
			exp.addNotifierListener(lstnr);
		}

		exp = setPropsFromCmdLine(exp);
		return exp;
	}

	protected void doRun(Experiment exp) {
		try {
			ExperimentFuture ef = ExperimentExecutor.getExecutor()
					.runExperiment(exp, null);
			Map<String, Object> res = ef.get();

			String msg = (String) res.get(Experiment.EXCEPTION_MESSAGE);
			String exc = (String) res.get(Experiment.EXCEPTION);
			if (msg != null || exc != null) {
				throw new RuntimeException(msg + "; detailed error: " + exc);
			}

			if (!hideResults)
				exp.printResults(res);
		} catch (InterruptedException e1) {
			throw new RuntimeException(e1);
		}
	}

	protected Experiment setPropsFromCmdLine(Experiment exp) {
		// sort props by number of segments so we set parent properties first
		Collections.sort(manualProps, new Comparator<Pair<String, Object>>() {
			@Override
			public int compare(Pair<String, Object> p1, Pair<String, Object> p2) {
				int i1 = numSegments(p1.a);
				int i2 = numSegments(p2.a);

				return i1 - i2;
			}

			/**
			 * Counts the number of dots '.' in a String.
			 */
			private int numSegments(String a) {
				if (a == null || a.length() == 0)
					return 0;

				int res = 1;
				int from = -1;
				while ((from = a.indexOf('.', from + 1)) >= 0) {
					res++;
				}

				return res;
			}
		});

		// try to set each property
		for (Pair<String, Object> p : manualProps) {
			String name = p.a;
			Object value = p.b;

			TypeUtil.setPropertyEx(exp, name, value, getClass()
					.getClassLoader(), packageSearchPath);
		}

		return exp;
	}

}