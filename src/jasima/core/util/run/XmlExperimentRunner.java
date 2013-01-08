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
package jasima.core.util.run;

import jasima.core.experiment.Experiment;
import jasima.core.util.XmlUtil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Loads an experiment from an XML file and executes it on the command line.
 * 
 * @author Robin Kreis
 * @author Torsten Hildebrandt, 2013-01-08
 * @version "$Id$"
 */
public class XmlExperimentRunner extends AbstractExperimentRunner {

	protected String experimentFileName = null;

	@Override
	protected AbstractExperimentRunner parseArgs(String[] args) {
		super.parseArgs(args);
		if (experimentFileName == null) {
			printUsageAndExit("No experiment file name given.%n");
		}
		return this;
	}

	protected boolean parseArg(String arg) {
		if (experimentFileName == null) {
			experimentFileName = arg;
		} else {
			return false;
		}
		return true;
	}

	@Override
	protected String getArgInfo() {
		return String.format("%s <xmlFile>", super.getArgInfo());
	}

	@Override
	protected String getOptInfo() {
		return String
				.format("%s"
						+ "    <xmlFile>         The file name of an XML-serialized Experiment.%n",
						super.getOptInfo());
	}

	@Override
	protected String getResultFileNameHint() {
		return experimentFileName;
	}

	@Override
	protected Experiment createExperiment() {
		BufferedReader r;
		try {
			r = new BufferedReader(new FileReader(experimentFileName));
		} catch (FileNotFoundException e) {
			printErrorAndExit(2, "Couldn't find experiment file.");
			return null;
		}

		Experiment retVal = (Experiment) XmlUtil.loadXML(r);
		try {
			r.close();
		} catch (IOException e) {
			// ignore
		}
		return retVal;
	}

	public static void main(String[] args) {
		new XmlExperimentRunner().parseArgs(args).run();
	}

}
