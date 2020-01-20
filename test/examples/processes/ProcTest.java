package examples.processes;

import static jasima.core.simulation.SimContext.activate;
import static jasima.core.simulation.SimContext.suspend;
import static jasima.core.simulation.SimContext.trace;
import static jasima.core.simulation.SimContext.waitFor;

import java.util.Map;

import jasima.core.simulation.SimContext;
import jasima.core.simulation.SimProcess.MightBlock;
import jasima.core.util.ConsolePrinter;

public class ProcTest {

	public static void main(String[] args) {
		System.out.println("Before sim");
		Map<String, Object> res = SimContext.of(ProcTest::xxx);
		System.out.println("After sim");
		ConsolePrinter.printResults(null, res);
	}

	public static void xxx() throws MightBlock {
		for (int i = 0; i < 1; i++) {
			waitFor(1.0);
			trace("generator", i);
			activate(ProcTest::xxx2, "sub" + i);
		}
		trace("generator suspends now");
		suspend();
		trace("never executes");
	}

	public static void xxx2() throws MightBlock {
		waitFor(1.2);
		trace("1");
		waitFor(1.2);
		trace("2");
		waitFor(1.2);
		trace("3");
	}

}
