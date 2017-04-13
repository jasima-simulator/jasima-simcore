package jasima.shopSim.prioRules.meta;

import java.io.Serializable;
import java.util.Objects;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.prioRules.basic.SPT;

/**
 * Utility class that can be used to write simple priority rules as a lambda
 * expression (PR itself is not a functional interface). So in order to express
 * the {@link SPT} rule you can now write
 * 
 * <pre>
 * PR spt = new SimplePR("spt", job -> -job.currProcTime());
 * </pre>
 * 
 * instead of writing an anonymous inner class:
 * 
 * <pre>
 * PR spt = new PR() {
 * 	&#64;Override
 * 	public double calcPrio(PrioRuleTarget job) {
 * 		return -job.currProcTime();
 * 	}
 * };
 * </pre>
 * 
 * @author Torsten Hildebrandt <torsten.hildebrandt@simplan.de>
 */
public class SimplePR extends PR {

	private static final long serialVersionUID = -1148070289969223577L;

	@FunctionalInterface
	public static interface JobEvaluator extends Serializable {
		double getValue(PrioRuleTarget jobOrBatch);
	}

	private final JobEvaluator prioFunction;
	private final String name;

	public SimplePR(JobEvaluator prioFunction) {
		this(null, prioFunction);
	}

	public SimplePR(String name, JobEvaluator prioFunction) {
		super();
		this.prioFunction = Objects.requireNonNull(prioFunction);
		if (name != null)
			this.name = name;
		else
			this.name = prioFunction.getClass().toString();
	}

	@Override
	public double calcPrio(PrioRuleTarget entry) {
		return prioFunction.getValue(entry);
	}

	@Override
	public String getName() {
		return "SimplePR(" + name + ")";
	}

}
