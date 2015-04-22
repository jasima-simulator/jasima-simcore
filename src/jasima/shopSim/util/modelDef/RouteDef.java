/*******************************************************************************
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This file is part of jasima, v1.2.
 *
 * jasima is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jasima is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jasima.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package jasima.shopSim.util.modelDef;

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
		firePropertyChange(PROP_OPERATIONS, this.operations,
				this.operations = operations);
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

}
