package jasima.core.util.run;

import jasima.core.expExecution.ExperimentExecutor;
import jasima.core.experiment.Experiment;
import jasima.core.experiment.Experiment.ExpMsgCategory;
import jasima.core.experiment.Experiment.ExperimentEvent;
import jasima.core.experiment.ExperimentListenerBase;
import jasima.core.util.AbstractResultSaver;
import jasima.core.util.ConsolePrinter;
import jasima.core.util.ExcelSaver;
import jasima.core.util.XmlSaver;
import jasima.core.util.observer.NotifierListener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Base class for experiment runner utility classes.
 * 
 * @see XmlExperimentRunner
 * @see ExcelExperimentRunner
 * 
 * @author Robin Kreis
 * @author Torsten Hildebrandt, 2013-01-08
 * @version "$Id$"
 */
public abstract class AbstractExperimentRunner {

	protected Map<Object, NotifierListener<Experiment, ExperimentEvent>> listeners = new HashMap<Object, NotifierListener<Experiment, ExperimentEvent>>();

	public AbstractExperimentRunner() {
		listeners.put(ConsolePrinter.class, new ConsolePrinter(
				ExpMsgCategory.INFO));
	}

	protected AbstractExperimentRunner parseArgs(String[] args) {
		for (String arg : args) {
			if (!parseOption(arg)) {
				if (arg.startsWith("--")) {
					printUsageAndExit("Unknown option: %s%n", arg);
				}
				if (!parseArg(arg)) {
					printUsageAndExit();
				}
			}
		}
		return this;
	}

	protected boolean parseOption(String arg) {
		if (arg.startsWith("--log=")) {
			ExpMsgCategory cat = ExpMsgCategory.valueOf(arg.substring(
					"--log=".length()).toUpperCase(Locale.ROOT));
			if (cat == ExpMsgCategory.OFF) {
				listeners.remove(ConsolePrinter.class);
			} else {
				listeners.put(ConsolePrinter.class, new ConsolePrinter(cat));
			}

		} else if (arg.equals("--printres")) {
			listeners.put("ResultPrinter", createResultPrinter());

		} else if (arg.equals("--xmlres")) {
			listeners.put(XmlSaver.class, new XmlSaver());

		} else if (arg.startsWith("--xmlres=")) {
			XmlSaver xs = new XmlSaver();
			xs.setResultFileName(arg.substring("--xmlres=".length()));
			listeners.put(XmlSaver.class, xs);

		} else if (arg.equals("--xlsres")) {
			listeners.put(ExcelSaver.class, new ExcelSaver());

		} else if (arg.startsWith("--xlsres=")) {
			ExcelSaver es = new ExcelSaver();
			es.setResultFileName(arg.substring("--xlsres=".length()));
			listeners.put(ExcelSaver.class, es);

		} else {
			return false;
		}
		return true;
	}

	protected boolean parseArg(String arg) {
		return false;
	}

	protected void printErrorAndExit(int exitCode, String format,
			Object... args) {
		System.err.printf(format, args);
		System.exit(exitCode);
	}

	protected void printUsageAndExit(String format, Object... args) {
		System.err.printf(format, args);
		printUsageAndExit();
	}

	protected String getArgInfo() {
		return "[options]";
	}

	protected String getOptInfo() {
		return String.format(""
				+ "    --log=OFF         Don't show any log messages.%n"
				+ "    --log=ERROR       Show only errors.%n"
				+ "    --log=WARN        Show errors and warnings.%n"
				+ "    --log=INFO        Also show info messages.%n"
				+ "    --log=DEBUG       Also show debug messages.%n"
				+ "    --log=TRACE       Also show trace messages.%n"
				+ "    --printres        Print results to console.%n"
				+ "    --xmlres=<file>   Save results in XML format%n"
				+ "    --xlsres=<file>   Save results in Excel format%n");
	}

	protected void printUsageAndExit() {
		System.err.printf("Usage: java %s %s%n%s", getClass().getName(),
				getArgInfo(), getOptInfo());
		System.exit(1);
	}

	protected abstract Experiment createExperiment();

	protected String getResultFileNameHint() {
		return null;
	}

	protected void run() {
		Experiment exp = createExperiment();
		String resultFileNameHint = getResultFileNameHint();

		for (NotifierListener<Experiment, ExperimentEvent> lstnr : listeners
				.values()) {
			if (resultFileNameHint != null
					&& lstnr instanceof AbstractResultSaver) {
				((AbstractResultSaver) lstnr)
						.setFileNameHint(resultFileNameHint);
			}
			exp.addNotifierListener(lstnr);
		}

		try {
			ExperimentExecutor.getExecutor().runExperiment(exp).get();
		} catch (InterruptedException e1) {
			throw new RuntimeException(e1);
		}
	}

	protected NotifierListener<Experiment, ExperimentEvent> createResultPrinter() {
		return new ExperimentListenerBase() {
			private static final long serialVersionUID = -4495594880800010905L;

			@Override
			protected void finished(Experiment e, Map<String, Object> results) {
				e.printResults(results);
			}
		};
	}
}