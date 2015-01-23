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
package jasima.core.util.run;

import jasima.core.expExecution.ExperimentExecutor;
import jasima.core.expExecution.ExperimentFuture;
import jasima.core.experiment.Experiment;
import jasima.core.experiment.Experiment.ExpMsgCategory;
import jasima.core.experiment.Experiment.ExperimentEvent;
import jasima.core.util.AbstractResultSaver;
import jasima.core.util.ConsolePrinter;
import jasima.core.util.ExcelSaver;
import jasima.core.util.XmlSaver;
import jasima.core.util.observer.NotifierListener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Base class for experiment runner utility classes.
 * 
 * @see XmlExperimentRunner
 * @see ExcelExperimentRunner
 * 
 * @author Robin Kreis
 * @author Torsten Hildebrandt, 2013-01-08
 * @version 
 *          "$Id$"
 */
public abstract class AbstractExperimentRunner {

	protected Map<Object, NotifierListener<Experiment, ExperimentEvent>> listeners = new HashMap<Object, NotifierListener<Experiment, ExperimentEvent>>();
	protected boolean printResults = false;

	public AbstractExperimentRunner() {
		listeners.put(ConsolePrinter.class, new ConsolePrinter(
				ExpMsgCategory.INFO));
	}

	protected AbstractExperimentRunner parseArgs(String[] args) {
		for (String arg : args) {
			if (!parseOption(arg)) {
				if (arg.startsWith("--")) {
					printUsageAndExit("Unknown option: %s%n", arg);
				}
				if (!parseArg(arg)) {
					printUsageAndExit();
				}
			}
		}
		return this;
	}

	protected boolean parseOption(String arg) {
		if (arg.startsWith("--log=")) {
			ExpMsgCategory cat = ExpMsgCategory.valueOf(arg.substring(
					"--log=".length()).toUpperCase(Locale.ROOT));
			if (cat == ExpMsgCategory.OFF) {
				listeners.remove(ConsolePrinter.class);
			} else {
				listeners.put(ConsolePrinter.class, new ConsolePrinter(cat));
			}

		} else if (arg.equals("--printres")) {
			printResults = true;
		} else if (arg.equals("--xmlres")) {
			listeners.put(XmlSaver.class, new XmlSaver());

		} else if (arg.startsWith("--xmlres=")) {
			XmlSaver xs = new XmlSaver();
			xs.setResultFileName(arg.substring("--xmlres=".length()));
			listeners.put(XmlSaver.class, xs);

		} else if (arg.equals("--xlsres")) {
			listeners.put(ExcelSaver.class, new ExcelSaver());

		} else if (arg.startsWith("--xlsres=")) {
			ExcelSaver es = new ExcelSaver();
			es.setResultFileName(arg.substring("--xlsres=".length()));
			listeners.put(ExcelSaver.class, es);

		} else {
			return false;
		}
		return true;
	}

	protected boolean parseArg(String arg) {
		return false;
	}

	protected void printErrorAndExit(int exitCode, String format,
			Object... args) {
		System.err.printf(format, args);
		System.exit(exitCode);
	}

	protected void printUsageAndExit(String format, Object... args) {
		System.err.printf(format, args);
		printUsageAndExit();
	}

	protected String getArgInfo() {
		return "[options]";
	}

	protected String getOptInfo() {
		return String.format(""
				+ "    --log=OFF         Don't show any log messages.%n"
				+ "    --log=ERROR       Show only errors.%n"
				+ "    --log=WARN        Show errors and warnings.%n"
				+ "    --log=INFO        Also show info messages.%n"
				+ "    --log=DEBUG       Also show debug messages.%n"
				+ "    --log=TRACE       Also show trace messages.%n"
				+ "    --printres        Print results to console.%n"
				+ "    --xmlres=<file>   Save results in XML format%n"
				+ "    --xlsres=<file>   Save results in Excel format%n");
	}

	protected void printUsageAndExit() {
		System.err.printf("Usage: java %s %s%n%s", getClass().getName(),
				getArgInfo(), getOptInfo());
		System.exit(1);
	}

	protected abstract Experiment createExperiment();

	protected String getResultFileNameHint() {
		return null;
	}

	protected void run() {
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
		
		try {
			ExperimentFuture ef = ExperimentExecutor.getExecutor()
					.runExperiment(exp);
			Map<String, Object> res = ef.get();

			String msg = (String) res.get(Experiment.EXCEPTION_MESSAGE);
			String exc = (String) res.get(Experiment.EXCEPTION);
			if (msg != null || exc != null) {
				throw new RuntimeException(msg + " detailed error: " + exc);
			}

			if (printResults)
				exp.printResults(res);
		} catch (InterruptedException e1) {
			throw new RuntimeException(e1);
		}
	}
}