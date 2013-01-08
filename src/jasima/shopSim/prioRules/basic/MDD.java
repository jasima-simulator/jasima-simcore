/*******************************************************************************
 * Copyright (c) 2010-2013 Torsten Hildebrandt and jasima contributors
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
 *
 * $Id$
 *******************************************************************************/
package jasima.shopSim.prioRules.basic;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;

/**
 * This class implements the Modified Due Date rule, developed by Baker and
 * Bertrand (1982) and Baker and Kanet (1983). The rule represents a compromise
 * between the EDD and SRPT rules.
 * 
 * @author Christoph Pickardt, 2011-11-15
 * @version $Id$
 */
public class MDD extends PR {

	@Override
	public double calcPrio(PrioRuleTarget job) {
		return -Math.max(job.remainingProcTime(), job.getDueDate()
				- job.getShop().simTime());
	}

}
