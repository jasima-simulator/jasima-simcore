package jasima.core.simulation;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import jasima.core.simulation.SimLifecycleListener.SimInitialized;
import jasima.core.simulation.Simulation.SimLifecycleEvent;

public class TestComponentInit {

	enum ExtLifecycleEvent implements Simulation.SimLifecycleEvent {
		INIT2;
	}

	Simulation s;

	List<String> callOrder;

	@Before
	public void setup() {
		s = new Simulation();

		SimComponentContainerBase container = new SimComponentContainerBase("main") {
			@Override
			public void init() {
				callOrder.add("main.init");
			}
		};
		SimComponentBase sub = new SimComponentBase("sub1") {
			@Override
			public void init() {
				callOrder.add("sub.init");
			}

			@Override
			public void simStart() {
				callOrder.add("sub.simStart");
			}

			@Override
			public void resetStats() {
				callOrder.add("sub.resetStats");
			}

			@Override
			public void simEnd() {
				callOrder.add("sub.simEnd");
			}

			@Override
			public void done() {
				callOrder.add("sub.done");
			}

			@Override
			public void produceResults(Map<String, Object> res) {
				callOrder.add("sub.results");
			}

			@Override
			public void handleOther(Simulation sim, SimLifecycleEvent event) {
				callOrder.add("sub.ho");
			}

		};
		container.addChild(sub);

		s.addComponent(container);

		callOrder = new ArrayList<>();
	}

	@Test
	public void testStandardLifecycleEvents() {
		s.performRun();
		assertEquals("initOrder", Arrays.asList("main.init", "sub.init", "sub.simStart", "sub.resetStats", "sub.simEnd",
				"sub.done", "sub.results"), callOrder);
	}

	@Test
	public void testNonStandardLifecycleEvent() {
		// this propagates after init was executed for all components
		s.addListener(SimInitialized.class, () -> s.fire(ExtLifecycleEvent.INIT2));

		s.performRun();

		assertEquals("initOrder", Arrays.asList("main.init", "sub.init", /**/"sub.ho"/**/, "sub.simStart",
				"sub.resetStats", "sub.simEnd", "sub.done", "sub.results"), callOrder);
	}

	@Test
	public void testAddComponentDuringInit() {
		AtomicInteger outerInits = new AtomicInteger(0);
		AtomicInteger innerInits = new AtomicInteger(0);

		s.addComponent(new SimComponentContainerBase("outer") {
			@Override
			public void init() {
				outerInits.incrementAndGet();
				addChild(new SimComponentBase("inner") {
					@Override
					public void init() {
						innerInits.incrementAndGet();
					}
				});
			}
		});

		s.performRun();

		assertEquals("innerInits", 1, innerInits.get());
		assertEquals("outerInits", 1, outerInits.get());
	}

}
