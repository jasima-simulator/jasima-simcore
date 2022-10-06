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
package jasima.core.random.discrete;

import jasima.core.random.continuous.DblSequence;

/**
 * A stream of integer numbers, usually the sequence is produced by a pseudo
 * random number generator. This is an abstract base class.
 * 
 * @author Torsten Hildebrandt
 */
public abstract class IntSequence extends DblSequence {

	private static final long serialVersionUID = -4799011636085252707L;

	public IntSequence() {
		super();
	}

	public abstract int nextInt();

	@Override
	public final double nextDbl() {
		return nextInt();
	}

	@Override
	public IntSequence clone() {
		return (IntSequence) super.clone();
	}

}
