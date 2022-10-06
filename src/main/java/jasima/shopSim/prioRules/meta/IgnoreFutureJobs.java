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
package jasima.shopSim.prioRules.meta;

import jasima.shopSim.core.PR;

/**
 * Helper class to use a rule which is designed to assign a low priority to
 * future jobs when the lookahead is enabled, thereby generating non delay
 * schedules. Such a non delay rule has to be set the tie breaker rule of this
 * class.
 * <p>
 * IgnoreFuturedRule assigns each future job a priority of -1 and each "normal"
 * job a priority of +1, therefore the tie breaker rule is used to select one of
 * the non-future jobs.
 * 
 * @author Torsten Hildebrandt, 2010-02-16
 */
public class IgnoreFutureJobs extends FixedLAThreshold {

	private static final long serialVersionUID = -2444578540937649208L;

	public IgnoreFutureJobs(PR baseRule) {
		super(baseRule, 0.0);
	}

	@Override
	public String getName() {
		return "IGF";
	}

}
