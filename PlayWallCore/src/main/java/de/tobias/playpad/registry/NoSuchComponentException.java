package de.tobias.playpad.registry;

public class NoSuchComponentException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NoSuchComponentException(String id) {
		super("No component for id: " + id);
	}

	public NoSuchComponentException(Throwable cause) {
		super(cause);
	}
}
