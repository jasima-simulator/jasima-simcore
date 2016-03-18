package jasima.core.util.observer;

public interface Subscriber<SUBJECT, MESSAGE> {
	void inform(SUBJECT publisher, MESSAGE event);
}
