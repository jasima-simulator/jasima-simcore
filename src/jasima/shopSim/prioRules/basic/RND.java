/*******************************************************************************
 * Copyright (c) 2010-2013 Torsten Hildebrandt and jasima contributors
 *
 * This file is part of jasima, v1.0.
 *
 * jasima is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jasima is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jasima.  If not, see <http://www.gnu.org/licenses/>.
 *
 * $Id$
 *******************************************************************************/
package jasima.shopSim.prioRules.basic;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.PriorityQueue;

import java.util.Random;

/**
 * Implements the random rule, i.e., each waiting job has an equal chance of
 * being selected.
 * 
 * @author Torsten Hildebrandt, 2012-03-08
 * @version $Id$
 */
public class RND extends PR {

	private static final long serialVersionUID = -153553326402046817L;

	private Random rnd = null;
	private long seedModify = 1l;

	@Override
	public PR clone() throws CloneNotSupportedException {
		RND res = (RND) super.clone();
		res.rnd = null;
		return res;
	}

	@Override
	public void beforeCalc(PriorityQueue<?> q) {
		super.beforeCalc(q);

		if (rnd == null) {
			rnd = getOwner().shop().getRndStreamFactory().createInstance(
					getOwner().toString() + "." + RND.class.getName());

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
	 * Allows to modify the random number stream used. This allows to switch to
	 * a different seed independently from the base seed used for an experiment.
	 * Therefore this attribute allows to test different random decisions for a
	 * single base seed, which can be useful if the base seed determines a
	 * certain scenario and the aim is to quantify the performance of RND for
	 * exactly this scenario by averaging over many random decisions.
	 */
	public void setSeedModify(long seedModify) {
		this.seedModify = seedModify;
	}

}
