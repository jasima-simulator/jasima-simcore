package jasima.core.simulation.processes;

import java.util.Set;

import jasima.core.util.Util;

public interface ComponentState {

	default boolean canTransitionTo(ComponentState next) {
		Set<ComponentState> validTransitions = validTransitions();
		return validTransitions == null ? true : validTransitions.contains(next);
	}

	default Set<ComponentState> validTransitions() {
		return ComponentStates.allowedTransitions(this);
	}

	default ComponentState transitionTo(ComponentState next) {
		if (!canTransitionTo(next)) {
			throw new IllegalStateException();
		}
		return next;
	}

}
