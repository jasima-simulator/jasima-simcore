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

import jasima.core.util.observer.Notifier;
import jasima.core.util.observer.NotifierListener;

/**
 * TypeHint for the compiler. This is required for, e.g.,
 * {@link Notifier#addListener(TypeHint, NotifierListener)}
 * to add a generic listener.
 */
public final class TypeHint<T> {
//	 * Represents a super type token to be able to access the generic type
//	 * information at runtime.
//	 * 
//	 * @author Torsten Hildebrandt
//	 *
//	 * @param <T> The (usually generic) type to represent.
//	private final Type type;

	public TypeHint() {
//		ParameterizedType superclass = (ParameterizedType) getClass().getGenericSuperclass();
//		type = superclass.getActualTypeArguments()[0];
	}

}