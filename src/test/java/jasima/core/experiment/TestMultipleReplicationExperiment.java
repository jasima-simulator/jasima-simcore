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
package jasima.core.experiment;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import jasima.shopSim.models.dynamicShop.DynamicShopExperiment;

public class TestMultipleReplicationExperiment {

	@Test
	public void testDynRunsWithMinReps() {
		MultipleReplicationExperiment mre = new MultipleReplicationExperiment();
		
		mre.setBaseExperiment(new DynamicShopExperiment());
		mre.addConfIntervalMeasure("flowtime");
		mre.setAllowancePercentage(0.02);
		mre.setMinReplications(1);
		mre.setMaxReplications(300);
		mre.setInitialSeed(23);

		Map<String, Object> res = mre.runExperiment();
		
		mre.printResults();

		int numTasks = (int) res.get(MultipleReplicationExperiment.NUM_TASKS_EXECUTED);

		assertEquals("number of replications performed", 94, numTasks);
	}

}
