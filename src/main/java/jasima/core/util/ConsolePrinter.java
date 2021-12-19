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

import static jasima.core.util.i18n.I18n.defFormat;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import jasima.core.experiment.AbstractMultiExperiment;
import jasima.core.experiment.Experiment;
import jasima.core.experiment.ExperimentListener;
import jasima.core.experiment.ExperimentMessage.ExpPrintMessage;
import jasima.core.statistics.SummaryStat;
import jasima.core.util.i18n.I18n;

/**
 * Prints experiment messages to the console.
 * 
 * @author Torsten Hildebrandt
 */
public class ConsolePrinter implements ExperimentListener {

	private MsgCategory logLevel = MsgCategory.DEBUG;
	private Locale locale = I18n.DEF_LOCALE;
	private String logFormat = "%1$tT.%1$tL\t%4$s\t%2$s\t%3$s";
	private PrintWriter out = null;
	private boolean printStdEvents = true;

	public ConsolePrinter() {
		this(null, null);
	}

	public ConsolePrinter(MsgCategory logLevel) {
		this(logLevel, null);
	}

	public ConsolePrinter(String logFormat) {
		this(null, logFormat);
	}

	public ConsolePrinter(MsgCategory logLevel, String logFormat) {
		super();
		if (logLevel != null)
			setLogLevel(logLevel);
		if (logFormat != null)
			setLogFormat(logFormat);
	}

	@Override
	public void print(Experiment e, ExpPrintMessage event) {
		if (event.category.ordinal() <= getLogLevel().ordinal()) {
			String name = e.toString();

			String msg = String.format(getLocale(), getLogFormat(), new Date(), event.category.toString(),
					event.getMessage(getLocale()), name);
			if (getOut() == null)
				System.out.println(msg);
			else
				getOut().println(msg);
		}
	}

	@Override
	public void starting(Experiment e) {
		if (isPrintStdEvents()) {
			e.print("starting...");
		}
	}

	@Override
	public void initialized(Experiment e) {
		if (isPrintStdEvents())
			e.print("initializing...");
	}

	@Override
	public void beforeRun(Experiment e) {
		if (isPrintStdEvents())
			e.print("running...");
	}

	@Override
	public void afterRun(Experiment e) {
		if (isPrintStdEvents())
			e.print("terminating...");
	}

	@Override
	public void done(Experiment e) {
		if (isPrintStdEvents())
			e.print("collecting results...");
	}

	@Override
	public void finished(Experiment e, Map<String, Object> results) {
		if (isPrintStdEvents())
			e.print("finished.");
	}

	@Override
	public void error(Experiment e, Throwable t) {
		if (isPrintStdEvents())
			e.print("there was an exception: %s", t);
	}

	@Override
	public void multiExperimentCompletedTask(Experiment e, Experiment runExperiment, Map<String, Object> runResults) {
		if (isPrintStdEvents() && e instanceof AbstractMultiExperiment) {
			AbstractMultiExperiment me = (AbstractMultiExperiment) e;

			Double runTime = (Double) runResults.get(Experiment.RUNTIME);
			Integer aborted = (Integer) runResults.get(Experiment.EXP_ABORTED);
			String errorMsg = (String) runResults.get(Experiment.EXCEPTION_MESSAGE);

			String abortStr = "";
			if (aborted != null && aborted.doubleValue() != 0.0) {
				abortStr = "; ABORTED";
				if (errorMsg != null)
					abortStr += "; " + toString(errorMsg);
			}

			me.print("finished experiment %d/%d in %.2fs%s", me.getNumTasksExecuted(), me.getNumTasks(), runTime,
					abortStr);
		}
	}

	private static String toString(Object o) {
		String res;
		if (o == null) {
			res = "null";
		} else if (o.getClass().isArray()) {
			res = Util.arrayToString(o);
		} else {
			res = o.toString();
		}
		return res;
	}

	// getter and setter below

	public MsgCategory getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(MsgCategory logLevel) {
		this.logLevel = logLevel;
	}

	public String getLogFormat() {
		return logFormat;
	}

	public void setLogFormat(String logFormat) {
		this.logFormat = logFormat;
	}

	public PrintWriter getOut() {
		return out;
	}

	/**
	 * Sets the {@link PrintWriter} where to send output. If this is null, print
	 * events will be written to {@link System#out}.
	 * 
	 * @param out The {@code PrintWriter} to use.
	 */
	public void setOut(PrintWriter out) {
		this.out = out;
	}

	public boolean isPrintStdEvents() {
		return printStdEvents;
	}

	/**
	 * PrintStdEvents determines, if print events for standard events (like
	 * experiment starting) should be produced. Defaults to {@code true}.
	 * 
	 * @param printStdEvents Whether or not to print standard events.
	 */
	public void setPrintStdEvents(boolean printStdEvents) {
		this.printStdEvents = printStdEvents;
	}

	public Locale getLocale() {
		return locale;
	}

	/**
	 * Sets the {@link Locale} that is used when formatting messages. The default is
	 * {@code Locale.US}.
	 * 
	 * @param locale The locale to use.
	 * 
	 * @see Util#DEF_LOCALE
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	// static utility methods below

	/**
	 * Static method to prints the results <code>res</code> of an experiment
	 * <code>e</code> to {@link System#out}.
	 * 
	 * @param e   The experiment that was executed.
	 * @param res The list of results.
	 */
	public static void printResults(Experiment e, Map<String, Object> res) {
		PrintWriter pw = new PrintWriter(System.out, true);
		try {
			printResults(pw, e, res);
		} finally {
			pw.flush();
		}
	}

	/**
	 * Static method to print the results <code>res</code> of an experiment
	 * <code>e</code> to a {@link PrintWriter}.
	 * 
	 * @param out The {@link PrintWriter} to use for printing.
	 * @param e   The experiment that was executed.
	 * @param res The list of results.
	 */
	public static void printResults(PrintWriter out, Experiment e, Map<String, Object> res) {
		out.println();
		if (e != null) {
			out.println(getDescription(e));
		}

		ArrayList<String> valStatNames = new ArrayList<String>();
		ArrayList<String> otherNames = new ArrayList<String>();

		for (String k : res.keySet()) {
			if (k != Experiment.RUNTIME) {
				Object v = res.get(k);
				if (v instanceof SummaryStat) {
					valStatNames.add(k);
				} else {
					otherNames.add(k);
				}
			}
		}

		// sort by name, ignoring upper and lower case
		Collections.sort(valStatNames, new Comparator<String>() {

			public int compare(String s1, String s2) {
				return s1.compareToIgnoreCase(s2);
			}
		});
		Collections.sort(otherNames, new Comparator<String>() {

			public int compare(String s1, String s2) {
				return s1.compareToIgnoreCase(s2);
			}
		});

		// output ValueStat-objects
		if (valStatNames.size() > 0) {
			out.println();
			out.println("Name\tMean\tMin\tMax\tStdDev\tCount\tSum");

			for (String k : valStatNames) {
				SummaryStat vs = (SummaryStat) res.get(k);
				out.printf(I18n.DEF_LOCALE, "%s\t%.4f\t%.4f\t%.4f\t%.4f\t%d\t%.4f%n", k, vs.mean(), vs.min(), vs.max(),
						vs.stdDev(), vs.numObs(), vs.sum());
			}
		}

		// output all other objects (except runtime)
		if (otherNames.size() > 0) {
			out.println();
			out.println("Name\tValue");

			for (String k : otherNames) {
				Object v = res.get(k);
				if (v != null) {
					if (v.getClass().isArray())
						v = Util.arrayToString(v);
					else if (v instanceof Experiment)
						v = getDescription(((Experiment) v));
				}
				out.println(k + "\t" + v);
			}
		}

		out.println();
		out.printf(I18n.DEF_LOCALE, "time needed:\t%.1fs%n", res.get(Experiment.RUNTIME));

		out.flush();
	}

	/**
	 * Returns a textual representation of an experiment's properties and their
	 * current values.
	 * 
	 * @param e The experiment to describe.
	 */
	public static String getDescription(Experiment e) {
		String s;
		if (e.getName() != null)
			s = defFormat("Results of %s '%s'", e.getClass().getSimpleName(), e.getName());
		else
			s = defFormat("Results of %s", e.getClass().getSimpleName());
		return s;
	}
}
