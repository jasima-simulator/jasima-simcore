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
package jasima.core.simulation.util;

import jasima.core.simulation.SimEvent;

/**
 * This class is used internally by {@link #scheduleAt(double,int,Runnable)} to
 * run a certain method at a particular simulation time.
 */
public final class SimEventMethodCall extends SimEvent {
	public final Runnable m;
	public final boolean isAppEvent;

	public SimEventMethodCall(double time, int prio, String description, Runnable method) {
		this(time, prio, description, method, true);
	}

	public SimEventMethodCall(double time, int prio, String description, Runnable method, boolean isAppEvent) {
		super(time, prio, description);
		this.m = method;
		this.isAppEvent = isAppEvent;
	}

	@Override
	public void handle() {
		m.run();
	}

	@Override
	public boolean isAppEvent() {
		return isAppEvent;
	}

	@Override
	public String toString() {
		return getDescription() != null ? getDescription() : String.format("MethodCallEvent(%s)", m.toString());
	}
}