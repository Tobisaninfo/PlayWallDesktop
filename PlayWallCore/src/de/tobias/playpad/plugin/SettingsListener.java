package de.tobias.playpad.plugin;

import de.tobias.playpad.settings.Profile;

public interface SettingsListener {

	public default void onLoad(Profile profile) {}

	public default void onSave(Profile profile) {}

	public default void onChange(Profile profile) {}
}
