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
package jasima.core.simulation;

import java.util.Map;

import jasima.core.simulation.Simulation.SimLifecycleEvent;
import jasima.core.simulation.Simulation.StdSimLifecycleEvents;
import jasima.core.util.observer.NotifierListener;

public interface SimLifecycleListener extends NotifierListener<Simulation, SimLifecycleEvent> {
	@Override
	default void inform(Simulation sim, SimLifecycleEvent event) {
		if (event == StdSimLifecycleEvents.INIT) {
			init();
		} else {
			handleOther(sim, event);
		}
	}

	default void init() {
	}

	@FunctionalInterface
	interface SimInitialized extends SimLifecycleListener {
		@Override
		void init();
	}

	default void simStart() {
	}

	@FunctionalInterface
	public interface SimStartListener extends SimLifecycleListener {
		@Override
		void simStart();
	}

	default void resetStats() {
	}

	@FunctionalInterface
	public interface ResetStatsListener extends SimLifecycleListener {
		@Override
		void resetStats();
	}

	default void simEnd() {
	}

	@FunctionalInterface
	public interface SimEndListener extends SimLifecycleListener {
		@Override
		void simEnd();
	}

	default void done() {
	}

	@FunctionalInterface
	public interface DoneListener extends SimLifecycleListener {
		@Override
		void done();
	}

	default void produceResults(Map<String, Object> resultMap) {
	}

	@FunctionalInterface
	public interface ResultsListener extends SimLifecycleListener {
		@Override
		void produceResults(Map<String, Object> resultMap);
	}

	default void handleOther(Simulation sim, SimLifecycleEvent event) {
	}

	@FunctionalInterface
	public interface OtherListener extends SimLifecycleListener {
		@Override
		void handleOther(Simulation c, SimLifecycleEvent msg);
	}
}