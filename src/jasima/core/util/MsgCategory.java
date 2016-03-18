package jasima.core.util;

import jasima.core.experiment.Experiment.ExpPrintEvent;
import jasima.core.simulation.Simulation.SimPrintEvent;

/**
 * Enum for message categories for {@link SimPrintEvent}s and
 * {@link ExpPrintEvent}s.
 * 
 * @author Torsten Hildebrandt
 */
public enum MsgCategory {
	OFF, ERROR, WARN, INFO, DEBUG, TRACE, ALL
}