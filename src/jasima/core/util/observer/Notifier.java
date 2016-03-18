package jasima.core.util.observer;

public interface Notifier<SUBJECT extends Notifier<SUBJECT, MESSAGE>, MESSAGE> {

	int numListener();

	void addListener(NotifierListener<SUBJECT, MESSAGE> l);

	boolean removeListener(NotifierListener<SUBJECT, MESSAGE> l);

	NotifierListener<SUBJECT, MESSAGE> getListener(int idx);

	void fire(MESSAGE msg);

}
