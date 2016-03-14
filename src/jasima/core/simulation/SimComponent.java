package jasima.core.simulation;

import java.util.Map;

public interface SimComponent {

	Simulation getSim();

	void setSim(Simulation sim);

	default double simTime() {
		return getSim().simTime();
	}

	SimComponentContainer<?> getParent();

	void setParent(SimComponentContainer<?> p);

	default void init() {
	}

	default void beforeRun() {
	}

	default void afterRun() {
	}

	default void done() {
	}

	default void produceResults(Map<String, Object> res) {
	}

}
