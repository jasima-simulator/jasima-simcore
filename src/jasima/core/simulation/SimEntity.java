package jasima.core.simulation;

import jasima.core.simulation.SimProcess.MightBlock;
import jasima.core.util.SimProcessUtil;
import jasima.core.util.SimProcessUtil.SimAction;
import jasima.core.util.SimProcessUtil.SimRunnable;

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
public class SimEntity extends SimComponentBase {

	private final SimAction lifecycleActions;
	private SimProcess<Void> lifecycleProcess = null;

	public SimEntity() {
		this(null, (SimAction) null);
	}

	public SimEntity(SimRunnable lifecycleActions) {
		this(null, SimProcessUtil.simAction(lifecycleActions));
	}

	public SimEntity(SimAction lifecycleActions) {
		this(null, lifecycleActions);
	}

	public SimEntity(String name) {
		this(name, (SimAction) null);
	}

	public SimEntity(String name, SimRunnable lifecycleActions) {
		this(name, SimProcessUtil.simAction(lifecycleActions));
	}

	public SimEntity(String name, SimAction lifecycleActions) {
		super(name);
		this.lifecycleActions = lifecycleActions;
	}

	@Override
	public void init() {
		super.init();

		lifecycleProcess = new SimProcess<>(getSim(), this::lifecycle, "lifecycle");
		lifecycleProcess.setOwner(this);
	}

	@Override
	public void simStart() {
		super.simStart();

		lifecycleProcess.awakeIn(0.0);
	}

	/**
	 * 
	 * @throws MightBlock
	 */
	protected void lifecycle() throws MightBlock {
		if (lifecycleActions != null) {
			lifecycleActions.run(getSim());
		}
	}

	public SimProcess<Void> getLifecycleProcess() {
		return lifecycleProcess;
	}

}
