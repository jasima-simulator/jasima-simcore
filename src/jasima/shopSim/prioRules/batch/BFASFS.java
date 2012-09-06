/*******************************************************************************
 * Copyright 2011, 2012 Torsten Hildebrandt and BIBA - Bremer Institut für Produktion und Logistik GmbH
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
 *******************************************************************************/
package jasima.shopSim.prioRules.batch;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;

/**
 * This class implements a batch version of the FASFS rule. Its main purpose is
 * to be used as a tie-breaker for priority rules that operate on batches, e.g.
 * BATCS.
 * <p />
 * BFASFS is supposed to be applied together with BestOfFamilyBatching to choose
 * among batches of different families.
 * 
 * @author Christoph Pickardt, 2011-11-14
 */
public class BFASFS extends PR {

	@Override
	public double calcPrio(PrioRuleTarget b) {
		double averageSqTimeInShop = 0;
		for (int i = 0; i < b.numJobsInBatch(); i++) {
			double tis = (b.job(i).getShop().simTime() - b.job(i).getRelDate());
			averageSqTimeInShop += tis * tis;
		}
		averageSqTimeInShop /= b.numJobsInBatch();
		return averageSqTimeInShop;
	}

}
