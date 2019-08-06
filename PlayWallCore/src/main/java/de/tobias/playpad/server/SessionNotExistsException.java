package de.tobias.playpad.server;

public class SessionNotExistsException extends Exception {
	public SessionNotExistsException() {
		super("Session invalid");
	}

	public SessionNotExistsException(String message) {
		super(message);
	}
}
