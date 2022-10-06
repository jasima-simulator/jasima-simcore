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

import java.util.Random;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.PriorityQueue;

/**
 * Implements the random rule, i.e., each waiting job has an equal chance of
 * being selected.
 * 
 * @author Torsten Hildebrandt
 */
public class RND extends PR {

	private static final long serialVersionUID = -153553326402046817L;

	private Random rnd = null;
	private long seedModify = 1l;

	@Override
	public PR clone() {
		RND res = (RND) super.clone();
		res.rnd = null;
		return res;
	}

	@Override
	public void beforeCalc(PriorityQueue<?> q) {
		super.beforeCalc(q);

		if (rnd == null) {
			rnd = getOwner().getSim().getRndStreamFactory()
					.createInstance(getOwner().toString() + "." + RND.class.getName());

			// modify seed
			long seed = rnd.nextLong();
			seed = seed ^ new Random(getSeedModify()).nextLong();

			// set modified seed
			rnd.setSeed(seed);
		}
	}

	@Override
	public double calcPrio(PrioRuleTarget entry) {
		return rnd.nextDouble();
	}

	public long getSeedModify() {
		return seedModify;
	}

	/**
	 * Allows to modify the random number stream used. This allows to switch to a
	 * different seed independently from the base seed used for an experiment.
	 * Therefore this attribute allows to test different random decisions for a
	 * single base seed, which can be useful if the base seed determines a certain
	 * scenario and the aim is to quantify the performance of RND for exactly this
	 * scenario by averaging over many random decisions.
	 */
	public void setSeedModify(long seedModify) {
		this.seedModify = seedModify;
	}

}
