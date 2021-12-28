package jasima.core.simulation;

import static jasima.core.simulation.SimEvent.EVENT_PRIO_MIN;
import static jasima.core.simulation.SimEvent.EVENT_PRIO_NORMAL;
import static org.junit.Assert.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class TestSimulationBasics {

	@Test(expected = IllegalArgumentException.class)
	public void testInitialSimTime() {
		Simulation sim = new Simulation();
		sim.addPrintListener(System.out::println);
		sim.scheduleAt(0.0, EVENT_PRIO_NORMAL, TestSimulationBasics::dummyHandler);
		sim.setInitialSimTime(100);

		// method under test
		sim.performRun();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOldEventInInit() {
		Simulation sim = new Simulation();
		sim.addPrintListener(System.out::println);
		sim.setInitialSimTime(-100);

		sim.init();

		// should raise exception
		sim.scheduleAt(-101.0, EVENT_PRIO_NORMAL, TestSimulationBasics::dummyHandler);
	}

	@Test
	public void testSimtimeInPast() {
		Simulation sim = new Simulation();
		sim.addPrintListener(System.out::println);
		sim.setInitialSimTime(-100);

		sim.init();

		// this should work
		sim.scheduleAt(-100.0, EVENT_PRIO_NORMAL, TestSimulationBasics::dummyHandler);
	}

	@Test
	public void testDefaultInitialSimPriority() {
		Simulation sim = new Simulation();
		sim.addPrintListener(System.out::println);
		sim.setMainProcessActions(() -> {
			assertEquals("initialPriority", EVENT_PRIO_NORMAL, sim.currentPrio());
		});

		sim.performRun();

		assertEquals("initialPriority", EVENT_PRIO_NORMAL, sim.currentPrio());
		assertEquals("simTime", 0.0, sim.simTime(), 1e-6);
	}

	@Test
	public void testInitialSimPriority() {
		Simulation sim = new Simulation();
		sim.addPrintListener(System.out::println);
		sim.setInitialEventPriority(EVENT_PRIO_MIN);
		
		sim.setMainProcessActions(() -> {
			assertEquals("initialPriority", EVENT_PRIO_MIN, sim.currentPrio());
		});

		sim.performRun();

		assertEquals("initialPriority", EVENT_PRIO_MIN, sim.currentPrio());
		assertEquals("simTime", 0.0, sim.simTime(), 1e-6);
	}

	@Test
	public void testSimLengthPrioIsLast() {
		AtomicInteger counter = new AtomicInteger(0);

		Simulation sim = new Simulation();
		sim.addPrintListener(System.out::println);
		sim.setSimulationLength(5.0);
		// first 2 events below get executed before simEnd, because they have a higher
		// priority
		sim.scheduleAt(5.0, 0, () -> counter.incrementAndGet());
		sim.scheduleAt(5.0, EVENT_PRIO_MIN - 1, () -> counter.incrementAndGet());
		// next event has same prio as simEnd, but is executed before it (because
		// default is FIFO order and simEnd event is scheduled in init() executed as
		// part of performRun())
		sim.scheduleAt(5.0, EVENT_PRIO_MIN, () -> counter.incrementAndGet());
		// not executed because after simEnd time
		sim.scheduleAt(5.1, 0, () -> counter.incrementAndGet());
		sim.setMainProcessActions(() -> {
			// not executed. Has the same time/prio as simEnd, but is main process is
			// executed after simEnd
			sim.scheduleAt(5.0, EVENT_PRIO_MIN, () -> counter.incrementAndGet());
		});

		sim.performRun();

		assertEquals("simTime", 5.0, sim.simTime(), 1e-6);
		assertEquals("counter", 3, counter.get());
	}

	@Test
	public void testEmptySimulationFromSimRunnable() {
		Map<String, Object> res = Simulation.of(() -> {
		});
		assertEquals("simTime", 0.0, (Double) res.get("simTime"), 1e-6);
	}

	@Test
	public void testEmptySimulationFromSimAction() {
		Map<String, Object> res = Simulation.of(sim -> {
		});
		assertEquals("simTime", 0.0, (Double) res.get("simTime"), 1e-6);
	}

	@Test
	public void testEmptySimulationNoComponents() {
		Map<String, Object> res = Simulation.of(/* empty component array */);
		assertEquals("simTime", 0.0, (Double) res.get("simTime"), 1e-6);
	}

	@Test(expected = IllegalStateException.class)
	public void testUndefinedSimTimeShouldRaiseException() {
		Simulation sim = new Simulation();
		sim.simTime();
	}

	@Test(expected = IllegalStateException.class)
	public void testUndefinedCurrentPrioShouldRaiseException() {
		Simulation sim = new Simulation();
		sim.currentPrio();
	}

	@Test
	public void testTimeConversion() {
		Simulation sim = new Simulation();
		sim.addPrintListener(System.out::println);
		sim.scheduleAt(360.0, EVENT_PRIO_NORMAL, TestSimulationBasics::dummyHandler);

		Map<String, Object> res = sim.performRun();
		System.out.println(res.toString());

		// default is to assume time to be in minutes, i.e. simtime is at 360.0
		// minutes
		Instant instant = sim.simTimeAbs();
		LocalDateTime exp = LocalDateTime.of(Year.now(Clock.systemUTC()).getValue(), 1, 1, 6, 0);
		assertEquals(exp.atOffset(ZoneOffset.UTC).toInstant(), instant);

		// assume simtime to be in hours, i.e. simtime is at 360.0 hours
		sim.setSimTimeToMillisFactor(60 * 60 * 1000);
		Instant i2 = sim.simTimeAbs();
		LocalDateTime e2 = LocalDateTime.of(Year.now(Clock.systemUTC()).getValue(), 1, 16, 0, 0);
		assertEquals(e2.atOffset(ZoneOffset.UTC).toInstant(), i2);
	}

	@Test
	public void testTimeConversionShouldUseInitialSimTime() {
		Simulation sim = new Simulation();
		sim.addPrintListener(System.out::println);
		sim.scheduleAt(360.0, EVENT_PRIO_NORMAL, TestSimulationBasics::dummyHandler);
		sim.setInitialSimTime(120.0);

		Map<String, Object> res = sim.performRun();
		System.out.println(res.toString());

		// default is to assume time to be in minutes, i.e. simtime is at 360.0
		// minutes
		assertEquals("simTime at end", 360.0, sim.simTime(), 1e-6);
		assertEquals("default simTimeToMillisFactor", 60 * 1000, sim.getSimTimeToMillisFactor());

		Instant instant = sim.simTimeAbs();
		LocalDateTime exp = LocalDateTime.of(Year.now(Clock.systemUTC()).getValue(), 1, 1, 4, 0);
		assertEquals(exp.atOffset(ZoneOffset.UTC).toInstant(), instant);
	}

	@Test
	public void testInstantToSimTime() {
		Simulation sim = new Simulation();
		sim.setSimTimeStartInstant(Instant.parse("2019-01-01T00:00:00.00Z"));
		sim.scheduleAt(360.0, EVENT_PRIO_NORMAL, TestSimulationBasics::dummyHandler);

		Map<String, Object> res = sim.performRun();
		System.out.println(res.toString());

		Instant instant = Instant.parse("2019-01-01T06:00:00.00Z");
		assertEquals(360.0, sim.toSimTime(instant), 1e-6);
	}

	@Test
	public void testInstantToSimTimeShouldUseInitialSimTime() {
		Simulation sim = new Simulation();
		sim.setSimTimeStartInstant(Instant.parse("2019-01-01T00:00:00.00Z"));
		sim.setInitialSimTime(120.0);
		sim.scheduleAt(360.0, EVENT_PRIO_NORMAL, TestSimulationBasics::dummyHandler);

		Map<String, Object> res = sim.performRun();
		System.out.println(res.toString());

		Instant instant = Instant.parse("2019-01-01T04:00:00.00Z");
		assertEquals(360.0, sim.toSimTime(instant), 1e-6);
	}

	@Test
	public void testInstantToSimTimeShouldUseTimeToMillisFactor() {
		Simulation sim = new Simulation();
		sim.setSimTimeStartInstant(Instant.parse("2019-01-01T00:00:00.00Z"));
		sim.setSimTimeToMillisFactor(60 * 60 * 1000); // simTime in hours
		sim.scheduleAt(360.0, EVENT_PRIO_NORMAL, TestSimulationBasics::dummyHandler);

		Map<String, Object> res = sim.performRun();
		System.out.println(res.toString());

		Instant instant = Instant.parse("2019-01-16T00:00:00.00Z");
		assertEquals(360.0, sim.toSimTime(instant), 1e-6);
	}

	public static void dummyHandler() {
		// dummy method, does nothing
	}

}
