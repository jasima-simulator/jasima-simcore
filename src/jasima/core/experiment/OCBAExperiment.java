package jasima.core.experiment;

import jasima.core.statistics.SummaryStat;
import jasima.core.util.Pair;
import jasima.core.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * Purpose of this class is to find the best configuration/parameterization of a
 * base experiment (subject to random effects) using the Optimal Computing
 * Budget Allocation (OCBA) method. In contrast to simply running a
 * {@link FullFactorialExperiment} performing a fixed number of replications for
 * each configuration, this class aims at intelligently distributing a given
 * budget of base experiment runs in order to maximize the probability of
 * actually selecting the best configuration (Probability of Correct Selection,
 * PCS).
 * <p />
 * Implements the OCBA method as described in Chen2000: Chen, C. H., J. Lin, E.
 * Yücesan, and S. E. Chick,
 * "Simulation Budget Allocation for Further Enhancing the Efficiency of Ordinal Optimization,"
 * Journal of Discrete Event Dynamic Systems: Theory and Applications, Vol. 10,
 * pp. 251-270, July 2000.
 * <p />
 * First minReplicationsPerConfiguration replications (default: 5) are performed
 * for each configuration. Later on runs are allocated dynamically using OCBA.
 * The total budget is given by: getNumReplications() (default: 10)*<number of
 * configurations>
 * <p />
 * To use this class at least the name of the objective value (
 * {@link #setObjective(String)}) and whether this objective is to be maximized
 * or minimized (setMaximize()) have to be set.
 * <p />
 * Each iteration of the allocation algorithm allocates more than a single run
 * in order to benefit from parallelization.
 * <p />
 * A usage example is given below. It selects the best of two dispatching rules
 * of a dynamic job shop scenario.
 * 
 * <blockquote>
 * 
 * <pre>
 * // create and configure base experiment
 * HolthausExperiment he = new HolthausExperiment();
 * he.setUtilLevel(0.9);
 * he.addShopListener(new BasicJobStatCollector());
 * 
 * // create OCBA experiment
 * OCBAExperiment ocbaExperiment = new OCBAExperiment();
 * ocbaExperiment.setInitialSeed(23);
 * 
 * // set base experiment to use
 * ocbaExperiment.setBaseExperiment(he);
 * 
 * // define configurations to test
 * ocbaExperiment.addFactor(&quot;sequencingRule&quot;,
 * 		new SPT().setFinalTieBreaker(new TieBreakerFASFS()));
 * ocbaExperiment.addFactor(&quot;sequencingRule&quot;,
 * 		new PTPlusWINQPlusNPT().setFinalTieBreaker(new TieBreakerFASFS()));
 * 
 * // define objective function
 * ocbaExperiment.setObjective(&quot;flowMean&quot;);
 * ocbaExperiment.setProblemType(ProblemType.MINIMIZE);
 * 
 * // no fixed budget, run until we are pretty sure to have the best
 * // configuration
 * ocbaExperiment.setNumReplications(0);
 * ocbaExperiment.setPcsLevel(0.95);
 * 
 * // optionally produce an Excel file with results and details
 * ocbaExperiment.addNotifierListener(new ExcelSaver());
 * 
 * // run
 * ocbaExperiment.runExperiment();
 * ocbaExperiment.printResults();
 * </pre>
 * 
 * </blockquote>
 * <p />
 * This class implements a basic ranking and selection method. In future
 * versions it would be very helpful to improve its algorithm to better deal
 * with very similar performances of good configurations (such as indifference
 * zone approaches, or using Expected Opportunity Costs). I'm also unsure about
 * the effects if base experiments use common random numbers. In summary, this
 * class is not a fool-proof intelligent allocator of replications, but should
 * provide reasonably good results to be useful. Probably it's also a good
 * starting point for experts in the field to implement (and contribute?)
 * improved algorithms.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version $Id$
 * 
 * @see MultipleReplicationExperiment
 * @see FullFactorialExperiment
 */
public class OCBAExperiment extends FullFactorialExperiment {

	private static final long serialVersionUID = 621315272493464195L;

	public enum ProblemType {
		MINIMIZE, MAXIMIZE
	};

	//
	// experiment parameters
	//

	private String objective;
	private ProblemType problemType = ProblemType.MINIMIZE;
	private int minReplicationsPerConfiguration = 5;
	private int numReplications = 10;
	private double pcsLevel = 0.0;
	private boolean detailedResults = true;

	//
	// fields used during experiment run
	//

	private int totalBudget, iterationBudget, budgetUsed;
	private ArrayList<MultipleReplicationExperiment> configurations;
	private SummaryStat[] stats;
	private double finalPCS;
	private int currBest;

	public OCBAExperiment() {
		super();
		setProduceAveragedResults(false);
	}

	@Override
	public void init() {
		super.init();
	}

	@Override
	protected void createExperiments() {
		// only perform once, not each iteration
		if (getNumTasksExecuted() == 0) {
			super.createExperiments();

			stats = Util
					.initializedArray(experiments.size(), SummaryStat.class);

			configurations = new ArrayList<MultipleReplicationExperiment>();
			for (Experiment e : experiments) {
				MultipleReplicationExperiment mre = (MultipleReplicationExperiment) e;
				mre.setMaxReplications(getMinReplicationsPerConfiguration());
				configurations.add(mre);
			}

			totalBudget = configurations.size() * getNumReplications();

			iterationBudget = Math.round(0.1f * configurations.size());
			if (iterationBudget < getMinReplicationsPerConfiguration())
				iterationBudget = getMinReplicationsPerConfiguration();
			int numProc = Runtime.getRuntime().availableProcessors();
			if (iterationBudget < numProc)
				iterationBudget = numProc;

			budgetUsed = 0;
		}
	}

	@Override
	protected Experiment createExperimentForConf(
			ArrayList<Pair<String, Object>> conf) {
		Experiment e = super.createExperimentForConf(conf);

		MultipleReplicationExperiment mre = new MultipleReplicationExperiment();
		mre.setBaseExperiment(e);
		configureRunExperiment(mre);
		//
		// for (Pair<String, Object> p : confs) {
		// if (p.b != null && p.b instanceof ComplexFactorSetter) {
		// ((ComplexFactorSetter) p.b).configureExperiment(e);
		// } else
		// Util.setProperty(e, p.a, p.b);
		// }

		return mre;
	}

	@Override
	protected void done() {
		super.done();

		finalPCS = calcPCS();
	}

	@Override
	public void produceResults() {
		super.produceResults();

		resultMap.put("bestConfiguration", configurations.get(currBest)
				.getBaseExperiment());
		resultMap.put("bestIndex", currBest);
		resultMap.put("bestPerformance", stats[currBest].mean());

		resultMap.put("numEvaluations", budgetUsed);
		resultMap.put("pcs", finalPCS);

		if (isDetailedResults()) {
			// allocation of evaluations to configurations
			int[] numRuns = new int[configurations.size()];
			double[] means = new double[stats.length];
			Experiment[] exps = new Experiment[configurations.size()];
			for (int i = 0; i < configurations.size(); i++) {
				exps[i] = configurations.get(i).getBaseExperiment();

				SummaryStat vs = stats[i];
				numRuns[i] = vs.numObs();
				means[i] = vs.mean();
			}
			resultMap.put("allocationVector", numRuns);
			resultMap.put("meansVector", means);
			resultMap.put("configurations", exps);
			// probability of configuration assumed being best to be better than
			// another configuration
			resultMap.put("probBestBetter", calcPCSPriosPerConfiguration());
			resultMap.put("rank", findRank(means));
		}
	}

	private int[] findRank(final double[] means) {
		Integer[] idx = new Integer[means.length];
		for (int i = 0; i < idx.length; i++) {
			idx[i] = i;
		}

		Arrays.sort(idx, new Comparator<Integer>() {
			@Override
			public int compare(Integer i1, Integer i2) {
				return (getProblemType() == ProblemType.MAXIMIZE ? -1 : +1)
						* Double.compare(means[i1.intValue()],
								means[i2.intValue()]);
			}
		});

		int[] ranks = new int[idx.length];
		for (int i = 0; i < ranks.length; i++) {
			ranks[idx[i].intValue()] = i + 1;
		}

		return ranks;
	}

	@Override
	protected boolean hasMoreTasks() {
		// identify currently best system
		currBest = 0;
		double bestMean = getProblemType() == ProblemType.MAXIMIZE ? stats[0]
				.mean() : -stats[0].mean();
		for (int i = 1; i < stats.length; i++) {
			double v = getProblemType() == ProblemType.MAXIMIZE ? stats[i]
					.mean() : -stats[i].mean();
			if (v > bestMean) {
				bestMean = v;
				currBest = i;
			}
		}

		experiments.clear();

		// check stopping conditions
		if ((totalBudget > 0 && budgetUsed >= totalBudget)
				|| (getPcsLevel() > 0.0 && calcPCS() > getPcsLevel()))
			return false;

		// allocate new iterations
		int iter = iterationBudget;
		if (totalBudget > 0)
			iter = Math.min(iter, totalBudget - budgetUsed);

		int[] newRuns = ocba(iter);
		// System.out.println(Arrays.toString(newRuns));

		// configure new experiments to be performed
		for (int i = 0; i < newRuns.length; i++) {
			if (newRuns[i] > 0) {
				MultipleReplicationExperiment mre = configurations.get(i);
				mre.setMaxReplications(newRuns[i]);
				experiments.add(mre);
			}
		}

		return true;
	}

	@Override
	protected void storeRunResults(Experiment e, Map<String, Object> r) {
		super.storeRunResults(e, r);

		// update statistics for this configuration
		int i = configurations.indexOf(e);
		assert i >= 0;

		Object o = r.get(getObjective());
		if (o == null)
			throw new RuntimeException(
					"Can't find result value for objective '" + getObjective()
							+ "'.");

		budgetUsed += configurations.get(i).getMaxReplications();

		SummaryStat vs = stats[i];
		if (o instanceof Number) {
			vs.value(((Number) o).doubleValue());
		} else if (o instanceof SummaryStat) {
			vs.combine((SummaryStat) o);
		} else
			throw new RuntimeException("Don't know how to handle result '"
					+ String.valueOf(o) + "'.");
	}

	protected double calcPCS() {
		double[] prodTerms = calcPCSPriosPerConfiguration();

		double res = 1.0d;
		for (int i = 0; i < prodTerms.length; i++) {
			if (i == currBest)
				continue;

			res *= prodTerms[i];
		}

		return res;
	}

	protected double[] calcPCSPriosPerConfiguration() {
		final SummaryStat best = stats[currBest];
		final double bestMean = best.mean();

		double bestNormVariance = best.variance() / best.numObs();

		double[] prodTerms = new double[stats.length];
		for (int i = 0; i < stats.length; i++) {
			if (i == currBest)
				continue;

			SummaryStat vs = stats[i];
			prodTerms[i] = (bestMean - vs.mean())
					/ Math.sqrt(bestNormVariance + vs.variance() / vs.numObs());
		}

		NormalDistribution normalDist = new NormalDistribution();

		for (int i = 0; i < stats.length; i++) {
			if (i == currBest)
				continue;

			prodTerms[i] = normalDist.cumulativeProbability(prodTerms[i]);
			if (getProblemType() == ProblemType.MINIMIZE)
				prodTerms[i] = 1.0 - prodTerms[i];
		}

		return prodTerms;
	}

	/**
	 * This subroutine implements the optimal computation budget allocation
	 * (OCBA) algorithm presented in Chen et al. (2000) in the J of DEDS. It
	 * determines how many additional runs each design will should have for next
	 * iteration of simulation.
	 * 
	 * @param s_mean
	 *            [i]: sample mean of design i, i=0,1,..,ND-1
	 * 
	 * @param s_var
	 *            [i]: sample variance of design i, i=0,1,..,ND-1
	 * 
	 * @param n
	 *            [i]: number of simulation replication of design i,
	 *            i=0,1,..,ND-1
	 * 
	 * @param add_budget
	 *            : the additional simulation budget
	 * 
	 * @param type
	 *            : type of optimization problem. type=1, MIN problem; type=2,
	 *            MAX problem
	 * 
	 * @return additional number of simulation replication assigned to design i,
	 *         i=0,1,..,ND-1
	 */
	public int[] ocba(int add_budget) {
		final int nd = stats.length;

		double t_s_mean[] = new double[nd];
		if (getProblemType() == ProblemType.MAXIMIZE) { /* MAX problem */
			for (int i = 0; i < nd; i++)
				t_s_mean[i] = -stats[i].mean();
		} else { /* MIN problem */
			for (int i = 0; i < nd; i++)
				t_s_mean[i] = stats[i].mean();
		}

		int t_budget = add_budget;

		for (int i = 0; i < nd; i++)
			t_budget += stats[i].numObs();

		int b = currBest;
		assert b == best(t_s_mean) : "" + b + " " + best(t_s_mean) + " "
				+ t_s_mean[b] + " " + t_s_mean[best(t_s_mean)] + " "
				+ stats[b].mean() + " " + stats[best(t_s_mean)].mean();

		int s = second_best(t_s_mean, b);

		double ratio[] = new double[nd];
		ratio[s] = 1.0d;
		for (int i = 0; i < nd; i++)
			if (i != s && i != b) {
				double temp = (t_s_mean[b] - t_s_mean[s])
						/ (t_s_mean[b] - t_s_mean[i]);
				ratio[i] = temp * temp * stats[i].variance()
						/ stats[s].variance();
			} /* calculate ratio of Ni/Ns */

		double temp = 0.0;
		for (int i = 0; i < nd; i++)
			if (i != b)
				temp += (ratio[i] * ratio[i] / stats[i].variance());
		ratio[b] = Math.sqrt(stats[b].variance() * temp); /* calculate Nb */

		int morerun[] = new int[nd];
		for (int i = 0; i < nd; i++)
			morerun[i] = 1;

		int t1_budget = t_budget;

		int[] an = new int[nd];
		boolean more_alloc;
		do {
			more_alloc = false;
			double ratio_s = 0.0f;
			for (int i = 0; i < nd; i++)
				if (morerun[i] == 1)
					ratio_s += ratio[i];

			for (int i = 0; i < nd; i++)
				if (morerun[i] == 1) {
					an[i] = (int) (t1_budget / ratio_s * ratio[i]);
					/* disable thoese design which have benn run too much */
					if (an[i] < stats[i].numObs()) {
						an[i] = stats[i].numObs();
						morerun[i] = 0;
						more_alloc = true;
					}
				}

			if (more_alloc) {
				t1_budget = t_budget;
				for (int i = 0; i < nd; i++)
					if (morerun[i] != 1)
						t1_budget -= an[i];
			}
		} while (more_alloc); /* end of WHILE */

		/* calculate the difference */
		t1_budget = an[0];

		for (int i = 1; i < nd; i++)
			t1_budget += an[i];

		an[b] += (t_budget - t1_budget); /* give the difference to design b */

		for (int i = 0; i < nd; i++)
			an[i] -= stats[i].numObs();

		return an;
	}

	/**
	 * This function determines the best design based on current simulation
	 * results.
	 * 
	 * @param t_s_mean
	 *            [i]: temporary array for sample mean of design i,
	 *            i=0,1,..,ND-1
	 */
	public static int best(double[] t_s_mean) {
		int min_index = 0;
		for (int i = 0; i < t_s_mean.length; i++) {
			if (t_s_mean[i] < t_s_mean[min_index]) {
				min_index = i;
			}
		}
		return min_index;
	}

	/**
	 * This function determines the second best design based on current
	 * simulation results.
	 * 
	 * @param t_s_mean
	 *            [i]: temporary array for sample mean of design i,
	 *            i=0,1,..,ND-1
	 * @param b
	 *            : current best design design determined by function best()
	 */
	public static int second_best(final double t_s_mean[], int b) {
		int second_index = (b == 0) ? 1 : 0;

		for (int i = 0; i < t_s_mean.length; i++) {
			if (t_s_mean[i] < t_s_mean[second_index] && i != b) {
				second_index = i;
			}
		}

		return second_index;
	}

	//
	//
	// getters and setters of parameters below
	//
	//

	public void setMinReplicationsPerConfiguration(int v) {
		this.minReplicationsPerConfiguration = v;
	}

	public int getMinReplicationsPerConfiguration() {
		return minReplicationsPerConfiguration;
	}

	public void setObjective(String objective) {
		this.objective = objective;
	}

	public String getObjective() {
		return objective;
	}

	public void setPcsLevel(double pcsLevel) {
		this.pcsLevel = pcsLevel;
	}

	public double getPcsLevel() {
		return pcsLevel;
	}

	public void setDetailedResults(boolean detailedResults) {
		this.detailedResults = detailedResults;
	}

	public boolean isDetailedResults() {
		return detailedResults;
	}

	public void setNumReplications(int numReplications) {
		this.numReplications = numReplications;
	}

	public int getNumReplications() {
		return numReplications;
	}

	public ProblemType getProblemType() {
		return problemType;
	}

	public void setProblemType(ProblemType problemType) {
		this.problemType = problemType;
	}

}
