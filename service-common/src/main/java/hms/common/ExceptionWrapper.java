package hms.common;

public class ExceptionWrapper extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ExceptionWrapper(Throwable throwable) {
        super(throwable);
    }

    public static ExceptionWrapper wrap(Throwable throwable) {
        return new ExceptionWrapper(throwable);
    }

    public Throwable unwrap() {
        return getCause();
    }
}
