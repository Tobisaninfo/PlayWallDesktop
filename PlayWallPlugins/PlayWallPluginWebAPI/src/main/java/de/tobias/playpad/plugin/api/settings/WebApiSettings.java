package de.tobias.playpad.plugin.api.settings;

import de.thecodelabs.storage.settings.annotation.FilePath;
import de.thecodelabs.storage.settings.annotation.Key;

import java.util.ArrayList;
import java.util.List;

@FilePath("webapi.json")
public class WebApiSettings {
	@Key
	private boolean enabled = false;
	@Key
	private int port = 9876;
	@Key
	private List<WebApiRemoteSettings> remoteSettings = new ArrayList<>();

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

	public List<WebApiRemoteSettings> getRemoteSettings() {
		return remoteSettings;
	}

	public void setRemoteSettings(List<WebApiRemoteSettings> remoteSettings) {
		this.remoteSettings = remoteSettings;
	}
}
