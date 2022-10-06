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
package jasima.shopSim.util.modelDef;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class PropertySupport implements Serializable, Cloneable {

	private static final long serialVersionUID = 4437433295143161294L;

	private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	public PropertySupport() {
		super();
	}

	// listener handling

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
	}

	protected void firePropertyChange(String propertyName, Object oldVal, Object newVal) {
		propertyChangeSupport.firePropertyChange(propertyName, oldVal, newVal);
	}

	// deserialize transient data
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		propertyChangeSupport = new PropertyChangeSupport(this);
	}

	private Object readResolve() {
		propertyChangeSupport = new PropertyChangeSupport(this);
		return this;
	}

	@Override
	public PropertySupport clone() {
		try {
			return (PropertySupport) super.clone();
		} catch (CloneNotSupportedException cantHappen) {
			throw new AssertionError(cantHappen);
		}
	}

}
