package de.tobias.playpad.server;

public class SessionNotExisitsException extends Exception {
	public SessionNotExisitsException() {
		super("Session invalid");
	}

	public SessionNotExisitsException(String message) {
		super(message);
	}
}
