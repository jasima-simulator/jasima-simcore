
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
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * 
 * @author Torsten Hildebrandt
 */
@RunWith(Suite.class)
@SuiteClasses({ HolthausSimpleTest.class, JobShopTests.class, TestStaticInsts.class, TestStaticInstsTraces.class,
		TestOrderIndependence.class, TestSetups.class, TestBatching.class, TestBestOfFamilyBatching.class,
		TestGECCOContinuity.class, TestWinterSim2010Continuity.class, TestMIMAC.class, TestForAllResults.class,
		TestMimacFab4Trace.class, jasima.core.experiment.AllTests.class, TestDetailedTraces.class, TestDowntimes.class,
		jasima.core.util.AllTests.class, jasima.core.statistics.AllTests.class, jasima.core.simulation.AllTests.class,
		jasima.core.random.continuous.AllTests.class, jasima.core.run.AllTests.class })
public class AllTests {

}
