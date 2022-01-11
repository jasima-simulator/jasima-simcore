package jasima.core.simulation;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * 
 * @author Torsten Hildebrandt
 */
@RunWith(Suite.class)
@SuiteClasses({ TestSimContext.class, TestSimProcessBasics.class, TestSimulationBasics.class,
		TestSimulationControlFlow.class, TestSimulationPausing.class, TestComponentHierarchy.class,
		TestComponentInit.class })
public class AllTests {

}
