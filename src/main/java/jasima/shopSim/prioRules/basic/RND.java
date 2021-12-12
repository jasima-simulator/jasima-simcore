/*******************************************************************************
 * This file is part of jasima, v1.3, the Java simulator for manufacturing and 
 * logistics.
 *  
 * Copyright (c) 2015 		jasima solutions UG
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
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
