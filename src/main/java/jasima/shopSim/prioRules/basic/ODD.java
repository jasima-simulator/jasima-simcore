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
 * This class implements the Operation Due Date rule. The idea to use due dates
 * of operations instead of job due dates originates from a paper by Kanet and
 * Hayya (1982).
 * 
 * @author Christoph Pickardt, 2011-11-15
 */
public class ODD extends PR {

	private static final long serialVersionUID = -2691388850642748038L;

	@Override
	public double calcPrio(PrioRuleTarget j) {
		return -j.getCurrentOperationDueDate();
	}

}
