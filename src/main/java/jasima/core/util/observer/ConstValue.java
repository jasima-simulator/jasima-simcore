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

/**
 * Technically an observable, but can't be changed. An attempt to set a new
 * value will result in an {@link UnsupportedOperationException}.
 * 
 * @author Torsten Hildebrandt
 *
 * @param <T>
 */
public class ConstValue<T> extends ObservableValue<T> {

	public ConstValue(T initialValue) {
		super(initialValue);
	}

	@Override
	public final void set(T newValue) {
		throw new UnsupportedOperationException("Can't change value of a ConstValue.");
	}
	

}
