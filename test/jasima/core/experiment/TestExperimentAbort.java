/*******************************************************************************
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This file is part of jasima, v1.2.
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
package jasima.core.experiment;

import static org.junit.Assert.assertEquals;
import jasima.core.statistics.SummaryStat;

import java.util.Map;

import org.junit.Test;

/**
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version "$Id$"
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
