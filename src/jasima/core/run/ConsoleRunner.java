/*******************************************************************************
 * This file is part of jasima, v1.3, the Java simulator for manufacturing and 
 * logistics.
 *  
 * Copyright (c) 2015 		jasima solutions UG
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package jasima.core.run;

import static jasima.core.util.i18n.I18n.defFormat;
import static java.lang.System.lineSeparator;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import jasima.core.experiment.Experiment;
import jasima.core.util.Pair;
import jasima.core.util.TypeUtil;
import jasima.core.util.i18n.I18n;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * Main class to load and run an experiment from the command line. The
 * experiment can be specified by either the class name (in this case a new
 * experiment of this type will be created) or by specifying the name of an xml
 * file or Excel file containing an experiment (e.g., created with the gui).
 * <p>
 * Furthermore this class is used indirectly by Experiments to configure and run
 * them.
 * 
 * @author Torsten Hildebrandt
 * 
 * @see Experiment#main(String[])
 */
public class ConsoleRunner extends AbstractExperimentRunner {

	private static final String MSG_NO_EXP_FILE = "No valid experiment file name/class name given.";

	private PropertyDescriptor[] beanProps;
	private Experiment experiment;
	private String expSpec;

	public ConsoleRunner(@Nullable Experiment indirectExperiment) {
		super();
		this.experiment = indirectExperiment;
		if (indirectExperiment != null) {
			this.experimentFileName = indirectExperiment.getClass().getSimpleName();
		}
	}

	@Override
	protected boolean createAdditionalOptions(OptionParser p) {
		if (experiment != null) {
			beanProps = TypeUtil.findWritableProperties(experiment);

			// create a command line option for each top level property
			for (PropertyDescriptor prop : beanProps) {
				Class<?> type = prop.getPropertyType();

				String description = type.isPrimitive() ? "" : defFormat("Property of type '%s'", type.getName());
				if (type.isEnum()) {
					String enumValues = Arrays.toString(type.getEnumConstants()).replaceAll("[\\[\\]]", "");
					description = defFormat("Possible values: %s", enumValues);
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
			return "<expSpec>            Class name of an Experiment or file name " + lineSeparator()
					+ "                     of an XML-serialized Experiment or" + lineSeparator()
					+ "                     name of an xls file." + lineSeparator() + super.getHelpFooterText();
		} else {
			return super.getHelpFooterText();
		}
	}

	@Override
	protected @Nullable Experiment createExperiment() {
		Experiment e;
		if (experiment != null) {
			e = experiment;
		} else if (expSpec.toLowerCase(I18n.DEF_LOCALE).endsWith(".xls")) {
			// is it an Excel experiment?
			experimentFileName = expSpec;
			e = new ExcelExperimentReader(new File(experimentFileName), getClass().getClassLoader(), packageSearchPath)
					.createExperiment();
		} else {
			// normal Experiment (class name) or loaded from xml file
			e = TypeUtil.convert(expSpec, Experiment.class, "", getClass().getClassLoader(), packageSearchPath);
		}

		if (e == null) {
			printError(1, MSG_NO_EXP_FILE);
		}

		return e;
	}

	public static @Nullable Map<String, Object> run(Experiment e, String... args) {
		ConsoleRunner cr = new ConsoleRunner(e);
		try {
			return cr.parseArgs(args).run();
		} catch (RuntimeException e1) {
			cr.printUsage(String.valueOf(e1.getLocalizedMessage()));
			return null;
		}
	}

	public static void main(String[] args) {
		run(null, args);
	}

}
