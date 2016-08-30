package de.tobias.playpad.registry;

public class NoSuchComponentException extends Exception {

	private static final long serialVersionUID = 1L;

	public NoSuchComponentException(String id) {
		super("No component for id: " + id);
	}
}
