package jasima.core.util.observer;

public interface Subscriber {
	void inform(Object publisher, Object event);

	void register(NotifierService s);
}
