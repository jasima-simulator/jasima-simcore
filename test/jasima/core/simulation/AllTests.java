package jasima.core.simulation;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * 
 * @author Torsten Hildebrandt
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ TestSimulationBasics.class, TestSimulationPausing.class, TestSimulationControlFlow.class,
		jasima.core.simulation.generic.AllTests.class, TestSimProcessBasics.class })
public class AllTests {

}
