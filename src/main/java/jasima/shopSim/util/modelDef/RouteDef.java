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

import jasima.core.util.TypeUtil;

public class RouteDef extends PropertySupport {

	private static final long serialVersionUID = -6956187506442015147L;

	public static final String PROP_NAME = "name";
	public static final String PROP_OPERATIONS = "operations";

	private String name;
	private OperationDef[] operations;
	private ShopDef shop;

	public OperationDef[] getOperations() {
		return operations;
	}

	public void setOperations(OperationDef[] operations) {
		firePropertyChange(PROP_OPERATIONS, this.operations, this.operations = operations);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		firePropertyChange(PROP_NAME, this.name, this.name = name);
	}

	public ShopDef getShop() {
		return shop;
	}

	public void setShop(ShopDef shop) {
		firePropertyChange("shop", this.shop, this.shop = shop);
	}

	@Override
	public RouteDef clone() {
		RouteDef c = (RouteDef) super.clone();

		c.operations = TypeUtil.deepCloneArrayIfPossible(operations);

		return c;
	}

}
