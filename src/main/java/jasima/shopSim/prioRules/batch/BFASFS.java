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
package jasima.shopSim.prioRules.batch;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;

/**
 * This class implements a batch version of the FASFS rule. Its main purpose is
 * to be used as a tie-breaker for priority rules that operate on batches, e.g.
 * BATCS.
 * <p>
 * BFASFS is supposed to be applied together with BestOfFamilyBatching to choose
 * among batches of different families.
 * 
 * @author Christoph Pickardt, 2011-11-14
 */
public class BFASFS extends PR {

	private static final long serialVersionUID = 1769946022493174146L;

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
