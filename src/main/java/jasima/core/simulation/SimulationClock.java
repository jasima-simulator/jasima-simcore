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
package jasima.core.simulation;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;

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
		return sim.simTimeAbs();
	}

	@Override
	public Clock withZone(ZoneId zone) {
		if (zone.equals(this.zone)) {
			return this;
		}
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