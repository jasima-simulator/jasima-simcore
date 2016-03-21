package jasima.core.util;

import jasima.core.experiment.Experiment.ExpPrintEvent;
import jasima.core.simulation.Simulation.SimPrintMessage;

/**
 * Enum for message categories for {@link SimPrintMessage}s and
 * {@link ExpPrintEvent}s.
 * 
 * @author Torsten Hildebrandt
 */
public enum MsgCategory {
	OFF, ERROR, WARN, INFO, DEBUG, TRACE, ALL
}