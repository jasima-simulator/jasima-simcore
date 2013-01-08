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
 * @version "$Id$"
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
