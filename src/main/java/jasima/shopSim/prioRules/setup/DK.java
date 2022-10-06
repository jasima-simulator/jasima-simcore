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
package jasima.shopSim.prioRules.setup;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;

/**
 * This class implements the DK rule, developed by Mahmoodi and Dooley (1991),
 * see also Pickardt and Branke (2012). The rule is an adaption of EDD, which
 * penalizes jobs that require a setup.
 * 
 * @author Christoph Pickardt, 2011-11-15
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
		if (getOwner().currMachine.setupState == job.getCurrentOperation().getSetupState())
			return -job.getDueDate();
		else
			return -(job.getDueDate() + k);
	}

	@Override
	public String getName() {
		return "DK(k=" + k + ")";
	}

}
