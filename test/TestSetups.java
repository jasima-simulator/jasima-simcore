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
import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.prioRules.basic.FCFS;
import jasima.shopSim.prioRules.basic.SPT;
import jasima.shopSim.prioRules.setup.SST;

import org.junit.Test;

/**
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version 
 *          "$Id$"
 */
public class TestSetups {

	@Test
	public void test1x4Setup2() throws Exception {
		TestStaticInsts.testFromFile("testInstances/01x04setups.txt", rules);
	}

	@SuppressWarnings("serial")
	static PR[] rules = new PR[] { new SPT(), new FCFS(), new PR() {

		@Override
		public double calcPrio(PrioRuleTarget j) {
			return -j.getArriveTime();
		}
	}, new PR() {

		@Override
		public double calcPrio(PrioRuleTarget j) {
			return -j.currProcTime();
		}
	}, new SST() };

}
