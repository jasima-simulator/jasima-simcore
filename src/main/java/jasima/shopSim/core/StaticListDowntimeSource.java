/*
This file is part of jasima, the Java simulator for manufacturing and logistics.
 
Copyright 2010-2022 jasima contributors (see license.txt)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package jasima.shopSim.core;

import jasima.core.util.Pair;

public class StaticListDowntimeSource extends DowntimeSource {

	private final Pair<Double, Double>[] data;
	private int index;

	public StaticListDowntimeSource(IndividualMachine machine, Pair<Double, Double>[] data) {
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
	protected double calcDeactivateTime(Shop shop) {
		return Math.max(shop.simTime(), data[index].a);
	}

	@Override
	protected double calcActivateTime(Shop shop) {
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
