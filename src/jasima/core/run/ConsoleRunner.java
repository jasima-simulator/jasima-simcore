package jasima.core.run;

import static java.lang.System.lineSeparator;
import static java.util.Arrays.asList;
import jasima.core.experiment.Experiment;
import jasima.core.util.Pair;
import jasima.core.util.TypeUtil;
import jasima.core.util.Util;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Objects;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * <p>
 * Main class to load and run an experiment from the command line. The
 * experiment can be specified by either the class name (in this case a new
 * experiment of this type will be created) or by specifying the name of an xml
 * file containing an experiment (e.g., created with the gui).
 * </p>
 * <p>
 * Furthermore this class is used indirectly by Experiments to configure and run
 * them.
 * </p>
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 * 
 * @see Experiment#main(String[])
 */
public class ConsoleRunner extends AbstractExperimentRunner {

	private static final String MSG_NO_EXP_FILE = "No valid experiment file name/class name given.";

	private PropertyDescriptor[] beanProps;
	private boolean isIndirectUse;
	private Experiment experiment;

	public ConsoleRunner(Experiment indirectExperiment) {
		super();
		this.experiment = indirectExperiment;
		this.isIndirectUse = indirectExperiment != null;
	}

	@Override
	protected void doParseArgs(String[] args) {
		if (!isIndirectUse) {
			if (args.length == 0)
				printErrorAndExit(1, MSG_NO_EXP_FILE);

			// try to read/instantiate the main experiment
			String main = args[0];

			// is it an option?
			if (!main.startsWith("-")) {
				// no, could be class or file name
				experimentFileName = main;
				experiment = (Experiment) TypeUtil.loadClassOrXmlFile(main,
						getClass().getClassLoader(), packageSearchPath);
				if (experiment == null)
					printErrorAndExit(1, MSG_NO_EXP_FILE);

				// reduce param array by one entry
				args = asList(args).subList(1, args.length).toArray(
						new String[args.length - 1]);
			}
		}

		super.doParseArgs(args);

		// is experiment still null? This can happen for a command line like
		// "-? -nores"
		if (experiment == null)
			printErrorAndExit(1, MSG_NO_EXP_FILE);
	}

	@Override
	protected boolean createAdditionalOptions(OptionParser p) {
		if (experiment != null) {
			beanProps = TypeUtil.findWritableProperties(experiment);

			// create a command line option for each top level property
			for (PropertyDescriptor prop : beanProps) {
				Class<?> type = prop.getPropertyType();

				String description = type.isPrimitive() ? "" : String.format(
						Util.DEF_LOCALE, "Property of type '%s'",
						type.getName());
				if (type.isEnum()) {
					String enumValues = Arrays
							.toString(type.getEnumConstants()).replaceAll(
									"[\\[\\]]", "");
					description = String.format(Util.DEF_LOCALE,
							"Possible values: %s", enumValues);
				}

				p.accepts(prop.getName(), description).withRequiredArg()
						.describedAs(type.getSimpleName().toUpperCase());
			}
			return true;
		} else {
			beanProps = new PropertyDescriptor[0];
			return false;
		}
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
		if (isIndirectUse) {
			return "usage: " + experiment.getClass().getName() + " [options]";
		} else {
			return "usage: " + getClass().getName() + " <expSpec> [options]";
		}
	}

	@Override
	protected String getHelpFooterText() {
		if (!isIndirectUse) {
			return "<expSpec>            Class name of an Experiment or file name "
					+ lineSeparator()
					+ "                     of an XML-serialized Experiment."
					+ lineSeparator() + super.getHelpFooterText();
		} else {
			return super.getHelpFooterText();
		}
	}

	@Override
	protected Experiment createExperiment() {
		return Objects.requireNonNull(experiment);
	}

	public static void main(String[] args) {
		new ConsoleRunner(null).parseArgs(args).run();
	}

}
