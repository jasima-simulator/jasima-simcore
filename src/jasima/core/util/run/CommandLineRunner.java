package jasima.core.util.run;

import static java.util.Arrays.asList;
import jasima.core.experiment.Experiment;
import jasima.core.util.Pair;
import jasima.core.util.Util;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.List;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * This class is used indirectly by Experiments to configure and run them.
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 * 
 * @see Experiment#main(String[])
 */
public class CommandLineRunner extends AbstractExperimentRunner {

	private static final String MSG_NO_EXP_FILE = "No experiment file name/class name given.";

	private final Experiment experiment;
	private PropertyDescriptor[] beanProps;
	private final boolean isUsedIndirectly;

	public CommandLineRunner(String expName, boolean isUsedIndirectly) {
		super();
		this.isUsedIndirectly = isUsedIndirectly;
		this.experimentFileName = expName;

		if (expName != null)
			this.experiment = (Experiment) loadClassOrXmlFile(expName,
					getClass().getClassLoader());
		else
			this.experiment = null;
	}

	@Override
	protected void handleRemainingArgs(List<?> argList) {
		super.handleRemainingArgs(argList);

		if (argList.size() == 0 && experiment == null) {
			throw new RuntimeException(MSG_NO_EXP_FILE);
		}
	}

	@Override
	protected OptionParser createParser() {
		OptionParser p = super.createParser();

		if (experiment != null) {
			beanProps = Util.findWritableProperties(experiment);

			// create a command line option for each top level property
			for (PropertyDescriptor prop : beanProps) {
				Class<?> type = prop.getPropertyType();

				String description = type.isPrimitive() ? "" : String.format(
						"Property of type '%s'", type.getName());
				if (type.isEnum()) {
					String enumValues = Arrays
							.toString(type.getEnumConstants()).replaceAll(
									"[\\[\\]]", "");
					description = String.format("Possible values: %s",
							enumValues);
				}

				p.accepts(prop.getName(), description).withRequiredArg()
						.describedAs(type.getSimpleName().toUpperCase());
			}
		} else {
			beanProps = new PropertyDescriptor[0];
		}

		return p;
	}

	@Override
	protected void processOptions(OptionSet opts) {
		super.processOptions(opts);

		for (PropertyDescriptor prop : beanProps) {
			String name = prop.getName();
			if (opts.has(name)) {
				String value = (String) opts.valueOf(name);
				manualProps.add(new Pair<String, Object>(name, value));
			}
		}
	}

	@Override
	protected String getHelpCmdLineText() {
		if (isUsedIndirectly && experiment != null) {
			return "usage: " + experiment.getClass().getName() + " [options]";
		} else {
			return "usage: " + getClass().getName() + " <expSpec> [options]";
		}
	}

	@Override
	protected Experiment createExperiment() {
		if (experiment != null) {
			return experiment;
		} else {
			Object o = loadClassOrXmlFile(getResultFileNameHint(), getClass()
					.getClassLoader());
			return (Experiment) o;
		}
	}

	public static void main(String[] args) {
		String classOrXml = null;
		if (args.length > 0) {
			// first argument is class name or experiment from xml file
			classOrXml = args[0];
			// remaining arguments
			args = asList(args).subList(1, args.length).toArray(
					new String[args.length - 1]);
		}

		new CommandLineRunner(classOrXml, false).parseArgs(args).run();
	}

}
