package jasima.core.util;

import static jasima.core.util.i18n.I18n.defFormat;

import java.util.EnumSet;
import java.util.Set;

public final class ComponentStates {

//	static Map<ComponentState, Set<ComponentState>> allowedTransitions = new HashMap<>();
//
//	public static void allowTransition(ComponentState from, ComponentState next) {
//		Set<ComponentState> succs = allowedTransitions.get(from);
//		if (succs == null) {
//			succs = new HashSet<>();
//			allowedTransitions.put(from, succs);
//		}
//		succs.add(next);
//	}
//
//	public static Set<ComponentState> allowedTransitions(ComponentState from) {
//		Set<ComponentState> s = allowedTransitions.get(from);
//		return s != null ? unmodifiableSet(s) : null;
//	}

	public static <E extends Enum<E>> E requireAllowedState(E state, E allowed1) {
		return requireAllowedState(state, allowed1, null, null);
	}

	public static <E extends Enum<E>> E requireAllowedState(E state, E allowed1, E allowed2) {
		return requireAllowedState(state, allowed1, allowed2, null);
	}

	public static <E extends Enum<E>> E requireAllowedState(E state, E allowed1, E allowed2, E allowed3) {
		if (state != allowed1 && state != allowed2 && state != allowed3) {
			raiseStateError(state, createSet(allowed1, allowed2, allowed3));
		}
		return state;
	}

	@SafeVarargs
	public static <E extends Enum<E>> E requireAllowedState(E state, E allowed1, E... allowedOther) {
		if (state == allowed1) {
			return state;
		}

		for (int i = 0; i < allowedOther.length; i++) {
			if (state == allowedOther[i]) {
				return state;
			}
		}

		raiseStateError(state, createSet(allowed1, allowedOther));
		throw new AssertionError(); // will never happen
	}

	public static <E extends Enum<E>> E requireAllowedState(E state, Set<E> allowedStates) {
		if (!allowedStates.contains(state)) {
			raiseStateError(state, allowedStates);
		}
		return state;
	}

	/**
	 * Same as {@link EnumSet#of(Enum, Enum...), just filtering out any {@code null}
	 * values.
	 */
	@SafeVarargs
	private static <E extends Enum<E>> EnumSet<E> createSet(E allowed1, E... allowedOther) {
		EnumSet<E> res = EnumSet.noneOf(allowed1.getDeclaringClass());
		res.add(allowed1);
		for (int i = 0; i < allowedOther.length; i++) {
			if (allowedOther[i] != null) {
				res.add(allowedOther[i]);
			}
		}
		return res;
	}

	private static <E extends Enum<E>> void raiseStateError(E state, Set<E> allowedStates) {
		throw new IllegalStateException(defFormat("Invalid state %s, allowed states: %s.", state, allowedStates));
	}

	/**
	 * Prevent instantiation and sub-classing.
	 */
	private ComponentStates() {
	}

}
