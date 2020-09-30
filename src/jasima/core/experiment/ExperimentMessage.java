package jasima.core.experiment;

import java.util.Locale;

import jasima.core.util.MsgCategory;
import jasima.core.util.Util;
import jasima.core.util.i18n.I18n;

/**
 * Simple base class for messages used by the notification mechanism of an
 * {@code Experiment}.
 */
public class ExperimentMessage {
	public final String s;

	public ExperimentMessage(String s) {
		super();
		this.s = s;
	}

	@Override
	public String toString() {
		return s;
	}

	public static final ExperimentMessage EXPERIMENT_STARTING = new ExperimentMessage("EXPERIMENT_STARTING");
	public static final ExperimentMessage EXPERIMENT_INITIALIZED = new ExperimentMessage("EXPERIMENT_INITIALIZED");
	public static final ExperimentMessage EXPERIMENT_BEFORE_RUN = new ExperimentMessage("EXPERIMENT_BEFORE_RUN");
	public static final ExperimentMessage EXPERIMENT_RUN_PERFORMED = new ExperimentMessage("EXPERIMENT_RUN_PERFORMED");
	public static final ExperimentMessage EXPERIMENT_AFTER_RUN = new ExperimentMessage("EXPERIMENT_AFTER_RUN");
	public static final ExperimentMessage EXPERIMENT_DONE = new ExperimentMessage("EXPERIMENT_DONE");
	public static final ExperimentMessage EXPERIMENT_COLLECTING_RESULTS = new ExperimentMessage(
			"EXPERIMENT_COLLECTING_RESULTS");
	public static final ExperimentMessage EXPERIMENT_FINISHING = new ExperimentMessage("EXPERIMENT_FINISHING");
	public static final ExperimentMessage EXPERIMENT_FINISHED = new ExperimentMessage("EXPERIMENT_FINISHED");
	public static final ExperimentMessage EXPERIMENT_ERROR = new ExperimentMessage("EXPERIMENT_ERROR");
	public static final ExperimentMessage EXPERIMENT_FINALLY = new ExperimentMessage("EXPERIMENT_FINALLY");

	/**
	 * Class to store print messages of an experiment.
	 */
	public static class ExpPrintMessage extends ExperimentMessage {

		public final Experiment exp;
		public final MsgCategory category;
		private String message;
		private String messageFormatString;
		private Object[] params;

		public ExpPrintMessage(Experiment exp, MsgCategory category, String message) {
			super("ExpPrintEvent");
			if (message == null)
				throw new NullPointerException();
			this.exp = exp;
			this.category = category;
			this.message = message;
		}

		public ExpPrintMessage(Experiment exp, MsgCategory category, String messageFormatString, Object... params) {
			super("ExpPrintEvent");
			this.exp = exp;
			this.category = category;
			this.messageFormatString = messageFormatString;
			this.params = params;
			this.message = null;
		}

		/**
		 * Returns this message formatted as a {@code String} using the default
		 * {@link Locale}.
		 * 
		 * @return The formatted message using the default {@code Locale}.
		 * @see Util#DEF_LOCALE
		 */
		public String getMessage() {
			return getMessage(I18n.DEF_LOCALE);
		}

		/**
		 * Returns this message formatted using the given {@link Locale}.
		 * 
		 * @param locale The {@link Locale} to use when formatting the message.
		 * @return The formatted message.
		 */
		public String getMessage(Locale locale) {
			// lazy creation of message only when needed
			if (message == null) {
				message = String.format(locale, messageFormatString, params);
				messageFormatString = null;
				params = null;
			}

			return message;
		}

		@Override
		public String toString() {
			return getMessage();
		}
	}
}