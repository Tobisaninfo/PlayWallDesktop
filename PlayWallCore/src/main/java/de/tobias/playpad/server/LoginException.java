package de.tobias.playpad.server;

/**
 * Created by tobias on 21.02.17.
 */
public class LoginException extends Exception {
	public LoginException() {
		super("Username or password invalid");
	}

	public LoginException(String message) {
		super(message);
	}
}
