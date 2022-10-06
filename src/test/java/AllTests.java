
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
