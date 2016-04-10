package de.tobias.playpad.settings;

/**
 * Listener for ProfileChanging
 * 
 * @author tobias
 *
 */
public interface ProfileListener {

	public void reloadSettings(Profile oldProfile, Profile currentProfile);
}