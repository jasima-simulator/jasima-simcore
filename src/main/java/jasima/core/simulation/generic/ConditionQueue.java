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
package jasima.core.simulation.generic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import jasima.core.util.observer.DerivedObservable;
import jasima.core.util.observer.ObservableValue;
import jasima.core.util.observer.ObservableValue.EventType;
import jasima.core.util.observer.ObservableValue.ObservableListener;

/**
 * This class allows basing the execution of certain actions on some condition
 * to, e.g., create a holdUntil/waitUntil statement in a simulation model.
 * Condition queues can be constructed by either specifying a boolean expression
 * (and its immediate dependencies) or directly by an {@link ObservableValue}
 * (which can be a plain value or a @link {@link DerivedObservable}).
 * <p>
 * Once created, the method {@link #executeWhenTrue(Runnable)} can be used to
 * run some code when the condition becomes true.
 * 
 * @author Torsten Hildebrandt
 * @see ObservableValue
 */
public class ConditionQueue {

	private final ObservableValue<Boolean> condition;

	private final List<Runnable> actions;

	private final ObservableListener<Boolean> changeListener;
	private boolean listenerInstalled;

	/**
	 * Creates a new condition queue depending on a certain condition. Internally
	 * the parameters to this constructor are used to create a
	 * {@link DerivedObservable<Boolean>} to get notifications whenever the
	 * condition is changing.
	 * 
	 * @param boolExpression   A boolean expression containing other
	 *                         {@link ObservableValue}s.
	 * @param exprDependencies All {@link ObservableValue}s used in the expression.
	 */
	public ConditionQueue(Supplier<Boolean> boolExpression, ObservableValue<?>... exprDependencies) {
		this(new DerivedObservable<>(boolExpression, exprDependencies));
	}

	/**
	 * Create a new condition queue based on the value of on {@link ObservableValue}
	 * (this can also be a {@link DerivedObservable}).
	 * 
	 * @param condition The observable value of type boolean to use in order to
	 *                  perform actions.
	 */
	public ConditionQueue(ObservableValue<Boolean> condition) {
		super();

		this.condition = Objects.requireNonNull(condition);
		this.changeListener = this::onConditionChanged;
		this.actions = new ArrayList<>();
		this.listenerInstalled = false;
	}

	/**
	 * Executes the {@link Runnable} provided as a parameter exactly once when the
	 * condition becomes true. If the condition is already true when this method is
	 * called, then 'action' is run immediately and true will be returned. Otherwise
	 * {@code false} will be returned and 'action' will be stored in an internal
	 * list to be executed when the condition becomes {@code true}.
	 * 
	 * @param action The action to run. Mustn't be null.
	 * @return The value of the condition when this method is executed.
	 */
	public boolean executeWhenTrue(Runnable action) {
		Objects.requireNonNull(action);

		boolean result = condition.get();

		actions.add(action);

		if (!listenerInstalled) {
			condition.addWeakListener(changeListener); // TODO: fixme
			listenerInstalled = true;
		}

		if (result) {
			runActions();
		}

		return result;
	}

	/**
	 * Returns the number of actions currently waiting on the condition.
	 */
	public int numActions() {
		return actions.size();
	}

	/**
	 * Called by {@literal condition} whenever its value has changed or could have
	 * changed.
	 */
	private void onConditionChanged(ObservableValue<Boolean> sender, EventType event) {
		assert EventType.VALUE_CHANGED.equals(event) || EventType.MIGHT_HAVE_CHANGED.equals(event);

		if (condition.get()) {
			runActions();
		}
	}

	/**
	 * Runs all actions waiting on the condition. Afterwards they are removed from
	 * the list. The condition is checked before each action is run, so if the
	 * condition changes because of the execution of an action, then other actions
	 * waiting on the same condition will only run while the condition is true.
	 */
	private void runActions() {
		Iterator<Runnable> it = actions.iterator();
		while (it.hasNext() && condition.get()) {
			Runnable action = it.next();
			action.run();

			it.remove();
		}
	}

}
