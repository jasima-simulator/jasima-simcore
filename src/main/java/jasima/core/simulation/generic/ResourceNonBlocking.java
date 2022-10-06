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
package jasima.core.simulation.generic;

import static java.util.Objects.requireNonNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import jasima.core.simulation.SimEvent;
import jasima.core.simulation.SimProcess.MightBlock;
import jasima.core.simulation.Simulation;
import jasima.core.simulation.generic.ResourceNonBlocking.Request.RequestState;

public class ResourceNonBlocking {

	private static final int DEF_CHECK_FULFILMENT_PRIO = SimEvent.EVENT_PRIO_NORMAL;

	public static class Request {

		public enum RequestState {
			NEW, WAITING, PROCESSING, FINISHED, CANCELLED;
		}

		private RequestState state;

		private final int amount;
		private final BiConsumer<ResourceNonBlocking, Request> onAvailableCallback;

		public Request(int amount, BiConsumer<ResourceNonBlocking, Request> onAvailableCallback) {
			super();

			this.state = RequestState.NEW;
			this.amount = amount;
			this.onAvailableCallback = onAvailableCallback;
		}

	}

	private Simulation sim;

	private Deque<Request> waitingRequests = new ArrayDeque<>();

	private int capacityTotal;
	private int capacityInUse;
	private Map<Request, Request> currentlyProcessed = new IdentityHashMap<>();

	private boolean scheduledCheckFulfilment = false;
	private SimEvent checkFulfilmentEvent = new SimEvent(Double.NaN, DEF_CHECK_FULFILMENT_PRIO, "CHECK_FULFILMENT") {
		@Override
		public void handle() {
			scheduledCheckFulfilment = false;
			checkFulfilment();
		}
	};

	public ResourceNonBlocking() {
		this(1);
	}

	public ResourceNonBlocking(int totalCapacity) {
		super();

		this.capacityTotal = totalCapacity;
		this.capacityInUse = 0;
	}

	public boolean canAcquire(int capacity) {
		if (capacity < 1) {
			throw new IllegalArgumentException("Capacity has to be positive: " + capacity);
		}

		return capacity <= capacityAvailable();
	}

	public int capacityAvailable() {
		return capacityTotal - capacityInUse;
	}

	public int capacityTotal() {
		return capacityTotal;
	}

	public Request get(int amount, BiConsumer<ResourceNonBlocking, Request> onAvailableCallback) {
		Request request = new Request(amount, requireNonNull(onAvailableCallback));
		waitingRequests.add(request);

		scheduleFulfilmentCheck();

		return request;
	}

	public void put(Request req) {
		if (req.state != RequestState.PROCESSING) {
			throw new IllegalArgumentException("Invalid request state.");
		}
		boolean removeRes = currentlyProcessed.remove(req, req);
		if (!removeRes) {
			throw new IllegalArgumentException("Request not processed.");
		}
		capacityInUse -= req.amount;
		req.state = RequestState.FINISHED;

		scheduleFulfilmentCheck();
	}

	public void scheduleFulfilmentCheck() {
		int checkPrio = Math.max(DEF_CHECK_FULFILMENT_PRIO, sim.currentPrio() + 1000);
		if (checkFulfilmentEvent.getPrio() != checkPrio || !scheduledCheckFulfilment) {
			if (scheduledCheckFulfilment) {
				// temporarily remove event if we have to reduce event prio to avoid priority
				// inversion
				boolean removeRes = sim.unschedule(checkFulfilmentEvent);
				assert removeRes;
				scheduledCheckFulfilment = false;
			}

			checkFulfilmentEvent.setPrio(checkPrio);
			sim.schedule(checkFulfilmentEvent);
			scheduledCheckFulfilment = true;
		}
	}

	protected void checkFulfilment() {
		int available = capacityAvailable();
		while (waitingRequests.size() > 0 && available > 0) {
			Request r = waitingRequests.peekFirst();
			if (r.amount > available) {
				// can't start due to insufficient capacity
				break; // while
			}
			waitingRequests.removeFirst();

			currentlyProcessed.put(r, r);
			r.state = RequestState.PROCESSING;

			available -= r.amount;
			capacityInUse += r.amount;

			r.onAvailableCallback.accept(this, r);
		}
	}

	public void acquire(int amount) throws MightBlock {

	}

	public void release(int amount) throws MightBlock {

	}

	public Simulation getSim() {
		return sim;
	}

	public void setSim(Simulation sim) {
		this.sim = sim;
	}

	public int getCapacityTotal() {
		return capacityTotal;
	}

	public void setCapacityTotal(int capacityTotal) {
		this.capacityTotal = capacityTotal;
	}

}
