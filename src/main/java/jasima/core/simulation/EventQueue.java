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