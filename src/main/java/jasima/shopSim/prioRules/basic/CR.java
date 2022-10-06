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
package jasima.shopSim.prioRules.basic;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;

/**
 * This class implements the Critical Ratio rule and some of its variants,
 * developed by Rose (2002), doi:10.1109/WSC.2002.1166410.
 * 
 * @author Torsten Hildebrandt
 * @author Christoph Pickardt, 2011-11-15
 */
public class CR extends PR {

	private static final long serialVersionUID = 1690734931010618195L;

	@Override
	public double calcPrio(PrioRuleTarget job) {
		return -((job.getDueDate() - job.getShop().simTime()) / job.remainingProcTime());
	}

	public static class Variant1 extends PR {

		private static final long serialVersionUID = -7545346726088550611L;

		@Override
		public double calcPrio(PrioRuleTarget job) {
			double slack = job.getDueDate() - job.getShop().simTime();

			if (slack > 0)
				return -(1.0d + slack) / (1.0d + job.remainingProcTime());
			else
				return -(1.0d / ((1.0d - slack) * (1.0d + job.remainingProcTime())));

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
			return -(job.getDueDate() - job.getShop().simTime()) / (1.0d + job.remainingProcTime());
		}

		@Override
		public String getName() {
			return "CR_V2";
		}

	}

}
