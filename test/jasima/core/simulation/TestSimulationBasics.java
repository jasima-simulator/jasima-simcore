package jasima.core.simulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.Map;

import org.junit.Test;

public class TestSimulationBasics {

	@Test
	public void testInitialSimTime() {
		Simulation sim = new Simulation();
		sim.addPrintListener(System.out::println);
		sim.schedule(0.0, Event.EVENT_PRIO_NORMAL, TestSimulationBasics::handleEvent);
		sim.setInitialSimTime(100);
		try {
			Map<String, Object> res = sim.performRun();
			System.out.println(res.toString());
			fail("No exception raised.");
		} catch (IllegalArgumentException e) {
			// do nothing, Exception is expected
		}
	}

	@Test
	public void testTimeConversion() {
		Simulation sim = new Simulation();
		sim.addPrintListener(System.out::println);
		sim.schedule(360.0, Event.EVENT_PRIO_NORMAL, TestSimulationBasics::handleEvent);

		Map<String, Object> res = sim.performRun();
		System.out.println(res.toString());

		// default is to assume time to be in minutes, i.e. simtime is at 360.0
		// minutes
		Instant instant = sim.simTimeToInstant();
		LocalDateTime exp = LocalDateTime.of(Year.now(Clock.systemUTC()).getValue(), 1, 1, 6, 0);
		assertEquals(exp.atOffset(ZoneOffset.UTC).toInstant(), instant);

		// assume simtime to be in hours, i.e. simtime is at 360.0 hours
		sim.setSimTimeToMillisFactor(60 * 60 * 1000);
		Instant i2 = sim.simTimeToInstant();
		LocalDateTime e2 = LocalDateTime.of(Year.now(Clock.systemUTC()).getValue(), 1, 16, 0, 0);
		assertEquals(e2.atOffset(ZoneOffset.UTC).toInstant(), i2);
	}

	public static void handleEvent() {
		// dummy method, does nothing
	}

}
