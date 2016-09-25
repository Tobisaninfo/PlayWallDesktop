package de.tobias.playpad.settings.keys;

/**
 * Exception wenn Key nicht hinzugefügt werden kann, aufgrund eines Konflikts.
 * 
 * @author tobias
 *
 * @since 6.0.0
 */
public class KeyConflictException extends Exception {

	private static final long serialVersionUID = 1L;

	public KeyConflictException(Key key) {
		super("Key: " + key.toString() + " cannot be used, caused by an conflict");
	}
}
