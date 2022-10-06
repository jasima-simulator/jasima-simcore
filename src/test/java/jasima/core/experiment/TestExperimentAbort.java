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

import jasima.core.statistics.SummaryStat;

/**
 * 
 * @author Torsten Hildebrandt
 */
public class TestExperimentAbort {

	private static final int NUM_RUNS = 10;

	private final static class AbortingExperiment extends Experiment {

		private static final long serialVersionUID = 8759221809243043695L;

		@Override
		protected void performRun() {
			// some dummy action
			String s = "";
			for (int i = 0, n = 30000; i < n; i++) {
				s += "a";
			}
			s.toString();

			// do nothing but to set abort
			aborted = 1;
		}

		@Override
		protected void produceResults() {
			super.produceResults();
			resultMap.put("a", 1);
		}
	}

	@Test
	public void runMultiNoAbort() throws Exception {
		Experiment e = new AbortingExperiment();

		MultipleReplicationExperiment mre = new MultipleReplicationExperiment();
		mre.setMaxReplications(NUM_RUNS);
		mre.setBaseExperiment(e);
		mre.setAbortUponBaseExperimentAbort(false);
		mre.setAllowParallelExecution(true);

		mre.runExperiment();
		mre.printResults();

		Map<String, Object> res = mre.getResults();

		SummaryStat vs = (SummaryStat) res.get("a");
		assertEquals("numPerformed", NUM_RUNS, vs.numObs());

		assertEquals("aborted", 0, res.get(Experiment.EXP_ABORTED));
	}

	@Test
	public void runMultiAbort() throws Exception {
		Experiment e = new AbortingExperiment();

		MultipleReplicationExperiment mre = new MultipleReplicationExperiment();
		mre.setMaxReplications(NUM_RUNS);
		mre.setBaseExperiment(e);
		mre.setAbortUponBaseExperimentAbort(true);
		mre.setAllowParallelExecution(true);

		mre.runExperiment();
		mre.printResults();

		Map<String, Object> res = mre.getResults();

		SummaryStat vs = (SummaryStat) res.get("a");
		assertEquals("numPerformed", 1, vs.numObs());

		assertEquals("aborted", 1, res.get(Experiment.EXP_ABORTED));
	}

	@Test
	public void runMultiNoAbortSeq() throws Exception {
		Experiment e = new AbortingExperiment();

		MultipleReplicationExperiment mre = new MultipleReplicationExperiment();
		mre.setMaxReplications(NUM_RUNS);
		mre.setBaseExperiment(e);
		mre.setAbortUponBaseExperimentAbort(false);
		mre.setAllowParallelExecution(false);

		mre.runExperiment();
		mre.printResults();

		Map<String, Object> res = mre.getResults();

		SummaryStat vs = (SummaryStat) res.get("a");
		assertEquals("numPerformed", NUM_RUNS, vs.numObs());

		assertEquals("aborted", 0, res.get(Experiment.EXP_ABORTED));
	}

	@Test
	public void runMultiAbortSeq() throws Exception {
		Experiment e = new AbortingExperiment();

		MultipleReplicationExperiment mre = new MultipleReplicationExperiment();
		mre.setMaxReplications(NUM_RUNS);
		mre.setBaseExperiment(e);
		mre.setAbortUponBaseExperimentAbort(true);
		mre.setAllowParallelExecution(false);

		mre.runExperiment();
		mre.printResults();

		Map<String, Object> res = mre.getResults();

		SummaryStat vs = (SummaryStat) res.get("a");
		assertEquals("numPerformed", 1, vs.numObs());

		assertEquals("aborted", 1, res.get(Experiment.EXP_ABORTED));
	}

}
