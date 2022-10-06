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

public interface ComponentState {

//	default boolean canTransitionTo(ComponentState next) {
//		Set<ComponentState> validTransitions = validTransitions();
//		return validTransitions == null ? true : validTransitions.contains(next);
//	}
//
//	default Set<ComponentState> validTransitions() {
//		return ComponentStates.allowedTransitions(this);
//	}
//
//	default ComponentState transitionTo(ComponentState next) {
//		if (!canTransitionTo(next)) {
//			throw new IllegalStateException();
//		}
//		return next;
//	}

}
