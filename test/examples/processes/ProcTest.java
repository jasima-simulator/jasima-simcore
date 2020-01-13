package examples.processes;

import static jasima.core.simulation.processes.SimContext.activate;
import static jasima.core.simulation.processes.SimContext.currentSimulation;
import static jasima.core.simulation.processes.SimContext.*;
import static java.util.Objects.requireNonNull;

import jasima.core.simulation.Simulation;
import jasima.core.simulation.processes.MightBlock;
import jasima.core.simulation.processes.SimProcess;
import jasima.core.util.MsgCategory;

public class ProcTest {

	public static void main(String[] args) {
		Simulation sim = new Simulation();
		sim.setPrintLevel(MsgCategory.ALL);
		sim.addPrintListener(System.out::println);

		new SimProcess<>(sim, ProcTest::xxx).awakeIn(0.0);

		sim.performRun();
	}

	public static void xxx() throws MightBlock {
		Simulation sim = requireNonNull(currentSimulation());

		for (int i = 0; i < 3; i++) {
			waitFor(1.0);
			sim.trace("generator", i);

			String name = "sub" + i;
			activate(() -> ProcTest.xxx2(name));
		}
		
		sim.trace("generator suspends now");
		suspend();

		sim.trace("never executes");
	}

	public static void xxx2(String name) throws MightBlock {
		Simulation sim = requireNonNull(currentSimulation());

		waitFor(1.2);
		sim.trace(name, "1");
		waitFor(1.2);
		sim.trace(name, "2");
		waitFor(1.2);
		sim.trace(name, "3");
	}

}
