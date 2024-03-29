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
package jasima.shopSim.prioRules.upDownStream;

import jasima.shopSim.core.PrioRuleTarget;

/**
 * This class implements an extension of the IFTMinusUIT rule, developed by
 * Branke and Pickardt (2011).
 * <p>
 * The lookahead needs to be enabled in the simulation for this rule to work
 * properly.
 * 
 * @author Christoph Pickardt, 2011-11-15
 */
public class IFTMinusUITPlusNPT extends IFTMinusUIT {

	private static final long serialVersionUID = -1423420723665183647L;

	@Override
	public double calcPrio(PrioRuleTarget j) {
		return super.calcPrio(j) - PTPlusWINQPlusNPT.npt(j);
	}

}
