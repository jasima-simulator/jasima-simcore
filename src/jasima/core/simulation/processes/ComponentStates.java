package jasima.core.simulation.processes;

import static jasima.core.util.i18n.I18n.defFormat;
import static java.util.Collections.unmodifiableSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ComponentStates {

	static Map<ComponentState, Set<ComponentState>> allowedTransitions = new HashMap<>();

	public static void allowTransition(ComponentState from, ComponentState next) {
		Set<ComponentState> succs = allowedTransitions.get(from);
		if (succs == null) {
			succs = new HashSet<>();
			allowedTransitions.put(from, succs);
		}
		succs.add(next);
	}

	public static Set<ComponentState> allowedTransitions(ComponentState from) {
		Set<ComponentState> s = allowedTransitions.get(from);
		return s != null ? unmodifiableSet(s) : null;
	}

	public static ComponentState requireAllowedState(ComponentState state,
			Set<? extends ComponentState> allowedStates) {
		if (!allowedStates.contains(state)) {
			throw new IllegalStateException(defFormat("Invalid state %s, allowed states: %s.", state, allowedStates));
		}
		return state;
	}

	/**
	 * Prevent instantiation and sub-classing.
	 */
	private ComponentStates() {
	}

}
