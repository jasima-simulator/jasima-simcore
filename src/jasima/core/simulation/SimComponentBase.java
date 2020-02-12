package jasima.core.simulation;

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

	public static final char NAME_SEPARATOR = '.';

	private transient SimComponentContainer<?> parent;
	private transient String hierarchicalName;
	private transient Simulation sim;

	private String name;

	// delegate Notifier functionality
	private NotifierImpl<SimComponent, Object> notifierAdapter;

	// delegate ValueStore functionality
	private ValueStoreImpl valueStore;

	public SimComponentBase() {
		this(null);
	}

	public SimComponentBase(String name) {
		super();
		if (name != null) {
			setName(name);
		}
	}

	@Override
	public Simulation getSim() {
		return sim;
	}

	@Override
	public void setSim(Simulation s) {
		sim = s;
	}

	@Override
	public SimComponentContainer<?> getParent() {
		return parent;
	}

	@Override
	public void setParent(SimComponentContainer<?> parent) {
		hierarchicalName = null;
		this.parent = parent;
	}

	@Override
	public String getHierarchicalName() {
		if (hierarchicalName == null) {
			StringBuilder sb = new StringBuilder();
			SimComponentContainer<?> p = getParent();
			if (p != null && p != getSim().getRootComponent()) {
				sb.append(p.getHierarchicalName()).append(NAME_SEPARATOR);
			}
			sb.append(this.toString());
			hierarchicalName = sb.toString();
		}

		return hierarchicalName;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Sets a name for this component.
	 */
	@Override
	public void setName(String name) {
		if (!isValidName(name)) {
			throw new IllegalArgumentException(String.format("Component name '%s' is not valid.", name));
		}

		if (this.name != null) {
			throw new IllegalStateException("'name' can only be set once.");
		}

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
	public NotifierImpl<SimComponent, Object> notifierImpl() {
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
