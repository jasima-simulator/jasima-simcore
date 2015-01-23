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
package jasima.core.experiment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Runs variations of a certain base experiment specified by a list of
 * configurations. Each configuration is a map containing name/value pairs to
 * describe the name of a property of baseExperiment which is set to a certain
 * value.
 * 
 * @see FullFactorialExperiment
 * @author Robin Kreis <r.kreis@uni-bremen.de>
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>, 2013-01-08
 * @version 
 *          "$Id$"
 */
public class MultipleConfigurationExperiment extends
		AbstractMultiConfExperiment {

	private static final long serialVersionUID = -6120299354891115684L;

	private List<Map<String, Object>> configurations = new ArrayList<Map<String, Object>>();

	public void setConfigurations(List<Map<String, Object>> configurations) {
		this.configurations = configurations;
	}

	public List<Map<String, Object>> getConfigurations() {
		return Collections.unmodifiableList(configurations);
	}

	public int getNumConfigurations() {
		return configurations.size();
	}

	public void addConfiguration(Map<String, Object> configuration) {
		configurations.add(configuration);
	}

	@Override
	protected void createExperiments() {
		for (Map<String, Object> conf : configurations) {
			handleConfig(conf);
		}
	}

}
