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
package jasima.core.util.observer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import jasima.core.util.TypeHint;

/**
 * Notifier inform {@link NotifierListener}s about events. This implements a
 * version of the Observer-pattern using Java Generics. Implementation can be
 * delegated via {@link #notifierImpl()}.
 * <p>
 * Usually the default implementation provided by {@link NotifierImpl} can be
 * used.
 * 
 * @author Torsten Hildebrandt
 */
public interface Notifier<SUBJECT extends Notifier<SUBJECT, MESSAGE>, MESSAGE> {

	default int numListener() {
		return notifierImpl().numListener();
	}

	default <T extends NotifierListener<SUBJECT, MESSAGE>> T addListener(T l) {
		return notifierImpl().addListener(l);
	}

	default <T extends NotifierListener<SUBJECT, MESSAGE>> void addListener(Class<T> eventType, T eventHandler) {
		notifierImpl().addListener(eventType, eventHandler);
	}

	default <T extends NotifierListener<SUBJECT, MESSAGE>> void addListener(TypeHint<T> eventType, T eventHandler) {
		notifierImpl().addListener(eventType, eventHandler);
	}

	default void removeCurrentListener() {
		notifierImpl().removeCurrentListener();
	}

	default boolean removeListener(NotifierListener<SUBJECT, MESSAGE> l) {
		return notifierImpl().removeListener(l);
	}

	default NotifierListener<SUBJECT, MESSAGE> getListener(int idx) {
		return notifierImpl().getListener(idx);
	}

	default void fire(MESSAGE msg) {
		notifierImpl().fire(msg);
	}

	default<T extends NotifierListener<SUBJECT, MESSAGE>> void fire(TypeHint<T> hint, Consumer<T> forwarder) {
		for (int i=0; i<numListener(); i++) {
			T l = (T) getListener(i);
			forwarder.accept(l);
		}
	}

	final static Map<Notifier<?, ?>, NotifierImpl<?, ?>> adapters = new HashMap<>();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	default Notifier<SUBJECT, MESSAGE> notifierImpl() {
		NotifierImpl<?, ?> adapter = adapters.get(this);
		if (adapter == null) {
			adapter = new NotifierImpl(this);
			adapters.put(this, adapter);
		}
		return (Notifier<SUBJECT, MESSAGE>) adapter;
	}

}
