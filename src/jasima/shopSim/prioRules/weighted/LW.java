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
package jasima.shopSim.prioRules.weighted;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;

/**
 * This class implements the Largest Weight rule, where different weights
 * express different levels of importance of jobs.
 * 
 * @author Christoph Pickardt, 2010-03-16
 * @version $Id$
 */
public class LW extends PR {

	@Override
	public double calcPrio(PrioRuleTarget t) {
		return t.getWeight();
	}

}
