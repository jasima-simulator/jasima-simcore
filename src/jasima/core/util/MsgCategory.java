package jasima.core.util;

import jasima.core.experiment.ExperimentMessage.ExpPrintMessage;
import jasima.core.simulation.Simulation.SimPrintMessage;

/**
 * Enum for message categories for {@link SimPrintMessage}s and
 * {@link ExpPrintMessage}s.
 * 
 * @author Torsten Hildebrandt
 */
public enum MsgCategory {
	OFF, ERROR, WARN, INFO, DEBUG, TRACE, ALL
}