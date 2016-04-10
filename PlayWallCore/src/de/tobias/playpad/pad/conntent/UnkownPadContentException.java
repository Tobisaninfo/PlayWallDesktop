package de.tobias.playpad.pad.conntent;

public class UnkownPadContentException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public UnkownPadContentException(String type) {
		super(type);
	}
}
