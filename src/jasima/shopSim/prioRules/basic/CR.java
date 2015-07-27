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
package jasima.shopSim.prioRules.basic;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;

/**
 * This class implements the Critical Ratio rule and some of its variants,
 * developed by Rose (2002), doi:10.1109/WSC.2002.1166410.
 * 
 * @author Torsten Hildebrandt
 * @author Christoph Pickardt, 2011-11-15
 * @version "$Id$"
 */
public class CR extends PR {

	private static final long serialVersionUID = 1690734931010618195L;

	@Override
	public double calcPrio(PrioRuleTarget job) {
		return -((job.getDueDate() - job.getShop().simTime()) / job
				.remainingProcTime());
	}

	public static class Variant1 extends PR {

		private static final long serialVersionUID = -7545346726088550611L;

		@Override
		public double calcPrio(PrioRuleTarget job) {
			double slack = job.getDueDate() - job.getShop().simTime();

			if (slack > 0)
				return -(1.0d + slack) / (1.0d + job.remainingProcTime());
			else
				return -(1.0d / ((1.0d - slack) * (1.0d + job
						.remainingProcTime())));

		}

		@Override
		public String getName() {
			return "CR_V1";
		}
	}

	public static class Variant2 extends PR {

		private static final long serialVersionUID = 6097877363880443570L;

		@Override
		public double calcPrio(PrioRuleTarget job) {
			return -(job.getDueDate() - job.getShop().simTime())
					/ (1.0d + job.remainingProcTime());
		}

		@Override
		public String getName() {
			return "CR_V2";
		}

	}

}
