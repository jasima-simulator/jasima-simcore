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
import static org.junit.Assert.assertEquals;
import jasima.shopSim.models.staticShop.StaticShopExperiment;
import jasima.shopSim.prioRules.basic.FCFS;
import jasima.shopSim.prioRules.basic.TieBreakerFASFS;

import java.io.File;

import org.junit.Test;

/**
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version $Id$
 */
public class TestBatching {

	@Test
	public void basicBatching() throws Exception {
		StaticShopExperiment m = TestStaticInsts.createTstModel(new File(
				"testInstances/batch03x02.txt"), new FCFS()
				.setFinalTieBreaker(new TieBreakerFASFS()));
		m.runExperiment();
		double simTime = (Double) m.getResults().get("simTime");

		assertEquals("CompletionTime", 4, simTime, 1e-6d);
	}

	@Test
	public void incompatibleJobs() throws Exception {
		StaticShopExperiment m = TestStaticInsts.createTstModel(new File(
				"testInstances/batchIncomp1.txt"), new FCFS()
				.setFinalTieBreaker(new TieBreakerFASFS()));
		m.runExperiment();
		double simTime = (Double) m.getResults().get("simTime");

		assertEquals("CompletionTime", 6, simTime, 1e-6d);
	}

}
