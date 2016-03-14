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
import jasima.core.experiment.Experiment.ExpPrintEvent;
import jasima.core.experiment.Experiment.ExperimentEvent;
import jasima.core.util.observer.NotifierService;
import jasima.core.util.observer.Subscriber;

/**
 * This class can be used as a base class for experiment listeners. It delegates
 * all events of {@link Experiment} to separate methods.
 * 
 * @author Torsten Hildebrandt
 */
public abstract class ExperimentListenerBase implements Subscriber, Cloneable {

	public ExperimentListenerBase() {
		super();
	}

	@Override
	public void register(NotifierService s) {
		s.addSubscription(ExperimentEvent.class, this);
	}

	@Override
	public void inform(Object e, Object event) {
		if (event == Experiment.EXPERIMENT_STARTING) {
			starting((Experiment) e);
		} else if (event == Experiment.EXPERIMENT_INITIALIZED) {
			initialized((Experiment) e);
		} else if (event == Experiment.EXPERIMENT_BEFORE_RUN) {
			beforeRun((Experiment) e);
		} else if (event == Experiment.EXPERIMENT_AFTER_RUN) {
			afterRun((Experiment) e);
		} else if (event == Experiment.EXPERIMENT_DONE) {
			done((Experiment) e);
		} else if (event == Experiment.EXPERIMENT_COLLECT_RESULTS) {
			Experiment exp = (Experiment) e;
			produceResults(exp, exp.results);
		} else if (event == Experiment.EXPERIMENT_FINISHING) {
			Experiment exp = (Experiment) e;
			finishing(exp, exp.results);
		} else if (event == Experiment.EXPERIMENT_FINISHED) {
			Experiment exp = (Experiment) e;
			finished(exp, exp.results);
		} else if (event instanceof ExpPrintEvent) {
			print((Experiment) e, (ExpPrintEvent) event);
		} else if (event instanceof BaseExperimentCompleted) {
			BaseExperimentCompleted evt = (BaseExperimentCompleted) event;
			multiExperimentCompletedTask((Experiment) e, evt.experimentRun, evt.results);
		} else {
			handleOther(e, event);
		}
	}

	protected void handleOther(Object e, Object event) {
	}

	protected void print(Experiment e, ExpPrintEvent event) {
	}

	protected void starting(Experiment e) {
	}

	protected void initialized(Experiment e) {
	}

	protected void beforeRun(Experiment e) {
	}

	protected void afterRun(Experiment e) {
	}

	protected void done(Experiment e) {
	}

	protected void produceResults(Experiment e, Map<String, Object> res) {
	}

	protected void finishing(Experiment e, Map<String, Object> results) {
	}

	protected void finished(Experiment e, Map<String, Object> results) {
	}

	protected void multiExperimentCompletedTask(Experiment baseExp, Experiment runExperiment,
			Map<String, Object> runResults) {
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
