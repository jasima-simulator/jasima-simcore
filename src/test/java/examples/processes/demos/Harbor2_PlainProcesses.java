package examples.processes.demos;

import static jasima.core.simulation.SimContext.*;
import static jasima.core.simulation.SimContext.end;
import static jasima.core.simulation.SimContext.initRndGen;
import static jasima.core.simulation.SimContext.scheduleIn;
import static jasima.core.simulation.SimContext.simTime;
import static jasima.core.simulation.SimContext.waitFor;

import jasima.core.random.continuous.DblExp;
import jasima.core.random.continuous.DblNormal;
import jasima.core.random.continuous.DblSequence;
import jasima.core.simulation.SimProcess.MightBlock;
import jasima.core.simulation.Simulation;
import jasima.core.simulation.generic.Resource;
import jasima.core.util.ConsolePrinter;

public class Harbor2_PlainProcesses {

	private Resource jetties;
	private Resource tugs;
	private DblSequence next;
	private DblSequence discharge;
	int n;
	int wip;

	void boatLifecycle() throws MightBlock {
		System.out.println("n: " + ++n + " " + simTime());
		wip++;
		scheduleIn(next.nextDbl(), 0, () -> activate(this::boatLifecycle)); // schedule next arrival

		jetties.seize(1);

		tugs.seize(2);
		waitFor(2.0);
		tugs.release(2);

		waitFor(discharge.nextDbl());

		tugs.seize(1);
		waitFor(2.0);
		tugs.release(1);

		jetties.release(1);
		wip--;
	}

	void harborLifecycle() throws MightBlock {
		jetties = new Resource("jetties", 2);
		tugs = new Resource("tugs", 3);

		next = initRndGen(new DblExp(10.0), "iat");
		discharge = initRndGen(new DblNormal(14.0, 3), "discharge");

		activate(this::boatLifecycle); // schedule first arrival
		waitFor(28.0 * 24.0);

		waitFor(0.0);
		end();

		addResult("wipEnd", wip);
	}

	public static void main(String... args) throws Exception {
		ConsolePrinter.printResults(Simulation.of("harbor2", new Harbor2_PlainProcesses()::harborLifecycle));
	}

}
