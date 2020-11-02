package jasima.core.experiment;

public class ExpTestControlFlow extends Experiment {
	private static final long serialVersionUID = 1L;

	private boolean fail = true;
	private boolean abort = false;
	private long runtimeMillis = -1;
	private int dummy = 23;
	private int nonNegativeProperty = 0;

	@Override
	protected void performRun() {
		if (runtimeMillis>0) {
			try {
				Thread.sleep(runtimeMillis);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException(e);
			}
		}
		
		checkCancelledOrInterrupted();
		
		if (isFail()) {
			throw new MyRuntimeException();
		}
		
		if (isAbort()) {
			aborted = 1;
		}
	}

	@Override
	protected void produceResults() {
		super.produceResults();
		resultMap.put("results", getDummy() * getDummy());
	}

	public boolean isFail() {
		return fail;
	}

	public void setFail(boolean fail) {
		this.fail = fail;
	}

	public int getDummy() {
		return dummy;
	}

	public void setDummy(int dummy) {
		this.dummy = dummy;
	}

	public long getRuntimeMillis() {
		return runtimeMillis;
	}

	public void setRuntimeMillis(long runtime) {
		this.runtimeMillis = runtime;
	}

	public boolean isAbort() {
		return abort;
	}

	public void setAbort(boolean abort) {
		this.abort = abort;
	}

	public int getNonNegativeProperty() {
		return nonNegativeProperty;
	}

	public void setNonNegativeProperty(int nonNegativeProperty) {
		if (nonNegativeProperty<0) {
			throw new IllegalArgumentException();
		}
		this.nonNegativeProperty = nonNegativeProperty;
	}
}