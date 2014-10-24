/*******************************************************************************
 * Copyright (c) 2010-2013 Torsten Hildebrandt and jasima contributors
 *
 * This file is part of jasima, v1.0.
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
 *
 * $Id$
 *******************************************************************************/
package jasima.core.util;

import jasima.core.experiment.AbstractMultiExperiment;
import jasima.core.experiment.Experiment;
import jasima.core.experiment.Experiment.ExpMsgCategory;
import jasima.core.experiment.Experiment.ExpPrintEvent;
import jasima.core.experiment.ExperimentListenerBase;
import jasima.core.statistics.SummaryStat;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Prints experiment messages to the console.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version 
 *          "$Id$"
 */
public class ConsolePrinter extends ExperimentListenerBase {

	private static final long serialVersionUID = 6722626849679009735L;

	private ExpMsgCategory logLevel = ExpMsgCategory.INFO;
	private String logFormat = "%1$tT.%1$tL\t%4$s\t%2$s\t%3$s";
	private PrintWriter out = null;
	private boolean printStdEvents = true;

	public ConsolePrinter() {
		this(null, null);
	}

	public ConsolePrinter(ExpMsgCategory logLevel) {
		this(logLevel, null);
	}

	public ConsolePrinter(String logFormat) {
		this(null, logFormat);
	}

	public ConsolePrinter(ExpMsgCategory logLevel, String logFormat) {
		super();
		if (logLevel != null)
			setLogLevel(logLevel);
		if (logFormat != null)
			setLogFormat(logFormat);
	}

	@Override
	protected void print(Experiment e, ExpPrintEvent event) {
		if (event.category.ordinal() <= getLogLevel().ordinal()) {
			String name = e.getName();
			if (name == null)
				name = "exp@" + Integer.toHexString(e.hashCode());

			String msg = String.format(Locale.UK, getLogFormat(), new Date(),
					event.category.toString(), event.getMessage(), name);
			if (getOut() == null)
				System.out.println(msg);
			else
				getOut().println(msg);
		}
	}

	@Override
	protected void starting(final Experiment e) {
		if (isPrintStdEvents())
			e.print("starting...");
	}

	@Override
	protected void initialized(final Experiment e) {
		if (isPrintStdEvents())
			e.print("initializing...");
	}

	@Override
	protected void beforeRun(Experiment e) {
		if (isPrintStdEvents())
			e.print("running...");
	}

	@Override
	protected void afterRun(Experiment e) {
		if (isPrintStdEvents())
			e.print("terminating...");
	}

	@Override
	protected void done(Experiment e) {
		if (isPrintStdEvents())
			e.print("collecting results...");
	}

	@Override
	protected void finished(Experiment e, Map<String, Object> results) {
		if (isPrintStdEvents())
			e.print("finished.");
	}

	@Override
	protected void multiExperimentCompletedTask(Experiment e,
			Experiment runExperiment, Map<String, Object> runResults) {
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

			me.print("finished experiment %d/%d in %fs%s",
					me.getNumTasksExecuted(), me.getNumTasks(), runTime,
					abortStr);
		}
	}

	private String toString(Object o) {
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

	public ExpMsgCategory getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(ExpMsgCategory logLevel) {
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
	 * @param out
	 *            The {@code PrintWriter} to use.
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
	 */
	public void setPrintStdEvents(boolean printStdEvents) {
		this.printStdEvents = printStdEvents;
	}

	// static utility methods below

	/**
	 * Static method to prints the results <code>res</code> of an experiment
	 * <code>e</code> to {@link System#out}.
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
	 */
	public static void printResults(PrintWriter out, Experiment e,
			Map<String, Object> res) {
		out.println();
		out.println(getDescription(e, "; "));

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
				out.printf(Locale.ENGLISH,
						"%s\t%.4f\t%.4f\t%.4f\t%.4f\t%d\t%.4f\n", k, vs.mean(),
						vs.min(), vs.max(), vs.stdDev(), vs.numObs(), vs.sum());
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
						v = getDescription(((Experiment) v), "; ");
				}
				out.println(k + "\t" + v);
			}
		}

		out.println();
		out.println("  time needed:        " + res.get(Experiment.RUNTIME)
				+ "s");

		out.flush();
	}

	/**
	 * Returns a textual representation of an experiment's properties and their
	 * current values.
	 */
	public static String getDescription(Experiment e, String delim) {
		StringBuffer res = new StringBuffer(e.getClass().getSimpleName() + ": ");
		Map<String, Object> props = e.getPropsWithValues();
		for (String s : props.keySet()) {
			Object v = props.get(s);

			String valString;
			if (v != null && (v.getClass().isArray()))
				valString = Util.arrayToString(v);
			else if (v != null && (v instanceof Experiment))
				valString = "{{" + getDescription(((Experiment) v), delim)
						+ "}}";
			else
				valString = String.valueOf(v);

			res.append(s).append('=').append(valString).append(delim);
		}

		if (res.length() > 0)
			return res.substring(0, res.length() - delim.length());
		else
			return "";
	}

}
