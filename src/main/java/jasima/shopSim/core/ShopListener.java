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

import jasima.core.simulation.SimComponent;
import jasima.core.simulation.SimComponent.SimComponentEvent;
import jasima.core.simulation.SimComponentLifecycleListener;
import jasima.shopSim.core.Shop.ShopMessage;
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
public interface ShopListener extends SimComponentLifecycleListener, Cloneable {

	/**
	 * Inform method to be notified of shop events.
	 */
	@Override
	default void inform(SimComponent c, SimComponentEvent msg) {
		if (msg == ShopMessage.JOB_RELEASED) {
			Shop shop = (Shop) c;
			jobReleased(shop, shop.lastJobReleased);
		} else if (msg == ShopMessage.JOB_FINISHED) {
			Shop shop = (Shop) c;
			jobFinished(shop, shop.lastJobFinished);
		} else {
			SimComponentLifecycleListener.super.inform(c, msg);
		}
	}

	default void jobReleased(Shop shop, Job j) {
	}

	default void jobFinished(Shop shop, Job j) {
	}
	
	ShopListener clone();

}
