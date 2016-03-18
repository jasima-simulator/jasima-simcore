package jasima.core.util.observer;

/**
 * Notifier inform {@link NotifierListener} about events. This implements a
 * version of the Observer-pattern using Java Generics.
 * 
 * @author Torsten Hildebrandt
 */
public interface Notifier<SUBJECT extends Notifier<SUBJECT, MESSAGE>, MESSAGE> {

	int numListener();

	void addListener(NotifierListener<SUBJECT, MESSAGE> l);

	boolean removeListener(NotifierListener<SUBJECT, MESSAGE> l);

	NotifierListener<SUBJECT, MESSAGE> getListener(int idx);

	void fire(MESSAGE msg);

}
