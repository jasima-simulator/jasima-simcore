package jasima.core.simulation;

import javax.annotation.Nullable;

import jasima.core.util.TypeUtil;
import jasima.core.util.ValueStore;
import jasima.core.util.ValueStoreImpl;
import jasima.core.util.observer.NotifierImpl;

/**
 * Potential base class for simulation components implementing the interface
 * {@link SimComponent}. Additionally this class introduces a name attribute
 * (that is also used in the {@link #toString()} method).
 * 
 * @author Torsten Hildebrandt
 */
public class SimComponentBase implements SimComponent {

	private transient SimComponent parent;
	private transient String hierarchicalName;
	protected transient Simulation sim;
	private transient boolean initialized;

	private String name;

//	 delegate Notifier functionality
	private NotifierImpl<SimComponent, SimComponentEvent> notifierAdapter;

	// delegate ValueStore functionality
	private ValueStoreImpl valueStore;

	public SimComponentBase() {
		this(null);
	}

	public SimComponentBase(String name) {
		super();
		initialized = false;
		if (name != null) {
			setName(name);
		}
	}

	@Override
	public Simulation getSim() {
		if (sim == null)
			throw new IllegalStateException("no simulation.");
		return sim;
	}

	@Override
	public void setSim(Simulation s) {
		sim = s;
	}

	@Override
	public @Nullable SimComponent getParent() {
		return parent;
	}

	@Override
	public void setParent(@Nullable SimComponent parent) {
		this.hierarchicalName = null;
		this.parent = parent;
	}

	@Override
	public String getHierarchicalName() {
		// cache the full name for performance reasons
		if (hierarchicalName == null) {
			hierarchicalName = SimComponent.super.getHierarchicalName();
		}
		return hierarchicalName;
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public void setInitialized(boolean initStatus) {
		initialized = initStatus;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Sets a name for this component.
	 */
	@Override
	public SimComponent setName(String name) {
		if (!isValidName(name)) {
			throw new IllegalArgumentException(String.format("Component name '%s' is not valid.", name));
		}
		setNameInternal(name);
		return this;
	}

	protected void setNameInternal(String name) {
		this.hierarchicalName = null;
		this.name = name;
	}

	@Override
	public String toString() {
		if (getName() == null) {
			return getClass().getSimpleName() + '@' + Integer.toHexString(this.hashCode());
		} else {
			return getName();
		}
	}

	// ValueStore implementation

	@Override
	public ValueStore valueStoreImpl() {
		if (valueStore == null) {
			valueStore = new ValueStoreImpl();
		}
		return valueStore;
	}

	// event notification

	@Override
	public NotifierImpl<SimComponent, SimComponentEvent> notifierImpl() {
		if (notifierAdapter == null) {
			notifierAdapter = new NotifierImpl<>(this);
		}
		return notifierAdapter;
	}

	// cloning

	@Override
	public SimComponentBase clone() {
		try {
			SimComponentBase c = (SimComponentBase) super.clone();

			// clone value store copying (but not cloning!) all of its entries
			if (valueStore != null) {
				c.valueStore = valueStore.clone();
			}

			if (notifierAdapter != null) {
				c.notifierAdapter = new NotifierImpl<>(c);
				for (int i = 0; i < numListener(); i++) {
					c.addListener(TypeUtil.cloneIfPossible(getListener(i)));
				}
			}

			return c;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

}
