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

/**
 * Loads an experiment from an XLS file and executes it on the command line.
 * 
 * @author Robin Kreis
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 * @deprecated Use ConsoleRunner instead.
 */
@Deprecated
public class ExcelExperimentRunner extends ConsoleRunner {

	public ExcelExperimentRunner() {
		super(null);
	}

}
