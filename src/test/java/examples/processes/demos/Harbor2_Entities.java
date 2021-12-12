package examples.processes.demos;

import static jasima.core.simulation.SimContext.waitFor;

import jasima.core.random.continuous.DblExp;
import jasima.core.random.continuous.DblNormal;
import jasima.core.random.continuous.DblSequence;
import jasima.core.simulation.SimEntity;
import jasima.core.simulation.SimProcess.MightBlock;
import jasima.core.simulation.Simulation;
import jasima.core.simulation.generic.Resource;
import jasima.core.util.ConsolePrinter;

public class Harbor2_Entities extends SimEntity {

	private Resource jetties;
	private Resource tugs;
	private DblSequence next;
	private DblSequence discharge;
	int n;
	int wip;

	class Boat extends SimEntity {
		@Override
		public void lifecycle() throws MightBlock {
			System.out.println("n: " + ++n + " " + simTime());
			wip++;
			new Boat().awakeIn(next.nextDbl()); // schedule next arrival

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
	}

	@Override
	protected void lifecycle() throws MightBlock {
		jetties = new Resource("jetties", 2);
		tugs = new Resource("tugs", 3);

		next = initRndGen(new DblExp(0.1), "iat");
		discharge = initRndGen(new DblNormal(14.0, 3), "discharge");

		new Boat().awakeAt(0.0); // schedule first arrival
		waitFor(28.0 * 24.0);

		waitFor(0.0);
		end();

		addResult("wipEnd", wip);
	}

	public static void main(String... args) throws Exception {
		ConsolePrinter.printResults(null, Simulation.of("harbor2", new Harbor2_Entities()));
	}

}
