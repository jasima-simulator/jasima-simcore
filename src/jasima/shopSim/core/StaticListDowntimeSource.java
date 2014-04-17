package jasima.shopSim.core;

import jasima.core.util.Pair;

public class StaticListDowntimeSource extends DowntimeSource {

	private final Pair<Double, Double>[] data;
	private int index;

	public StaticListDowntimeSource(IndividualMachine machine,
			Pair<Double, Double>[] data) {
		super(machine);
		this.data = data;
	}

	@Override
	public void init() {
		index = -1;
		super.init();
	}

	@Override
	protected void onActivate() {
		index++;
		super.onActivate();
	}

	@Override
	protected double calcDeactivateTime(JobShop shop) {
		return Math.max(shop.simTime(), data[index].a);
	}

	@Override
	protected double calcActivateTime(JobShop shop) {
		double duration = data[index].b - data[index].a;
		assert duration >= 0.0;
		if (duration < 1e-6)
			duration = 1.0 / 600.0; // min duration 1/10th of a second
		return shop.simTime() + duration;
	}

	@Override
	protected boolean isSourceActive() {
		return index < data.length;
	}

}
