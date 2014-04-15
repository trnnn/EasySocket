package easysocket.serialize;

public class SerializeException extends Exception {

	private static final long serialVersionUID = 1L;

	public SerializeException() {
		super();
	}

	public SerializeException(String message) {
		super(message);
	}

	public SerializeException(Throwable parent) {
		super(parent);
	}

	public SerializeException(String message, Throwable parent) {
		super(message, parent);
	}
}
