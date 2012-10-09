/*******************************************************************************
 * Copyright 2011, 2012 Torsten Hildebrandt and BIBA - Bremer Institut für Produktion und Logistik GmbH
 *
 * This file is part of jasima, v1.0.
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
package jasima.shopSim.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Simple container for Operations.
 * 
 * @version "$Id$"
 */
public class Route {

	private ArrayList<Operation> ops;
	private Operation[] opsArray; // cache results of call to ops()

	public Route() {
		this.ops = new ArrayList<Operation>();
	}

	public Operation operation(int i) {
		return ops.get(i);
	}

	public void addSequentialOperation(Operation op) {
		opsArray = null;
		ops.add(op);
	}

	public int numOperations() {
		return ops.size();
	}

	public Collection<Operation> operations() {
		return Collections.unmodifiableCollection(ops);
	}

	public Operation[] ops() {
		if (opsArray == null)
			opsArray = ops.toArray(new Operation[ops.size()]);
		return opsArray;
	}

}
