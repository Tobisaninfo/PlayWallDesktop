package de.tobias.playpad.settings;

/**
 * Listener for ProfileChanging
 * 
 * @author tobias
 *
 */
public interface ProfileListener {

	void reloadSettings(Profile oldProfile, Profile currentProfile);
}