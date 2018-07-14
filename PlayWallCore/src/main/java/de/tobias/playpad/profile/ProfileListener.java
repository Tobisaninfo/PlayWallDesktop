package de.tobias.playpad.profile;

/**
 * Listener for ProfileChanging
 *
 * @author tobias
 */
public interface ProfileListener {

	void reloadSettings(Profile oldProfile, Profile currentProfile);
}