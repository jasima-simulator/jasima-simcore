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

import jasima.core.experiment.Experiment;

import java.io.File;
import java.util.List;

/**
 * Loads an experiment from an XLS file and executes it on the command line.
 * 
 * @author Robin Kreis
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 */
public class ExcelExperimentRunner extends AbstractExperimentRunner {

	private static final String MSG_NO_EXP_FILE = "No experiment file name given.";

	@Override
	protected void handleRemainingArgs(List<?> argList) {
		super.handleRemainingArgs(argList);

		if (argList.size() == 0) {
			throw new RuntimeException(MSG_NO_EXP_FILE);
		}

		// we have to have at least one argument
		experimentFileName = (String) argList.remove(0);
	}

	@Override
	protected String getHelpCmdLineText() {
		return "usage: " + getClass().getName() + " <xlsFileName> [options]";
	}

	@Override
	protected String getHelpFooterText() {
		return "<xlsFileName>        The file name of an Excel Experiment."
				+ System.lineSeparator() + super.getHelpFooterText();
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

	public static void main(String[] args) {
		new ExcelExperimentRunner().parseArgs(args).run();
	}

}
