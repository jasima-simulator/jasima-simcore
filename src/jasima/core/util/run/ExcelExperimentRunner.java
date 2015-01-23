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

import jasima.core.experiment.Experiment;

import java.io.File;

/**
 * Loads an experiment from an XLS file and executes it on the command line.
 * 
 * @author Robin Kreis
 * @author Torsten Hildebrandt, 2013-01-08
 * @version 
 *          "$Id$"
 */
public class ExcelExperimentRunner extends AbstractExperimentRunner {

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
		return String.format("%s <xlsFile>", super.getArgInfo());
	}

	@Override
	protected String getOptInfo() {
		return String
				.format("%s"
						+ "    <xlsFile>         The file name of an Excel Experiment.%n",
						super.getOptInfo());
	}

	@Override
	protected String getResultFileNameHint() {
		return experimentFileName;
	}

	@Override
	protected Experiment createExperiment() {
		return new ExcelExperimentReader(new File(experimentFileName))
				.createExperiment();
	}

	@Override
	protected void run() {
		try {
			super.run();
		} catch (Throwable t) {
			printErrorAndExit(10, "%s: %s", experimentFileName,
					t.getLocalizedMessage());
		}
	}

	public static void main(String[] args) {
		new ExcelExperimentRunner().parseArgs(args).run();
	}

}
