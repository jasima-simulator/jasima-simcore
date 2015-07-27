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
 * This class implements the Shortest Setup and Processing Time rule, developed
 * by Wilbrecht and Prescott (1969), which selects the job that can be completed
 * the quickest, see also Pickardt and Branke (2012).
 * 
 * @author Christoph Pickardt, 2011-11-15
 * @version "$Id$"
 */
public class SSPT extends PR {

	private static final long serialVersionUID = -6352884056364065280L;

	@Override
	public double calcPrio(PrioRuleTarget job) {
		return -(getOwner().getSetupMatrix()[getOwner().currMachine.setupState][job
				.getCurrentOperation().setupState] + job.getCurrentOperation().procTime);
	}

}
