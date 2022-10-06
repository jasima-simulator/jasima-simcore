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

import java.util.Locale;

import jasima.core.experiment.Experiment.ExperimentEvent;
import jasima.core.util.MsgCategory;
import jasima.core.util.Util;
import jasima.core.util.i18n.I18n;

/**
 * Simple base class for messages used by the notification mechanism of an
 * {@code Experiment}.
 */
public enum ExperimentMessage implements ExperimentEvent {
	EXPERIMENT_STARTING, EXPERIMENT_INITIALIZED, EXPERIMENT_BEFORE_RUN, EXPERIMENT_RUN_PERFORMED, EXPERIMENT_AFTER_RUN,
	EXPERIMENT_DONE, EXPERIMENT_COLLECTING_RESULTS, EXPERIMENT_FINISHING, EXPERIMENT_FINISHED, EXPERIMENT_ERROR,
	EXPERIMENT_FINALLY;

	/**
	 * Class to store print messages of an experiment.
	 */
	public static class ExpPrintMessage implements ExperimentEvent {

		public final Experiment exp;
		public final MsgCategory category;
		private String message;
		private String messageFormatString;
		private Object[] params;

		public ExpPrintMessage(Experiment exp, MsgCategory category, String message) {
			if (message == null)
				throw new NullPointerException();
			this.exp = exp;
			this.category = category;
			this.message = message;
		}

		public ExpPrintMessage(Experiment exp, MsgCategory category, String messageFormatString, Object... params) {
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