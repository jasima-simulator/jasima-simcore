package jasima.shopSim.util.modelDef;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class PropertySupport implements Serializable {

	private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
			this);

	public PropertySupport() {
		super();
	}

	// listener handling

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(propertyName,
				listener);
	}

	protected void firePropertyChange(String propertyName, Object oldVal,
			Object newVal) {
		propertyChangeSupport.firePropertyChange(propertyName, oldVal, newVal);
	}

	// deserialize transient data
	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		propertyChangeSupport = new PropertyChangeSupport(this);
	}

	private Object readResolve() {
		propertyChangeSupport = new PropertyChangeSupport(this);
		return this;
	}

}
