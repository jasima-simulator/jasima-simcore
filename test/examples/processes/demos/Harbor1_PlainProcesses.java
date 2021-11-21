package examples.processes.demos;

import static jasima.core.simulation.SimContext.activate;
import static jasima.core.simulation.SimContext.waitFor;
import static jasima.core.simulation.SimContext.waitUntil;

import jasima.core.simulation.SimProcess.MightBlock;
import jasima.core.simulation.Simulation;
import jasima.core.simulation.generic.Resource;
import jasima.core.util.ConsolePrinter;

public class Harbor1_PlainProcesses {

	private Resource jetties;
	private Resource tugs;

	void boatLifecycle() throws MightBlock {
		jetties.seize(1);
		tugs.seize(2);
		waitFor(2.0);
		tugs.release(2);
		waitFor(14.0);
		tugs.seize(1);
		waitFor(2.0);
		tugs.release(1);
		jetties.release(1);
	}

	void harborLifecycle() throws MightBlock {
		jetties = new Resource("jetties", 2);
		tugs = new Resource("tugs", 3);
		// TODO: create activateAt method?
		activate(this::boatLifecycle);
		waitUntil(1.0);
		activate(this::boatLifecycle);
		waitUntil(15.0);
		activate(this::boatLifecycle);
		waitUntil(36.0);
	}

	public static void main(String... args) {
		ConsolePrinter.printResults(null, Simulation.of("harbor1", new Harbor1_PlainProcesses()::harborLifecycle));
	}

}
