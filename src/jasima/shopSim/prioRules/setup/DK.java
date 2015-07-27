/*******************************************************************************
 * This file is part of jasima, v1.3, the Java simulator for manufacturing and 
 * logistics.
 *  
 * Copyright (c) 2015 		jasima solutions UG
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package jasima.shopSim.prioRules.setup;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;

/**
 * This class implements the DK rule, developed by Mahmoodi and Dooley (1991),
 * see also Pickardt and Branke (2012). The rule is an adaption of EDD, which
 * penalizes jobs that require a setup.
 * 
 * @author Christoph Pickardt, 2011-11-15
 * @version "$Id$"
 */
public class DK extends PR {

	private static final long serialVersionUID = -4144397706147527937L;

	public final double k;

	public DK(double k) {
		super();
		this.k = k;
	}

	@Override
	public double calcPrio(PrioRuleTarget job) {
		if (getOwner().currMachine.setupState == job.getCurrentOperation().setupState)
			return -job.getDueDate();
		else
			return -(job.getDueDate() + k);
	}

	@Override
	public String getName() {
		return "DK(k=" + k + ")";
	}

}
