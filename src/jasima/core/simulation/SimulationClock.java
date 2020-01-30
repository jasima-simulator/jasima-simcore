package jasima.core.simulation;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;

import org.apache.logging.log4j.core.util.SystemMillisClock;

/**
 * Implementation of a {@link Clock} that always returns the latest time from
 * the underlying simulation. Don't instantiate this class directly, use
 * {@link Simulation#clock()} instead.
 * 
 * @author Torsten Hildebrandt
 */
final class SimulationClock extends Clock {
	private final Simulation sim;
	private final ZoneId zone;

	SimulationClock(Simulation sim, ZoneId zone) {
		this.sim = requireNonNull(sim);
		this.zone = requireNonNull(zone);
	}

	@Override
	public ZoneId getZone() {
		return zone;
	}

	@Override
	public Instant instant() {
		return sim.simTimeToInstant();
	}

	@Override
	public Clock withZone(ZoneId zone) {
		if (zone.equals(this.zone)) {
			return this;
		}SystemMillisClock c;
		return new SimulationClock(sim, zone);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof SimulationClock) {
			SimulationClock sc = (SimulationClock) obj;
			return zone.equals(sc.zone) && sim.equals(sc.sim);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(sim, zone);
	}

	@Override
	public String toString() {
		return "SimulationClock[" + zone + "]";
	}
}