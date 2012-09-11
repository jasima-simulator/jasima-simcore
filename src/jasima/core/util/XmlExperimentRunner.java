/*******************************************************************************
 * Copyright 2011, 2012 Torsten Hildebrandt and BIBA - Bremer Institut für Produktion und Logistik GmbH
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
 *******************************************************************************/
package jasima.core.util;

import jasima.core.experiment.Experiment;
import jasima.core.experiment.Experiment.ExpMsgCategory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

/**
 * Runs an experiment from an XML file and executes it on the command line.
 * 
 * @version $Id$
 */
public class XmlExperimentRunner {

	protected boolean optSilent = false;
	protected String experimentFileName = null;
	protected String resultsFileName = null;
	protected ExpMsgCategory outputCat = ExpMsgCategory.INFO;

	public static void main(String[] args) {
		new XmlExperimentRunner().parseArgs(args).run();
	}

	protected XmlExperimentRunner parseArgs(String[] args) {
		for (String arg : args) {
			if (arg.equals("--silent")) {
				optSilent = true;
				outputCat = ExpMsgCategory.WARN;
			} else if (arg.equals("--debug")) {
				outputCat = ExpMsgCategory.DEBUG;
			} else if (arg.equals("--trace")) {
				outputCat = ExpMsgCategory.TRACE;
			} else {
				if (arg.startsWith("--")) {
					printUsageAndExit("Unknown option: %s%n", arg);
				}
				if (experimentFileName == null) {
					experimentFileName = arg;
				} else if (resultsFileName == null) {
					resultsFileName = arg;
				} else {
					printUsageAndExit();
				}
			}
		}
		if (experimentFileName == null) {
			printUsageAndExit("No experiment file name given.%n");
		}
		return this;
	}

	protected void printUsageAndExit(String format, Object... args) {
		System.err.printf(format, args);
		printUsageAndExit();
	}

	protected void printUsageAndExit() {
		System.err
				.printf("Usage: java %s [options] <fileName> [<resultFileName>]%n"
						+ "    --silent          Don't print results.%n"
						+ "    --trace           Set logging level to trace.%n"
						+ "    --debug           Set logging level to debug.%n"
						+ "    <fileName>        The file name of an xml-serialized Experiment.%n"
						+ "    <resultFileName>  Optional: Save results as an xml file.",
						getClass().getName());
		System.exit(1);
	}

	protected void run() {
		BufferedReader r;
		try {
			r = new BufferedReader(new FileReader(experimentFileName));
		} catch (FileNotFoundException e) {
			System.err.println("Couldn't find experiment file.");
			System.exit(2);
			return;
		}

		Experiment exp = (Experiment) XmlUtil.loadXML(r);
		exp.addNotifierListener(new ConsolePrinter(outputCat));
		
		exp.runExperiment();
		Map<String, Object> res = exp.getResults();

		if (!optSilent)
			exp.printResults();

		try {
			r.close();
		} catch (IOException e) {
			// ignore
		}

		if (resultsFileName != null) {
			XmlUtil.saveXML(res, new File(resultsFileName));
		}
	}

}
