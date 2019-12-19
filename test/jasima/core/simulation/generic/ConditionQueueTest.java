package jasima.core.simulation.generic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import jasima.core.simulation.SimEvent;
import jasima.core.simulation.Simulation;
import jasima.core.simulation.generic.ConditionQueue;
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
