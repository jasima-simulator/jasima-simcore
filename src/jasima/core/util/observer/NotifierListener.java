package jasima.core.util.observer;

public interface NotifierListener<SUBJECT, MESSAGE> {
	void inform(SUBJECT publisher, MESSAGE event);
}
