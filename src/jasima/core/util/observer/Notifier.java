package jasima.core.util.observer;

/**
 * Notifier inform {@link NotifierListener}s about events. This implements a
 * version of the Observer-pattern using Java Generics. Implementation can be
 * delegated via {@link #notifierImpl()}.
 * <p>
 * Usually the default implementation provided by {@link NotifierImpl} can be
 * used.
 * 
 * @author Torsten Hildebrandt
 */
public interface Notifier<SUBJECT extends Notifier<SUBJECT, MESSAGE>, MESSAGE> {

	default int numListener() {
		return notifierImpl().numListener();
	}

	default void addListener(NotifierListener<SUBJECT, MESSAGE> l) {
		notifierImpl().addListener(l);
	}

	default <T extends NotifierListener<SUBJECT, MESSAGE>> void addListener(Class<T> eventType, T eventHandler) {
		notifierImpl().addListener(eventType, eventHandler);
	}

	default <T extends NotifierListener<SUBJECT, MESSAGE>> void addListenerOnce(Class<T> eventType, T eventHandler) {
		notifierImpl().addListenerOnce(eventType, eventHandler);
	}

	default void removeCurrentListener() {
		notifierImpl().removeCurrentListener();
	}

	default boolean removeListener(NotifierListener<SUBJECT, MESSAGE> l) {
		return notifierImpl().removeListener(l);
	}

	default NotifierListener<SUBJECT, MESSAGE> getListener(int idx) {
		return notifierImpl().getListener(idx);
	}

	default void fire(MESSAGE msg) {
		notifierImpl().fire(msg);
	}

	Notifier<SUBJECT, MESSAGE> notifierImpl();

}
