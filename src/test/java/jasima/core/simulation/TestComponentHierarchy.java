package jasima.core.simulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.Test;
import org.junit.rules.Timeout;

import jasima.core.simulation.Simulation.SimulationFailed;
import jasima.core.simulation.Simulation.StdSimLifecycleEvents;

public class TestComponentHierarchy {
//	@Rule
	public Timeout globalTimeout = new Timeout(5000);

	@Test
	public void testGetComponentByName() {
		SimComponentContainerBase c = createTree();

		assertEquals("sub1", c.getChildByName("sub1").getName());
		assertEquals("b", ((SimComponentContainer) c.getChildByName("sub2")).getChildByName("b").getName());
	}

	@Test
	public void testGetComponentByNameNonExisting() {
		SimComponentContainerBase c = createTree();
		assertEquals(null, c.getChildByName("subXXX"));
	}

	@Test
	public void testGetComponentByHierarchicalNameSub() {
		SimComponentContainerBase c = createTree();

		SimComponent cmp = c.getByHierarchicalName("main.sub1");
		assertEquals("sub1", cmp.getName());
		assertEquals("main.sub1", cmp.getHierarchicalName());
	}

	@Test
	public void testGetComponentByHierarchicalNameSubSub() {
		SimComponentContainerBase c = createTree();

		SimComponent cmp = c.getByHierarchicalName("main.sub2.b");
		assertEquals("b", cmp.getName());
		assertEquals("main.sub2.b", cmp.getHierarchicalName());
	}

	@Test
	public void testGetComponentByHierarchicalNameFromSimulation() {
		SimComponentContainerBase c = createTree();

		SimContext.simulationOf(sim -> {
			sim.addComponent(c);

			SimComponent cmp = sim.getComponentByHierarchicalName(".main.sub2.b");
			assertEquals("b", cmp.getName());
			assertEquals(".main.sub2.b", cmp.getHierarchicalName());
		});
	}

	@Test
	public void testGetComponentByHierarchicalNameFromSimExperiment() {
		SimulationExperiment se = new SimulationExperiment(sim -> {
			SimComponent cmp = sim.getComponentByHierarchicalName(".main.sub2.b");
			assertEquals("b", cmp.getName());
			assertEquals(".main.sub2.b", cmp.getHierarchicalName());
		});
		se.setModelRoot(createTree());
		se.runExperiment();
	}

	private SimComponentContainerBase createTree() {
		SimComponentContainerBase c = new SimComponentContainerBase("main");
		c.addChild(new SimComponentBase("sub1"));
		SimComponentContainerBase sub2 = new SimComponentContainerBase("sub2");
		sub2.addChild(new SimComponentBase("a")).addChild(new SimComponentBase("b"));
		c.addChild(sub2);
		return c;
	}

	// test in all 3 contexts for SimOperations

	@Test
	public void testAddComponentShouldWorkInComponent() {
		SimComponentContainerBase c = new SimComponentContainerBase("main");
		c.addComponent(new SimComponentBase("sub1"));
		SimComponentContainerBase sub2 = new SimComponentContainerBase("sub2");
		sub2.addComponent(new SimComponentBase("a"), new SimComponentBase("b"));
		c.addComponent(sub2);

		SimComponent cmp = c.getByHierarchicalName("main.sub2.b");
		assertEquals("b", cmp.getName());
		assertEquals("main.sub2.b", cmp.getHierarchicalName());
	}

	@Test
	public void testAddComponentDynamicallyFromSimContext() {
		SimComponentContainer c = new SimComponentContainerBase("main", new EventTracer("t"));

		Map<String, Object> res = SimContext.simulationOf(sim -> {
			SimContext.waitFor(1.0);
			sim.addComponent(c);

			EventTracer et = (EventTracer) sim.getComponentByHierarchicalName(".main.t");
			assertEquals("LifecycleCalls", Arrays.asList("INIT", "SIM_START"), et.getLifecycleCalls());
			assertEquals("EventCalls", Arrays.asList("INIT", "SIM_START"), et.getEventCalls());
		});

		assertEquals("LifecycleCalls", Arrays.asList("INIT", "SIM_START", "SIM_END", "DONE", "PRODUCE_RESULTS"),
				res.get(".main.t.lifecycleCalls"));
		assertEquals("EventCalls", Arrays.asList("INIT", "SIM_START", "SIM_END", "DONE", "ProduceResultsEvent"),
				res.get(".main.t.eventCalls"));
	}

	@Test(expected = IllegalStateException.class)
	public void componentAccessingSimOperation__notInSimContext__shouldRaiseException() {
		SimComponentBase c = new SimComponentBase();

		assertTrue("sim", c.getSim() == null);
		// SimOperation tries to forward to simulation
		c.scheduleAt(0.0, 0, () -> {
		});
	}

	@Test(expected = IllegalStateException.class)
	public void simExperimentAccessingSimOperation__notInSimContext__shouldRaiseException() {
		SimulationExperiment se = new SimulationExperiment();

		assertTrue("sim", se.getSim() == null);
		// SimOperation tries to forward to simulation
		se.scheduleAt(0.0, 0, () -> {
		});
	}

	@Test(expected = UnsupportedOperationException.class)
	public void simpleComponent__shouldNotSupportAddComponent() {
		SimComponentBase c = new SimComponentBase();
		c.addComponent(new SimComponentBase());
	}

	@Test
	public void testComponentEventsBasic() {
		SimulationExperiment se = new SimulationExperiment(sim -> {
			SimComponent container = Objects.requireNonNull(sim.getComponentByHierarchicalName(".base"));
			container.addComponent(new EventTracer("t"));
		});
		se.setModelRoot(new SimComponentContainerBase("base"));

		Map<String, Object> res = se.runExperiment();

		assertEquals("LifecycleCalls", Arrays.asList("INIT", "SIM_START", "SIM_END", "DONE", "PRODUCE_RESULTS"),
				res.get(".base.t.lifecycleCalls"));
		assertEquals("EventCalls", Arrays.asList("INIT", "SIM_START", "SIM_END", "DONE", "ProduceResultsEvent"),
				res.get(".base.t.eventCalls"));
	}

	@Test
	public void testAddComponentDynamicallyFromSimExperiment() {
		SimulationExperiment se = new SimulationExperiment(sim -> {
			SimComponent cmp = sim.getComponentByHierarchicalName(".main.sub2.b");
			assertEquals("b", cmp.getName());
			assertEquals(".main.sub2.b", cmp.getHierarchicalName());
		});
		se.setModelRoot(createTree());
		se.runExperiment();
	}

	static class EventTracer extends SimComponentBase {
		private List<String> lifecycleCalls = new ArrayList<>();
		private List<String> eventCalls = new ArrayList<>();

		public EventTracer(String name) {
			super(name);
			addListener(new SimComponentLifecycleListener() {
				@Override
				public void inform(SimComponent o, SimComponentEvent msg) {
					eventCalls.add(msg.toString());
				}
			});
		}

		@Override
		public void init() {
			lifecycleCalls.add("INIT");
		}

		@Override
		public void simStart() {
			lifecycleCalls.add("SIM_START");
		}

		@Override
		public void resetStats() {
			lifecycleCalls.add("RESET_STATS");
		}

		@Override
		public void simEnd() {
			lifecycleCalls.add("SIM_END");
		}

		@Override
		public void done() {
			lifecycleCalls.add("DONE");
		}

		@Override
		public void produceResults(Map<String, Object> res) {
			lifecycleCalls.add("PRODUCE_RESULTS");

			res.put(getHierarchicalName() + ".lifecycleCalls", new ArrayList<>(lifecycleCalls));
			res.put(getHierarchicalName() + ".eventCalls", new ArrayList<>(eventCalls));
		}

		public List<String> getLifecycleCalls() {
			return lifecycleCalls;
		}

		public List<String> getEventCalls() {
			return eventCalls;
		}

	}

	@Test(expected = IllegalStateException.class)
	public void component__initializedTwice__raiseException() {
		SimComponentBase c = new SimComponentBase();
		c.inform(null, StdSimLifecycleEvents.INIT);
		c.inform(null, StdSimLifecycleEvents.INIT);
	}

	@Test
	public void component__alreadyInitializedIsAddedToContainer__raiseException() {
		SimComponentBase c = new SimComponentBase();
		c.init();

		try {
			SimContext.simulationOf(sim -> {
				SimComponentContainerBase b = new SimComponentContainerBase("test");
				sim.addComponent(b);
				b.addChild(c);
			});
		} catch (SimulationFailed sf) {
			assertTrue(sf.getCause() instanceof IllegalStateException);
		}
	}

}
