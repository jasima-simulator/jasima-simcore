package jasima.core.util.observer;

public interface Notifier<SUBJECT extends Notifier<SUBJECT, MESSAGE>, MESSAGE> {

	int numListener();

	void addListener(Subscriber<SUBJECT, MESSAGE> l);

	boolean removeListener(Subscriber<SUBJECT, MESSAGE> l);

	Subscriber<SUBJECT, MESSAGE> getListener(int idx);

	void fire(MESSAGE msg);

}
