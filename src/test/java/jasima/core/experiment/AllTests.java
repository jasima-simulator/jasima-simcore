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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * 
 * @author Torsten Hildebrandt
 */
@RunWith(Suite.class)
@SuiteClasses({ OCBATest.class, Test_FFE_Array_Setter.class, TestExperimentAbort.class, TestExperimentBasics.class,
		TestExperimentCancellation.class, TestExperimentExceptions.class, TestFFEFactorSetting.class,
		TestMultipleReplicationExperiment.class })

public class AllTests {

}
