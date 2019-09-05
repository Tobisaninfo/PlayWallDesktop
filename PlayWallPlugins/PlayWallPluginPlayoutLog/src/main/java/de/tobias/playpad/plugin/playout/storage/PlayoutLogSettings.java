package de.tobias.playpad.plugin.playout.storage;

import de.thecodelabs.storage.proxy.DefaultBoolean;
import de.thecodelabs.storage.proxy.Setter;
import de.thecodelabs.storage.proxy.Settings;
import de.thecodelabs.storage.settings.annotation.FilePath;

@FilePath("PlayOutLog.json")
public interface PlayoutLogSettings extends Settings {

	@DefaultBoolean(false)
	boolean autoStartLogging();

	@Setter
	void autoStartLogging(boolean value);
}
