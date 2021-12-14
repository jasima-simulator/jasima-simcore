package jasima.core.simulation;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalUnit;

import jasima.core.simulation.SimProcess.MightBlock;
import jasima.core.util.SimProcessUtil.SimAction;

/**
 * A {@code SimEntity} is a {@link SimComponent} with a single
 * {@link SimProcess} modelling its main lifecycle actions using the
 * process-oriented modelling world view. The behaviour of {@code SimEntity}s
 * can be defined either by specifying a {@link SimAction}
 * {@code lifecycleActions} or by subclassing and overriding the method
 * {@link #lifecycle()}.
 * 
 * @author Torsten Hildebrandt
 * @since 3.0
 */
public class SimEntity extends SimComponentContainerBase {

//	private final SimAction lifecycleActions;
	private SimProcess<Void> lifecycleProcess = null;

	public SimEntity() {
//		this(null, (SimAction) null);
		this(null);
	}

//	public SimEntity(SimRunnable lifecycleActions) {
//		this(null, SimProcessUtil.simAction(lifecycleActions));
//	}
//
//	public SimEntity(SimAction lifecycleActions) {
//		this(null, lifecycleActions);
//	}

	public SimEntity(String name) {
//		this(name, (SimAction) null);
		super(name);
	}

//	public SimEntity(String name, SimRunnable lifecycleActions) {
//		this(name, SimProcessUtil.simAction(lifecycleActions));
//	}
//
//	public SimEntity(String name, SimAction lifecycleActions) {
//		super(name);
//		this.lifecycleActions = lifecycleActions;
//	}

	@Override
	public void init() {
		super.init();

		if (lifecycleProcess != null)
			throw new IllegalStateException();
		lifecycleProcess = new SimProcess<>(getSim(), this::lifecycle, "lifecycle");
		lifecycleProcess.setOwner(this);
	}

	@Override
	public void simStart() {
		super.simStart();

		lifecycleProcess.awakeIn(0.0);
	}

	/**
	 * Defines the behaviour of the {@link SimEntity}, potentially using
	 * blocking/process-oriented operations. This method can be overridden, the
	 * implementation here only delegates to {@link #lifecycleActions} if set.
	 * 
	 * @throws MightBlock
	 */
	protected void lifecycle() throws MightBlock {
		// TODO: make SimEntity abstract?
//		if (lifecycleActions != null) {
//			lifecycleActions.run(getSim());
//		}
	}

	public SimProcess<Void> getLifecycleProcess() {
		return lifecycleProcess;
	}

	/*
	 * Delegate most important methods of SimProcess to entity's lifecycle process
	 */

	/**
	 * @see SimProcess#resume()
	 */
	public void resume() {
		ensureProcessInitialized();
		getLifecycleProcess().resume();
	}

	/**
	 * @see SimProcess#suspend()
	 */
	public SimProcess<Void> suspend() throws MightBlock {
		ensureProcessInitialized();
		return getLifecycleProcess().suspend();
	}

	/**
	 * @see SimProcess#join()
	 */
	public SimProcess<Void> join() throws MightBlock {
		ensureProcessInitialized();
		return getLifecycleProcess().join();
	}

	/**
	 * @see SimProcess#awakeIn(double)
	 */
	public void awakeIn(double deltaT) {
		ensureProcessInitialized();
		getLifecycleProcess().awakeIn(deltaT);
	}

	/**
	 * @see SimProcess#awakeIn(long,TemporalUnit)
	 */
	public void awakeIn(long amount, TemporalUnit u) {
		ensureProcessInitialized();
		getLifecycleProcess().awakeIn(amount, u);
	}

	/**
	 * @see SimProcess#awakeIn(Duration)
	 */
	public void awakeIn(Duration d) {
		ensureProcessInitialized();
		getLifecycleProcess().awakeIn(d);
	}

	/**
	 * @see SimProcess#awakeAt(double)
	 */
	public void awakeAt(double tAbs) {
		ensureProcessInitialized();
		getLifecycleProcess().awakeAt(tAbs);
	}

	/**
	 * @see SimProcess#awakeAt(Instant)
	 */
	public void awakeAt(Instant instant) {
		ensureProcessInitialized();
		getLifecycleProcess().awakeAt(instant);
	}

//	/**
//	 * @see SimProcess#waitFor(double)
//	 */
//	public SimProcess<Void> waitFor(double deltaT) throws MightBlock {
//		return getLifecycleProcess().waitFor(deltaT);
//	}
//
//	/**
//	 * @see SimProcess#waitFor(long, TemporalUnit)
//	 */
//	public SimProcess<Void> waitFor(long amount, TemporalUnit u) throws MightBlock {
//		return getLifecycleProcess().waitFor(amount, u);
//	}
//
//	/**
//	 * @see SimProcess#waitFor(Duration)
//	 */
//	public SimProcess<Void> waitFor(Duration d) throws MightBlock {
//		return getLifecycleProcess().waitFor(d);
//	}
//
//	/**
//	 * @see SimProcess#waitUntil(double)
//	 */
//	public SimProcess<Void> waitUntil(double tAbs) throws MightBlock {
//		return getLifecycleProcess().waitUntil(tAbs);
//	}
//
//	/**
//	 * @see SimProcess#waitUntil(Instant)
//	 */
//	public SimProcess<Void> waitUntil(Instant instant) throws MightBlock {
//		return getLifecycleProcess().waitUntil(instant);
//	}

	/**
	 * @see SimProcess#cancel()
	 */
	public SimProcess<Void> cancel() {
		ensureProcessInitialized();
		return getLifecycleProcess().cancel();
	}

	private void ensureProcessInitialized() {
		if (lifecycleProcess != null)
			return;
		if (sim == null)
			setSim(SimContext.currentSimulation());
		lifecycleProcess = new SimProcess<>(getSim(), this::lifecycle, "lifecycle");
	}

	@Override
	public SimEntity setName(String name) {
		super.setName(name);
		return this;
	}

}
