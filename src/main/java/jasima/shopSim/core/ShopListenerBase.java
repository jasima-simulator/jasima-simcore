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

import jasima.shopSim.util.BasicJobStatCollector;
import jasima.shopSim.util.ExtendedJobStatCollector;

/**
 * This class can be used as a base class for classes collecting results based
 * on job releases/job completions.
 * 
 * @author Torsten Hildebrandt
 * 
 * @see BasicJobStatCollector
 * @see ExtendedJobStatCollector
 */
public abstract class ShopListenerBase implements ShopListener, Cloneable {

	private double initialPeriod = 0;
	private int ignoreFirst = 0;

	public ShopListenerBase() {
		super();
	}

	protected boolean shouldCollect(Job j) {
		return (j.getShop().simTime() >= getInitialPeriod() && j.getJobNum() >= getIgnoreFirst());
	}

	@Override
	public ShopListenerBase clone() {
		try {
			return (ShopListenerBase) super.clone();
		} catch (CloneNotSupportedException cantHappen) {
			throw new AssertionError(cantHappen);
		}
	}

	// boring getters and setters below

	public double getInitialPeriod() {
		return initialPeriod;
	}

	public void setInitialPeriod(double initialPeriod) {
		this.initialPeriod = initialPeriod;
	}

	public int getIgnoreFirst() {
		return ignoreFirst;
	}

	public void setIgnoreFirst(int ignoreFirst) {
		this.ignoreFirst = ignoreFirst;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}
