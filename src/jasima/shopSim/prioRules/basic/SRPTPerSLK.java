package jasima.shopSim.prioRules.basic;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;

/**
 * This rule implements the "remaining processing time per slack" rule.
 * This is rule 17 (S/WKR, the smallest ratio of slack per work remaining) in
 * Haupt (1989): "A Survey of Priority Rule-Based Scheduling".
 * 
 * @author Torsten Hildebrandt, 2013-08-10
 * @version "$Id$"
 */
public class SRPTPerSLK extends PR {

	public SRPTPerSLK() {
		super();
	}

	@Override
	public double calcPrio(PrioRuleTarget job) {
		return job.remainingProcTime() / SLK.slack(job);
	}

}
