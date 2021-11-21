package de.tobias.playpad.plugin.api.settings;

import de.thecodelabs.storage.settings.annotation.Key;
import de.tobias.playpad.Displayable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.UUID;

public class WebApiRemoteSettings implements Displayable {
	@Key
	private UUID id = UUID.randomUUID();
	@Key
	private String name;
	@Key
	private String serverAddress;
	@Key
	private int port;

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		displayProperty.set(name);
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	private final StringProperty displayProperty = new SimpleStringProperty();

	@Override
	public StringProperty displayProperty() {
		displayProperty.set(name);
		return displayProperty;
	}
}
