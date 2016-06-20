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
//		mre.setMinReplications(0);
		mre.setMaxReplications(300);
		mre.setInitialSeed(23);

		Map<String, Object> res = mre.runExperiment();
		
		mre.printResults();

		int numTasks = (int) res.get(MultipleReplicationExperiment.NUM_TASKS_EXECUTED);

		assertEquals("number of replications performed", 94, numTasks);
	}

}
