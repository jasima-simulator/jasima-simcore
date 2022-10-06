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
package examples.processes.servers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import jasima.core.simulation.SimContext;
import jasima.core.simulation.generic.Resource;

public class TestResource {

	Resource r = new Resource("r");

	@Test
	public void testSeizeRelease() {
		AtomicBoolean lifecycleFinished = new AtomicBoolean(false);
		SimContext.simulationOf(sim -> {
			double startTime = sim.simTime();

			assertEquals("available at start", 1, r.numAvailable());
			r.seize();
			assertEquals("available after seize", 0, r.numAvailable());
			r.release();
			assertEquals("available after release", 1, r.numAvailable());

			assertEquals("no blocking", 0.0, sim.simTime() - startTime, 1e-6);
			lifecycleFinished.set(true);
		});
		assertTrue("lifecycle finished", lifecycleFinished.get());
	}

	@Test
	public void testSeizeReleaseNoBlocking() {
		AtomicBoolean lifecycleFinished = new AtomicBoolean(false);
		SimContext.simulationOf(sim -> {
			double startTime = sim.simTime();

			assertEquals("available at start", 1, r.numAvailable());
			assertTrue("first seize", r.trySeize());
			assertFalse("2nd seize", r.trySeize());
			
			assertEquals("available after seize", 0, r.numAvailable());
			
			r.release();
			assertEquals("available after release", 1, r.numAvailable());

			assertEquals("no blocking", 0.0, sim.simTime() - startTime, 1e-6);
			lifecycleFinished.set(true);
		});
		assertTrue("lifecycle finished", lifecycleFinished.get());
	}

}
