package jasima.core.experiment;

public class MyRuntimeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	MyRuntimeException() {
		super("Some strange error.");
	}
}