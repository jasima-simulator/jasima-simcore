package jasima.core.simulation;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * 
 * @author Torsten Hildebrandt
 */
@RunWith(Suite.class)
@SuiteClasses({ TestSimProcessBasics.class, TestSimulationBasics.class, TestSimulationControlFlow.class,
		TestSimulationPausing.class })

public class AllTests {

}
