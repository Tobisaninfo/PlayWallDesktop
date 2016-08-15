package de.tobias.playpad.audio.fade;

/**
 * Schnittstelle, die für das Faden die Lautstärke in der Audio Implementierung setzt.
 * 
 * @author tobias
 *
 * @since 6.0.0
 */
public interface Fadeable {

	public void setVolume(double vol);
}
