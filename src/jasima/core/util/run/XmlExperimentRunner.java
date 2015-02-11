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
import jasima.core.util.XmlUtil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Loads an experiment from an XML file and executes it on the command line.
 * 
 * @author Robin Kreis
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 */
public class XmlExperimentRunner extends AbstractExperimentRunner {

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
		return "usage: " + getClass().getName() + " <xmlFileName> [options]";
	}

	@Override
	protected String getHelpFooterText() {
		return "<xmlFileName>        The file name of an XML-serialized Experiment."
				+ System.lineSeparator()
				+ System.lineSeparator()
				+ super.getHelpFooterText();
	}

	@Override
	protected Experiment createExperiment() {
		try (BufferedReader r = new BufferedReader(new FileReader(
				experimentFileName))) {
			Experiment retVal = (Experiment) XmlUtil.loadXML(r);
			return retVal;
		} catch (FileNotFoundException e) {
			printErrorAndExit(2, "Couldn't find experiment file '%s'.",
					experimentFileName);
		} catch (IOException e) {
			// exception on close
			printErrorAndExit(3, "Error on closing experiment file. %s",
					e.getLocalizedMessage());
		}

		throw new AssertionError(); // should never be reached
	}

	public static void main(String[] args) {
		new XmlExperimentRunner().parseArgs(args).run();
	}

}
