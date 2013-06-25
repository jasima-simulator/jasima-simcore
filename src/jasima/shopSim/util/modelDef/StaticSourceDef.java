package jasima.shopSim.util.modelDef;

public class StaticSourceDef extends SourceDef {
	public static final String PROP_JOB_SPECS = "jobSpecs";
	
	private JobDef[] jobSpecs;

	public JobDef[] getJobSpecs() {
		return jobSpecs;
	}

	public void setJobSpecs(JobDef[] jobSpecs) {
		firePropertyChange(PROP_JOB_SPECS, this.jobSpecs, this.jobSpecs = jobSpecs);
	}
}
