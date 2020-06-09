package jasima.core.experiment;

public class ExpTestControlFlow extends Experiment {
	private static final long serialVersionUID = 1L;

	private boolean fail = true;
	private long runtimeMillis = -1;
	private int dummy = 23;

	@Override
	protected void performRun() {
		if (runtimeMillis>0) {
			try {
				Thread.sleep(runtimeMillis);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		
		checkCancelledOrInterrupted();
		
		if (isFail())
			throw new MyRuntimeException();
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
}