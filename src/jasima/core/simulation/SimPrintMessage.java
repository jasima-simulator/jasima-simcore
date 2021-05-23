package jasima.core.simulation;

import static java.util.Objects.requireNonNull;

import jasima.core.util.ConsolePrinter;
import jasima.core.util.MsgCategory;
import jasima.core.util.TraceFileProducer;

/**
 * {@link SimPrintMessage}s are produced whenever {@code print()} or
 * {@code trace()} is called during a simulation to produce status/debug
 * messages. They are passed to print listeners registered with a simulation for
 * further processing.
 * 
 * @see ConsolePrinter
 * @see TraceFileProducer
 */
public class SimPrintMessage {

	private static final Object[] EMPTY = new Object[0];

	private final Simulation sim;
	private final MsgCategory category;
	private final double simTime;
	private final Object[] params;
	private String message;

	public SimPrintMessage(Simulation sim, MsgCategory category, String message) {
		this(sim, category, requireNonNull(message), EMPTY);
	}

	public SimPrintMessage(Simulation sim, MsgCategory category, Object... params) {
		this(sim, category, null, requireNonNull(params));
	}

	protected SimPrintMessage(Simulation sim, MsgCategory category, String msg, Object... params) {
		super();
		this.sim = sim;
		this.simTime = sim.simTime();
		this.category = category;
		this.params = params;

		this.message = msg;
	}

	public Simulation getSim() {
		return sim;
	}

	public MsgCategory getCategory() {
		return category;
	}

	public double getSimTime() {
		return simTime;
	}

	public Object[] getParams() {
		return params;
	}

	public String getMessage() {
		// lazy creation of message only when needed
		if (message == null) {
			StringBuilder sb = new StringBuilder();
			sb.append(getSimTime());
			for (Object o : getParams()) {
				sb.append('\t').append(String.valueOf(o));
			}
			message = sb.toString();
		}

		return message;
	}

	@Override
	public String toString() {
		return getMessage();
	}
}