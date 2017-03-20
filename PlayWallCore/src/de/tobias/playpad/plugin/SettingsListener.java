package de.tobias.playpad.plugin;

import de.tobias.playpad.profile.Profile;

public interface SettingsListener {

	default void onLoad(Profile profile) {}

	default void onSave(Profile profile) {}

	default void onChange(Profile profile) {}
}
