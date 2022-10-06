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
 * Returns a priority of +1 if setup states of the machine matches the setup
 * state required by a job, or -1 otherwise.
 * 
 * @author Torsten Hildebrandt
 */
public class SetupAvoidance extends PR {

	private static final long serialVersionUID = -7225391816297143198L;

	public SetupAvoidance() {
		super();
	}

	@Override
	public double calcPrio(PrioRuleTarget j) {
		if (getOwner().currMachine.setupState == j.getCurrentOperation().getSetupState())
			return +1;
		else
			return -1;
	}

	@Override
	public String getName() {
		return "SA";
	}

}
