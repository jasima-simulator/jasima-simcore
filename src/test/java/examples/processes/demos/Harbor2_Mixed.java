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
package examples.processes.demos;

import static jasima.core.simulation.SimContext.activate;
import static jasima.core.simulation.SimContext.waitFor;

import jasima.core.random.continuous.DblExp;
import jasima.core.random.continuous.DblNormal;
import jasima.core.random.continuous.DblSequence;
import jasima.core.simulation.SimEntity;
import jasima.core.simulation.SimProcess.MightBlock;
import jasima.core.simulation.Simulation;
import jasima.core.simulation.generic.Resource;
import jasima.core.util.ConsolePrinter;

public class Harbor2_Mixed extends SimEntity {

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

	@Override
	public void lifecycle() throws MightBlock {
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
		ConsolePrinter.printResults(Simulation.of("harbor2", new Harbor2_Mixed()));
	}

}
