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
package jasima.core.experiment;

import java.util.Map;

import jasima.core.experiment.AbstractMultiExperiment.BaseExperimentCompleted;
import jasima.core.experiment.Experiment.ExperimentEvent;
import jasima.core.experiment.ExperimentMessage.ExpPrintMessage;
import jasima.core.util.observer.NotifierListener;

/**
 * This class can be used as a base class for experiment listeners. It delegates
 * all events of {@link Experiment} to separate methods. The class can be used
 * by implementing his interface, overriding the methods of interest.
 * <p>
 * If only a single event/message type is of interest, then also lambda
 * expressions and the functional interfaces like {@link StartingListener} can
 * be used.
 * 
 * @author Torsten Hildebrandt
 */

public interface ExperimentListener extends NotifierListener<Experiment, ExperimentEvent> {

	// default implementations of all methods

	@Override // this is the only method defined in the parent interface
	default void inform(Experiment e, ExperimentEvent event) {
		if (event == ExperimentMessage.EXPERIMENT_STARTING) {
			starting(e);
		} else if (event == ExperimentMessage.EXPERIMENT_INITIALIZED) {
			initialized(e);
		} else if (event == ExperimentMessage.EXPERIMENT_BEFORE_RUN) {
			beforeRun(e);
		} else if (event == ExperimentMessage.EXPERIMENT_RUN_PERFORMED) {
			runPerformed(e);
		} else if (event == ExperimentMessage.EXPERIMENT_AFTER_RUN) {
			afterRun(e);
		} else if (event == ExperimentMessage.EXPERIMENT_DONE) {
			done(e);
		} else if (event == ExperimentMessage.EXPERIMENT_COLLECTING_RESULTS) {
			produceResults(e, e.resultMap);
		} else if (event == ExperimentMessage.EXPERIMENT_FINISHING) {
			finishing(e, e.resultMap);
		} else if (event == ExperimentMessage.EXPERIMENT_FINISHED) {
			finished(e, e.resultMap);
		} else if (event == ExperimentMessage.EXPERIMENT_ERROR) {
			error(e, e.error);
		} else if (event == ExperimentMessage.EXPERIMENT_FINALLY) {
			finalAction(e);
		} else if (event instanceof ExpPrintMessage) {
			print(e, (ExpPrintMessage) event);
		} else if (event instanceof BaseExperimentCompleted) {
			BaseExperimentCompleted evt = (BaseExperimentCompleted) event;
			multiExperimentCompletedTask(e, evt.experimentRun, evt.results);
		} else {
			handleOther(e, event);
		}
	}

	default void handleOther(Experiment e, ExperimentEvent event) {
	}

	/**
	 * See parent interface {@link ExperimentListener}.
	 */
	@FunctionalInterface
	public interface HandleOtherListener extends ExperimentListener {
		@Override
		void handleOther(Experiment e, ExperimentEvent event);
	}

	default void print(Experiment e, ExpPrintMessage event) {
	}

	/**
	 * See parent interface {@link ExperimentListener}.
	 */
	@FunctionalInterface
	public interface PrintListener extends ExperimentListener {
		@Override
		void print(Experiment e, ExpPrintMessage event);
	}

	default void starting(Experiment e) {
	}

	/**
	 * See parent interface {@link ExperimentListener}.
	 */
	@FunctionalInterface
	public interface StartingListener extends ExperimentListener {
		@Override
		void starting(Experiment e);
	}

	default void initialized(Experiment e) {
	}

	/**
	 * See parent interface {@link ExperimentListener}.
	 */
	@FunctionalInterface
	public interface InitializedListener extends ExperimentListener {
		@Override
		void initialized(Experiment e);
	}

	default void beforeRun(Experiment e) {
	}

	/**
	 * See parent interface {@link ExperimentListener}.
	 */
	@FunctionalInterface
	public interface BeforeRunListener extends ExperimentListener {
		@Override
		void beforeRun(Experiment e);
	}

	default void runPerformed(Experiment e) {
	}

	/**
	 * See parent interface {@link ExperimentListener}.
	 */
	@FunctionalInterface
	public interface RunPerformedListener extends ExperimentListener {
		@Override
		void runPerformed(Experiment e);
	}

	default void afterRun(Experiment e) {
	}

	/**
	 * See parent interface {@link ExperimentListener}.
	 */
	@FunctionalInterface
	public interface AfterRunListener extends ExperimentListener {
		@Override
		void afterRun(Experiment e);
	}

	default void done(Experiment e) {
	}

	/**
	 * See parent interface {@link ExperimentListener}.
	 */
	@FunctionalInterface
	public interface DoneListener extends ExperimentListener {
		@Override
		void done(Experiment e);
	}

	default void produceResults(Experiment e, Map<String, Object> res) {
	}

	/**
	 * See parent interface {@link ExperimentListener}.
	 */
	@FunctionalInterface
	public interface ProduceResultsListener extends ExperimentListener {
		@Override
		void produceResults(Experiment e, Map<String, Object> res);
	}

	default void finishing(Experiment e, Map<String, Object> results) {
	}

	/**
	 * See parent interface {@link ExperimentListener}.
	 */
	@FunctionalInterface
	public interface FinishingListener extends ExperimentListener {
		@Override
		void finishing(Experiment e, Map<String, Object> results);
	}

	default void finished(Experiment e, Map<String, Object> results) {
	}

	/**
	 * See parent interface {@link ExperimentListener}.
	 */
	@FunctionalInterface
	public interface FinishedListener extends ExperimentListener {
		@Override
		void finished(Experiment e, Map<String, Object> results);
	}

	default void multiExperimentCompletedTask(Experiment baseExp, Experiment runExperiment,
			Map<String, Object> runResults) {
	}

	/**
	 * See parent interface {@link ExperimentListener}.
	 */
	@FunctionalInterface
	public interface MultiExperimentCompletedTaskListener extends ExperimentListener {
		@Override
		void multiExperimentCompletedTask(Experiment baseExp, Experiment runExperiment, Map<String, Object> runResults);
	}

	default void error(Experiment e, Throwable t) {
	}

	/**
	 * See parent interface {@link ExperimentListener}.
	 */
	@FunctionalInterface
	public interface ErrorListener extends ExperimentListener {
		@Override
		void error(Experiment e, Throwable t);
	}

	default void finalAction(Experiment e) {
	}

	/**
	 * See parent interface {@link ExperimentListener}.
	 */
	@FunctionalInterface
	public interface FinallyListener extends ExperimentListener {
		@Override
		void finalAction(Experiment e);
	}
}
