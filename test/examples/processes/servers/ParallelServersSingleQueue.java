package examples.processes.servers;

import static jasima.core.simulation.SimContext.waitFor;

import java.util.Map;

import jasima.core.random.continuous.DblNormal;
import jasima.core.random.continuous.DblSequence;
import jasima.core.random.discrete.IntUniformRange;
import jasima.core.simulation.SimContext;
import jasima.core.simulation.SimEntity;
import jasima.core.simulation.SimEvent;
import jasima.core.simulation.SimProcess.MightBlock;
import jasima.core.simulation.generic.Q;
import jasima.core.simulation.generic.QStatCollector;
import jasima.core.statistics.SummaryStat;
import jasima.core.util.ConsolePrinter;

public class ParallelServersSingleQueue extends SimEntity {

	public static void main(String[] args) throws Exception {
		Map<String, Object> res = SimContext.of(sim -> sim.activate(new ParallelServersSingleQueue()));
		ConsolePrinter.printResults(null, res);
	}

	// parameters

	private int numServers = 5;

	// fields used during run

	private SummaryStat flowtimes = new SummaryStat();
	private Server[] servers;
	private Q<Customer> q;

	private DblSequence serviceTimes;

	public class Customer extends SimEntity {
		@Override
		protected void lifecycle() throws MightBlock {
			double entered = simTime();
			q.put(this);
			getLifecycleProcess().suspend();
			flowtimes.value(simTime() - entered);
		}
	}

	public class Server extends SimEntity {
		private SummaryStat services = new SummaryStat();
		
		public Server(String name) {
			super(name);
		}

		@Override
		protected void lifecycle() throws MightBlock {
			while (true) {
				Customer c = q.take();
				double serviceStart = simTime();
				waitFor(serviceTimes.nextDbl());
				services.value(simTime()-serviceStart);
				c.getLifecycleProcess().resume();
			}
		}

		@Override
		public void produceResults(Map<String, Object> res) {
			super.produceResults(res);
			res.put(getHierarchicalName()+".serviceTimes", new SummaryStat(services));
		}
		
	}

	@Override
	protected void lifecycle() throws MightBlock {
		DblSequence arrivals = initRndGen(new IntUniformRange(1, 3), "arrivals");
		serviceTimes = initRndGen(new DblNormal(8.0, 2.0), "services");
		getSim().setSimulationLength(100 * 60.0);

		servers = new Server[getNumServers()];
		for (int i = 0; i < getNumServers(); i++) {
			servers[i] = addComponent(new Server("server" + (i + 1)));
		}

		q = new Q<>();
		new QStatCollector(q, getSim());
		trace("simulation start");

		scheduleProcess(SimEvent.EVENT_PRIO_NORMAL, () -> {
			activate(new Customer());
			return arrivals.nextDbl();
		});
	}

	@Override
	public void produceResults(Map<String, Object> res) {
		super.produceResults(res);
		trace("simulation finished");
		res.put("flowtimes", new SummaryStat(flowtimes));
	}

	// boring getters / setters below

	/**
	 * @return the numServers
	 */
	public int getNumServers() {
		return numServers;
	}

	/**
	 * @param numServers the numServers to set
	 */
	public void setNumServers(int numServers) {
		this.numServers = numServers;
	}

}
