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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import jasima.core.simulation.SimEvent;
import jasima.core.simulation.Simulation;
import jasima.core.util.MsgCategory;
import jasima.core.util.observer.ObservableValue;

public class ConditionQueueTest {

	private Simulation sim;

	private Double timeTriggered;

	@Before
	public void setUp() throws Exception {
		System.out.println();
		System.out.println(this.getClass() + "    =======     setUp()    =======     ");

		sim = new Simulation();
		sim.setPrintLevel(MsgCategory.ALL);
		sim.addPrintListener(System.out::println);

		timeTriggered = null;
	}

	@Test
	public void testBasicFunctionality() {
		ObservableValue<Integer> ov = new ObservableValue<>(0);

		ConditionQueue holdUntil = new ConditionQueue(() -> ov.get() == 5, ov);

		boolean immediateResult = holdUntil.executeWhenTrue(() -> timeTriggered = sim.simTime());
		assertFalse(immediateResult);

		sim.schedulePeriodically(0.0, 1.0, SimEvent.EVENT_PRIO_NORMAL, () -> {
			ov.set(ov.get() + 1);
			return sim.simTime() < 10.0;
		});

		sim.performRun();
		assertEquals(10.0, sim.simTime(), 1e-6);

		assertEquals(4.0, timeTriggered, 1e-6);
	}

	@Test
	public void testInitiallyTrue() {
		ObservableValue<Integer> ov = new ObservableValue<>(0);

		ConditionQueue holdUntil = new ConditionQueue(() -> ov.get() == 0, ov);

		// this evaluated even before simulation is executed
		boolean immediateResult = holdUntil.executeWhenTrue(() -> timeTriggered = sim.simTime());
		assertTrue(immediateResult);

		assertEquals(0.0, timeTriggered, 1e-6);
	}
}
