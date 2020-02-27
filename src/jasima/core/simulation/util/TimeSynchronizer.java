package jasima.core.simulation.util;

import java.time.Duration;

import jasima.core.simulation.SimEvent;
import jasima.core.simulation.Simulation;

public class TimeSynchronizer {

	public static final int NUM_SYNC_EVENTS_PER_REAL_SECOND = 25;
	public static final long REAL_TIME_BETWEEN_SYNC_MILLIS = Math.round(1000.0 / NUM_SYNC_EVENTS_PER_REAL_SECOND);

	private final double realTimeFactor;
	private final Simulation sim;

	private long eventCounter;
	private long framesDropped;
	private double simTimeBetweenSync;

	private long tStartReal;
	private double tStartSim;

	private SimEvent syncEvent;

	public TimeSynchronizer(Simulation sim, double realTimeFactor) {
		super();

		this.sim = sim;
		this.realTimeFactor = realTimeFactor;

		eventCounter = framesDropped = 0;

		tStartReal = System.currentTimeMillis();
		tStartSim = sim.getInitialSimTime();

		syncEvent = new SimEvent(tStartSim, SimEvent.EVENT_PRIO_HIGHEST, "sync_event") {
			@Override
			public void handle() {
				// sync with real time
				syncTime();

				// schedule next sync event
				eventCounter++;
				simTimeBetweenSync = sim.toSimTime(Duration.ofMillis(REAL_TIME_BETWEEN_SYNC_MILLIS))
						* realTimeFactor;

				setTime(tStartSim + eventCounter * simTimeBetweenSync);
				sim.schedule(this);
			}

			@Override
			public boolean isAppEvent() {
				return false;
			}
		};
		sim.schedule(syncEvent);
	}

	protected void syncTime() {
		double realTimeDue = tStartReal + eventCounter * REAL_TIME_BETWEEN_SYNC_MILLIS;
		double timeDiff = realTimeDue - System.currentTimeMillis();
//		System.out.println(System.currentTimeMillis()+"\t"+sim.simTime()+"\t"+realTimeDue+"\t"+timeDiff+"\t"+framesDropped);
		if (timeDiff > 0) {
			try {
				Thread.sleep(Math.round(timeDiff));
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		} else if (timeDiff < 0) {
			framesDropped++;
		}
	}

}
