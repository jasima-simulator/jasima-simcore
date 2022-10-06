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

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;

/**
 * This class implements the 2PT+WINQ+NPT rule, developed by Holthaus and
 * Rajendran (2000), DOI: 10.1080/095372800232379, which extends PT+WINQ by
 * taking into account the processing time of a job's next operation.
 * 
 * @author Christoph Pickardt, 2011-11-15
 */
public class PTPlusWINQPlusNPT extends PR {

	private static final long serialVersionUID = 249563235097986052L;

	@Override
	public double calcPrio(PrioRuleTarget j) {
		return -(2 * j.currProcTime() + WINQ.winq(j) + npt(j));
	}

	/**
	 * Next Processing Time of a job, i.e. processing time of next operation.
	 */
	public static double npt(PrioRuleTarget job) {
		int nextTask = job.getTaskNumber() + 1;
		if (nextTask >= job.getOps().length)
			return 0.0d;
		else
			return job.getOps()[nextTask].getProcTime();
	}

}
