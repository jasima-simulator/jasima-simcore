package examples.processes;

import static jasima.core.simulation.processes.SimContext.activate;
import static jasima.core.simulation.processes.SimContext.waitFor;

import java.util.Random;

import jasima.core.random.continuous.DblExp;
import jasima.core.random.continuous.DblStream;
import jasima.core.simulation.Simulation;
import jasima.core.simulation.processes.SimProcess;
import jasima.core.util.MsgCategory;

public class MM1Queue {

	private Simulation sim;
	private DblStream iat;
	private DblStream serviceTimes;

	public static void main(String[] args) {
		new MM1Queue().run();
	}

	void generatorProcess() {
		for (int i = 0; i < 4; i++) {
			String name = "customer" + i;

			waitFor(iat.nextDbl());
			sim.trace("generator", i);

			activate(() -> serveCustomer(name));
		}
	}

	void serveCustomer(String name) {
		waitFor(serviceTimes.nextDbl());
		System.out.println("Customer " + name + " served.");
	}

	private void run() {
		iat = new DblExp(1.0);
		iat.setRndGen(new Random(23));

		serviceTimes = new DblExp(1.0);
		serviceTimes.setRndGen(new Random(42));

		sim = new Simulation();

		sim.setPrintLevel(MsgCategory.ALL);
		sim.addPrintListener(System.out::println);

		new SimProcess(sim, this::generatorProcess);

		sim.performRun();
	}

}
