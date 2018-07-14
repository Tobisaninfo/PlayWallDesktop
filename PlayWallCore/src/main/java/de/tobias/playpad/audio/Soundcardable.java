package de.tobias.playpad.audio;

/**
 * Setzt die Soundcard für die Audioimplementierung.
 *
 * @author tobias
 * @since 6.0.0
 */
public interface Soundcardable extends AudioFeature {

	void setOutputDevice(String name);
}
