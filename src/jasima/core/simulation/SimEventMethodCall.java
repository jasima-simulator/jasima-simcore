package jasima.core.simulation;

/**
 * This class is used internally by {@link #schedule(double,int,Runnable)} to
 * run a certain method at a particular simulation time.
 */
public final class SimEventMethodCall extends SimEvent {
	public final Runnable m;

	public SimEventMethodCall(double time, int prio, String description, Runnable method) {
		super(time, prio, description);
		m = method;
	}

	@Override
	public void handle() {
		m.run();
	}

	@Override
	public String toString() {
		return getDescription() != null ? getDescription() : String.format("MethodCallEvent(%s)", m.toString());
	}
}