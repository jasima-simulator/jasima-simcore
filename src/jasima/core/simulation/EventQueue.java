package jasima.core.simulation;

/** Public interface of event queue implementations. */
public interface EventQueue {

	/** Insert an event in the queue. */
	public void insert(SimEvent e);

	/** Extract the (chronologically) next event from the queue. */
	public SimEvent extract();

	/**
	 * Removes the given element from the queue.
	 * 
	 * @param element the element to remove (mustn't be null)
	 * @return {@code true} if the element was contained in the heap and
	 *         successfully removed, {@code false} otherwise
	 */
	public boolean remove(SimEvent element);
}