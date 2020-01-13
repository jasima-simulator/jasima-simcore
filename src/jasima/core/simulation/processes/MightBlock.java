package jasima.core.simulation.processes;

/**
 * Marker for all methods that might block, i.e., where execution might not
 * finish at the same simulation time when it was started.
 * 
 * @author Torsten.Hildebrandt
 *
 */
public class MightBlock extends Exception {

	private static final long serialVersionUID = 3091300075872193106L;

	private MightBlock() { // prevent instantiation, this class is just a maker
	}

}
