/*******************************************************************************
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This file is part of jasima, v1.2.
 *
 * jasima is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jasima is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jasima.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
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
