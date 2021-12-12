package jasima.core.experiment;

import static java.util.Objects.requireNonNull;

import javax.annotation.Nullable;

import jasima.core.util.Util;

final class AbortingExperiment extends Experiment {
	private final Exception e;
	private final String msg;
	private static final long serialVersionUID = 1L;

	AbortingExperiment(String msg, @Nullable Exception e) {
		this.e = e;
		this.msg = requireNonNull(msg);
	}

	@Override
	protected void produceResults() {
		super.produceResults();
		resultMap.put(Experiment.EXCEPTION_MESSAGE, msg);
		resultMap.put(Experiment.EXCEPTION, Util.exceptionToString(e));
	}

	@Override
	protected void performRun() {
		abort();
	}
}