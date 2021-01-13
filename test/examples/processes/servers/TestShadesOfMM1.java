package examples.processes.servers;

import static jasima.core.simulation.SimContext.activate;
import static jasima.core.simulation.SimContext.end;
import static jasima.core.simulation.SimContext.trace;
import static jasima.core.simulation.SimContext.waitFor;
import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import jasima.core.random.RandomFactory;
import jasima.core.random.continuous.DblExp;
import jasima.core.random.continuous.DblSequence;
import jasima.core.run.ConsoleRunner;
import jasima.core.simulation.SimComponentBase;
import jasima.core.simulation.SimContext;
import jasima.core.simulation.SimProcess;
import jasima.core.simulation.Simulation;
import jasima.core.simulation.SimulationExperiment;
import jasima.core.simulation.generic.Q;
import jasima.core.simulation.generic.Q.QListener.ItemAdded;
import jasima.core.simulation.generic.Resource;
import jasima.core.util.ConsolePrinter;
import jasima.core.util.MsgCategory;
import jasima.core.util.SimProcessUtil.SimAction;
import jasima.core.util.TypeRef;

public class TestShadesOfMM1 {

	private static final double INTER_ARRIVAL_TIME = 1.0;

	@Test
	public void testProc1() {
		AtomicInteger numServed = new AtomicInteger(0);
		AtomicInteger numCreated = new AtomicInteger(0);

		Map<String, Object> res = SimContext.of(sim -> {
			RandomFactory rf = new RandomFactory(sim, 23);
			int numJobs = 10;
			double trafficIntensity = 0.85;

			// the queue
			Q<Integer> q = new Q<>();

			// source as SimProcess from lambda
			activate(() -> {
				DblSequence iats = rf.initRndGen(new DblExp(INTER_ARRIVAL_TIME), "arrivals");
				for (int n = 0; n < numJobs; n++) {
					waitFor(iats.nextDbl());
					trace("created job", n);
					q.put(n);
					numCreated.incrementAndGet();
				}
				end();
			});

			// service as SimProcess from lambda
			activate(() -> {
				DblSequence serviceTimes = rf.initRndGen(new DblExp(INTER_ARRIVAL_TIME * trafficIntensity), "services");
				while (true) {
					Integer job = q.take();
					trace("procStarted", job);
					waitFor(serviceTimes.nextDbl());
					numServed.incrementAndGet();
					trace("procFinished", job);
				}
			});
		});
		ConsolePrinter.printResults(null, res);

		assertEquals("numCreated", 10, numCreated.get());
		assertEquals("numServed", 6, numServed.get());
		assertEquals("simTime", 14.716741001406954, (Double) res.get("simTime"), 1e-6);
	}

	@Test
	public void testTransactionOriented() {
		AtomicInteger numServed = new AtomicInteger(0);
		AtomicInteger numCreated = new AtomicInteger(0);

		Map<String, Object> res = SimContext.of(sim -> {
			sim.setPrintLevel(MsgCategory.ALL);
			sim.addPrintListener(System.out::println);

			RandomFactory rf = new RandomFactory(sim, 23);
			int numJobs = 10;
			double trafficIntensity = 0.85;

			// the queue
			Q<SimProcess<?>> q = new Q<>();
			Resource server = new Resource("server", 1);

			DblSequence iats = rf.initRndGen(new DblExp(INTER_ARRIVAL_TIME), "arrivals");
			DblSequence serviceTimes = rf.initRndGen(new DblExp(INTER_ARRIVAL_TIME * trafficIntensity), "services");
			
			SimAction jobLifecycle = s -> {
				trace("created job");
				numCreated.incrementAndGet();
				Q.enter(q);
				trace("entered queue");
				server.seize();
				trace("seized server");
				Q.leave(q);
				trace("procStarted");
				waitFor(serviceTimes.nextDbl());
				numServed.incrementAndGet();
				trace("procFinished");
				server.release();
			};

			// source as SimProcess from lambda
			activate(() -> {
				for (int n = 0; n < numJobs; n++) {
					waitFor(iats.nextDbl());
					activate(jobLifecycle);
				}
				waitFor(1.0);
				end();
			});
		});
		ConsolePrinter.printResults(null, res);

		assertEquals("numCreated", 10, numCreated.get());
		assertEquals("numServed", 6, numServed.get());
		assertEquals("simTime", 14.716741001406954, (Double) res.get("simTime"), 1e-6);
	}

	static class MM1Model extends SimComponentBase {
		// parameters
		private int numJobs = 10;
		private double trafficIntensity = 0.85;

		// fields used during run
		private int numServed = 0;
		private int numCreated = 0;
		private DblSequence iats;
		private DblSequence serviceTimes;
		private Q<Integer> q;
		private Integer currentJob;

		@Override
		public void init() {
			super.init();
			RandomFactory rf = new RandomFactory(getSim(), 23);
			iats = rf.initRndGen(new DblExp(INTER_ARRIVAL_TIME), "arrivals");
			serviceTimes = rf.initRndGen(new DblExp(INTER_ARRIVAL_TIME * trafficIntensity), "services");
			q = new Q<>();
			q.addListener(new TypeRef<ItemAdded<Integer>>() {
			}, (q, item) -> checkStartService());

			scheduleIn(iats.nextDbl(), getSim().currentPrio(), this::createNext);
		}

		@Override
		public void produceResults(Map<String, Object> res) {
			super.produceResults(res);
			res.put("numCreated", numCreated);
			res.put("numServed", numServed);
		}

		void createNext() {
			int n = numCreated++;
			if (!q.tryPut(n)) {
				throw new IllegalStateException("can't put in queue?");
			}
			SimContext.trace("created job", n);
			checkStartService();
			if (numCreated < numJobs) {
				scheduleIn(iats.nextDbl(), getSim().currentPrio(), this::createNext);
			} else {
				end();
			}
		}

		void checkStartService() {
			if (q.numItems() == 0 || currentJob != null) {
				return; // nothing to do
			}
			currentJob = requireNonNull(q.tryTake());
			SimContext.trace("procStarted", currentJob);
			scheduleIn(serviceTimes.nextDbl(), getSim().currentPrio(), this::finishedService);
		}

		void finishedService() {
			SimContext.trace("procFinished", currentJob);
			currentJob = null;
			numServed++;
			checkStartService();
		}

	}

	@Test
	public void testEventOriented1() {
		Simulation sim = new Simulation();
		sim.addComponent(new MM1Model());
		Map<String, Object> res = sim.performRun();

		ConsolePrinter.printResults(null, res);

		assertEquals("numCreated", 10, (int) res.get("numCreated"));
		assertEquals("numServed", 6, (int) res.get("numServed"));
		assertEquals("simTime", 14.716741001406954, (Double) res.get("simTime"), 1e-6);
	}

	static class MM1Experiment extends SimulationExperiment {
		// parameters
		private int numJobs = 10;
		private double trafficIntensity = 0.85;

		// fields used during run
		private int numServed = 0;
		private int numCreated = 0;
		private DblSequence iats;
		private DblSequence serviceTimes;
		private Q<Integer> q;
		private Integer currentJob;

		@Override
		public void init() {
			super.init();
			iats = initRndGen(new DblExp(INTER_ARRIVAL_TIME), "arrivals");
			serviceTimes = initRndGen(new DblExp(INTER_ARRIVAL_TIME * trafficIntensity), "services");
			q = new Q<>();
			q.addListener(new TypeRef<ItemAdded<Integer>>() {
			}, (q, item) -> checkStartService());

			scheduleIn(iats.nextDbl(), getSim().currentPrio(), this::createNext);
		}

		@Override
		public void produceResults() {
			super.produceResults();
			resultMap.put("numCreated", numCreated);
			resultMap.put("numServed", numServed);
		}

		void createNext() {
			int n = numCreated++;
			if (!q.tryPut(n)) {
				throw new IllegalStateException("can't put in queue?");
			}
			SimContext.trace("created job", n);
			checkStartService();
			if (numCreated < numJobs) {
				scheduleIn(iats.nextDbl(), getSim().currentPrio(), this::createNext);
			} else {
				end();
			}
		}

		void checkStartService() {
			if (q.numItems() == 0 || currentJob != null) {
				return; // nothing to do
			}
			currentJob = requireNonNull(q.tryTake());
			SimContext.trace("procStarted", currentJob);
			scheduleIn(serviceTimes.nextDbl(), getSim().currentPrio(), this::finishedService);
		}

		void finishedService() {
			SimContext.trace("procFinished", currentJob);
			currentJob = null;
			numServed++;
			checkStartService();
		}

		/**
		 * @return the numJobs
		 */
		public int getNumJobs() {
			return numJobs;
		}

		/**
		 * @param numJobs the numJobs to set
		 */
		public void setNumJobs(int numJobs) {
			this.numJobs = numJobs;
		}

		/**
		 * @return the trafficIntensity
		 */
		public double getTrafficIntensity() {
			return trafficIntensity;
		}

		/**
		 * @param trafficIntensity the trafficIntensity to set
		 */
		public void setTrafficIntensity(double trafficIntensity) {
			this.trafficIntensity = trafficIntensity;
		}

	}

	@Test
	public void testExperimentEventOriented() {
		MM1Experiment exp = new MM1Experiment();
		exp.setInitialSeed(23);
		Map<String, Object> res = exp.runExperiment();

		ConsolePrinter.printResults(null, res);

		assertEquals("numCreated", 10, (int) res.get("numCreated"));
		assertEquals("numServed", 6, (int) res.get("numServed"));
		assertEquals("simTime", 14.716741001406954, (Double) res.get("simTime"), 1e-6);
	}

	@Test
	public void testExperimentEventOriented2() {
		MM1Experiment exp = new MM1Experiment();
		exp.setInitialSeed(23);
		exp.setNumJobs(10);

		Map<String, Object> res = ConsoleRunner.runWithArgs(exp);

		assertEquals("numCreated", 10, (int) res.get("numCreated"));
		assertEquals("numServed", 6, (int) res.get("numServed"));
		assertEquals("simTime", 14.716741001406954, (Double) res.get("simTime"), 1e-6);
	}

}
