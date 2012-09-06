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
package jasima.shopSim.prioRules.weighted;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;

/**
 * This class implements the Weighted Modified Operation Due Date rule, which
 * extends MOD by taking different job weights into account. The implementation
 * here is based on the analogous extension of the MDD rule by Kanet and Li
 * (2004), which turned out to be more effective than an earlier formulation of
 * the WMOD rule by Jensen et al. (1995).
 * 
 * @author Christoph Pickardt, 2011-11-15
 */
public class WMOD extends PR {

	@Override
	public double calcPrio(PrioRuleTarget job) {
		return -Math.max(job.currProcTime(), job.getCurrentOperationDueDate()
				- job.getShop().simTime())
				/ job.getWeight();
	}

}
