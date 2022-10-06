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

import static jasima.core.simulation.generic.Q.enter;
import static jasima.core.simulation.generic.Q.leave;

import jasima.core.simulation.SimContext;
import jasima.core.simulation.SimProcess;
import jasima.core.simulation.SimProcess.MightBlock;

public class Resource {

	private Q<SimProcess<?>> seizedBy;

	public Resource(String string, int numResources) {
		seizedBy = new Q<>();
		setCapacity(numResources);
	}

	public Resource(String string) {
		this(string, 1);
	}

	public void seize() throws MightBlock {
		enter(seizedBy);
	}

	public boolean trySeize() {
		SimProcess<?> p = SimContext.currentProcess();
		return seizedBy.tryPut(p);
	}

	public void release() {
		leave(seizedBy);
	}

	public void seize(int numResources) throws MightBlock {
		if (numResources<1)
			throw new IllegalArgumentException();
		
		for (int i=0; i<numResources; i++) {
			seize();
		}
	}

	public boolean trySeize(int numResources) {
		if (numResources<1)
			throw new IllegalArgumentException();

		if (numAvailable()<numResources)
			return false;
		
		for (int i=0; i<numResources; i++) {
			trySeize();
		}
		return true;
	}

	public void release(int numResources) {
		if (numResources<1)
			throw new IllegalArgumentException();
		
		for (int i=0; i<numResources; i++) {
			release();
		}
	}

	public int numAvailable() {
		return seizedBy.numAvailable();
	}

	public int getCapacity() {
		return seizedBy.getCapacity();
	}

	public void setCapacity(int numResources) {
		seizedBy.setCapacity(numResources);
	}

}
