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
package jasima.core.simulation;

import java.util.List;

/** Public interface of event queue implementations. */
public interface EventQueue {

	/** Insert an event in the queue. */
	void insert(SimEvent e);

	/** Extract the (chronologically) next event from the queue. */
	SimEvent extract();

	/**
	 * Removes the given element from the queue.
	 * 
	 * @param element the element to remove (mustn't be null)
	 * @return {@code true} if the element was contained in the heap and
	 *         successfully removed, {@code false} otherwise
	 */
	boolean remove(SimEvent element);

	/**
	 * Returns the number of events currently contained in the event queue.
	 */
	int size();

	/**
	 * Returns all events as an ordered list. Use carefully, this is an expensive
	 * operation.
	 * 
	 * @return All events.
	 */
	List<SimEvent> allEvents();

}