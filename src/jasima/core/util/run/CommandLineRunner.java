package jasima.core.util.run;

import jasima.core.experiment.Experiment;
import jasima.core.util.Pair;
import jasima.core.util.Util;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * This class is used indirectly by Experiments to configure and run them.
 * 
 * @author Torsten Hildebrandt
 * @version "$Id$"
 * 
 * @see Experiment#main(String[])
 */
public class CommandLineRunner extends AbstractExperimentRunner {

	private Experiment experiment;
	private ArrayList<String> beanOptions;

	public CommandLineRunner(Experiment experiment) {
		super();
		this.experiment = Objects.requireNonNull(experiment);
		beanOptions = new ArrayList<>();
	}

	@Override
	protected OptionParser createParser() {
		OptionParser p = super.createParser();

		PropertyDescriptor[] props = Util.findWritableProperties(experiment);
		for (PropertyDescriptor prop : props) {
			Class<?> type = prop.getPropertyType();

			String description = "";
			if (type.isEnum()) {
				String enumValues = Arrays.toString(type.getEnumConstants())
						.replaceAll("[\\[\\]]", "");
				description = String.format("possible values: %s", enumValues);
			}

			p.accepts(prop.getName(), description).withRequiredArg()
					.describedAs(type.getSimpleName().toUpperCase());

			beanOptions.add(prop.getName());
		}

		return p;
	}

	@Override
	protected void processOptions(OptionSet opts) {
		super.processOptions(opts);

		for (String s : beanOptions) {
			if (opts.has(s)) {
				String value = (String) opts.valueOf(s);

				manualProps.add(new Pair<String, Object>(s, value));
			}
		}
	}

	@Override
	protected String getHelpCmdLineText() {
		return "usage: " + experiment.getClass().getName() + " [options]";
	}

	@Override
	protected Experiment createExperiment() {
		return experiment;
	}

}
