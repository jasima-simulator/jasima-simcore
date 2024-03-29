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
package jasima.core.util;

import java.util.Objects;

import jasima.core.simulation.SimContext;
import jasima.core.simulation.SimEvent;
import jasima.core.simulation.Simulation;

public class StateMachine<E extends Enum<E>> {

	Class<E> clazz;
	E currentState;
	E lastState;
	private long versionId = Long.MIN_VALUE;

	StateMachine<?> sub;

	public StateMachine(E initialState, E terminalState) {
		super();
		Objects.requireNonNull(initialState);
		Objects.requireNonNull(terminalState);
		if (initialState.equals(terminalState)) {
			throw new IllegalArgumentException();
		}
		clazz = initialState.getDeclaringClass();
		currentState = lastState = initialState;
	}

	public void transition(E newState) {
		transitionIn(0.0, newState, null);
	}

	public void transition(E newState, Runnable onTransition) {
		transitionIn(0.0, newState, onTransition);
	}

	public void transitionNow(E newState) {
		transitionNow(newState, null);
	}

	public void transitionNow(E newState, Runnable onTransition) {
		runNonNull(onExit(currentState));

		lastState = currentState;
		currentState = newState;
		versionId++;
		
		runNonNull(onTransition);
		
		runNonNull(onEnter(newState));
	}

	private Runnable onEnter(E newState) {
		// TODO Auto-generated method stub
		return null;
	}

	private Runnable onExit(E currentState2) {
		// TODO Auto-generated method stub
		return null;
	}

	private static void runNonNull(Runnable r) {
		if (r!=null) {
			r.run();
		}
	}
	
	public void transitionIn(double simTimeDuration, E newState) {
		transitionIn(simTimeDuration, newState, null);
	}

	public void transitionIn(double simTimeDuration, E newState, Runnable onTransition) {
		Simulation sim = SimContext.requireSimContext();
		sim.scheduleIn(simTimeDuration, SimEvent.EVENT_PRIO_NORMAL, () -> transitionNow(newState, onTransition));
	}

	public long versionId() {
		return versionId;
	}

}
