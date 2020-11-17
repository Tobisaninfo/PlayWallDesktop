package de.tobias.playpad.plugin.api.websocket.settings;

import de.thecodelabs.storage.settings.annotation.FilePath;
import de.thecodelabs.storage.settings.annotation.Key;

@FilePath("webapi.json")
public class WebApiSettings {
	@Key
	private boolean enabled = false;
	@Key
	private int port = 9876;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
