package jasima.core.util.observer;

/**
 * NotifierListener are notified by a {@link Notifier} about events. This
 * implements the Observer pattern using Java Generics.
 * 
 * @author Torsten Hildebrandt
 */
public interface NotifierListener<SUBJECT, MESSAGE> {
	void inform(SUBJECT publisher, MESSAGE event);
}
