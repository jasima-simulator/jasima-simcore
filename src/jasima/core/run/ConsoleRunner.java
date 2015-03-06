package jasima.core.run;

import static java.lang.System.lineSeparator;
import jasima.core.experiment.Experiment;
import jasima.core.util.Pair;
import jasima.core.util.TypeUtil;
import jasima.core.util.Util;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * <p>
 * Main class to load and run an experiment from the command line. The
 * experiment can be specified by either the class name (in this case a new
 * experiment of this type will be created) or by specifying the name of an xml
 * file or Excel file containing an experiment (e.g., created with the gui).
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
	private Experiment experiment;
	private String expSpec;

	public ConsoleRunner(Experiment indirectExperiment) {
		super();
		this.experiment = indirectExperiment;
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
	protected void handleRemainingArgs(List<?> argList) {
		super.handleRemainingArgs(argList);

		if (experiment != null)
			return;

		if (argList.size() == 0) {
			throw new RuntimeException(MSG_NO_EXP_FILE);
		}

		if (((String) argList.get(0)).startsWith("-"))
			return;

		// we have to have at least one argument
		expSpec = (String) argList.remove(0);
	}

	@Override
	protected String getHelpCmdLineText() {
		if (experiment != null) {
			return "usage: " + experiment.getClass().getName() + " [options]";
		} else {
			return "usage: " + getClass().getName() + " <expSpec> [options]";
		}
	}

	@Override
	protected String getHelpFooterText() {
		if (experiment != null) {
			return "<expSpec>            Class name of an Experiment or file name "
					+ lineSeparator()
					+ "                     of an XML-serialized Experiment or"
					+ lineSeparator()
					+ "                     name of an xls file."
					+ lineSeparator() + super.getHelpFooterText();
		} else {
			return super.getHelpFooterText();
		}
	}

	@Override
	protected Experiment createExperiment() {
		Experiment e;
		// is it an Excel experiment?
		if (expSpec.toLowerCase(Util.DEF_LOCALE).endsWith(".xls")) {
			experimentFileName = expSpec;
			e = new ExcelExperimentReader(new File(experimentFileName),
					getClass().getClassLoader(), packageSearchPath)
					.createExperiment();
		} else {
			// normal Experiment (class name) or loaded from xml file
			experimentFileName = expSpec;
			if (experimentFileName.indexOf('(') > 0) {
				experimentFileName = experimentFileName.substring(0,
						experimentFileName.indexOf('(')).trim();
			}

			// load/create experiment
			e = TypeUtil.convert(expSpec, Experiment.class, "", getClass()
					.getClassLoader(), packageSearchPath);
		}

		if (e == null)
			printErrorAndExit(1, MSG_NO_EXP_FILE);

		return Objects.requireNonNull(e);
	}

	public static void main(String[] args) {
		new ConsoleRunner(null).parseArgs(args).run();
	}

}
