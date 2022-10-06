/*
This file is part of jasima, the Java simulator for manufacturing and logistics.
 
Copyright 2010-2022 jasima contributors (see license.txt)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
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
 * @author Robin Kreis
 * @author Torsten Hildebrandt
 */
public class MultipleConfigurationExperiment extends AbstractMultiConfExperiment {

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
			Experiment e = createExperimentForConf(conf);
			if (e != null) {
				experiments.add(e);
			}
		}
	}

}
