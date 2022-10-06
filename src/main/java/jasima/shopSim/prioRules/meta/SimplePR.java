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

import java.io.Serializable;
import java.util.Objects;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.prioRules.basic.SPT;

/**
 * Utility class that can be used to write simple priority rules as a lambda
 * expression (PR itself is not a functional interface). So in order to express
 * the {@link SPT} rule you can now write
 * 
 * <pre>
 * PR spt = new SimplePR("spt", job -> -job.currProcTime());
 * </pre>
 * 
 * instead of writing an anonymous inner class:
 * 
 * <pre>
 * PR spt = new PR() {
 * 	&#64;Override
 * 	public double calcPrio(PrioRuleTarget job) {
 * 		return -job.currProcTime();
 * 	}
 * };
 * </pre>
 * 
 * @author Torsten Hildebrandt <torsten.hildebrandt@simplan.de>
 */
public class SimplePR extends PR {

	private static final long serialVersionUID = -1148070289969223577L;

	@FunctionalInterface
	public static interface JobEvaluator extends Serializable {
		double getValue(PrioRuleTarget jobOrBatch);
	}

	private final JobEvaluator prioFunction;
	private final String name;

	public SimplePR(JobEvaluator prioFunction) {
		this(null, prioFunction);
	}

	public SimplePR(String name, JobEvaluator prioFunction) {
		super();
		this.prioFunction = Objects.requireNonNull(prioFunction);
		if (name != null)
			this.name = name;
		else
			this.name = prioFunction.getClass().toString();
	}

	@Override
	public double calcPrio(PrioRuleTarget entry) {
		return prioFunction.getValue(entry);
	}

	@Override
	public String getName() {
		return "SimplePR(" + name + ")";
	}

}
