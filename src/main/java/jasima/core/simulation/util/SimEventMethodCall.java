package jasima.core.simulation.util;

import jasima.core.simulation.SimEvent;

/**
 * This class is used internally by {@link #scheduleAt(double,int,Runnable)} to
 * run a certain method at a particular simulation time.
 */
public final class SimEventMethodCall extends SimEvent {
	public final Runnable m;
	public final boolean isAppEvent;

	public SimEventMethodCall(double time, int prio, String description, Runnable method) {
		this(time, prio, description, method, true);
	}

	public SimEventMethodCall(double time, int prio, String description, Runnable method, boolean isAppEvent) {
		super(time, prio, description);
		this.m = method;
		this.isAppEvent = isAppEvent;
	}

	@Override
	public void handle() {
		m.run();
	}

	@Override
	public boolean isAppEvent() {
		return isAppEvent;
	}

	@Override
	public String toString() {
		return getDescription() != null ? getDescription() : String.format("MethodCallEvent(%s)", m.toString());
	}
}