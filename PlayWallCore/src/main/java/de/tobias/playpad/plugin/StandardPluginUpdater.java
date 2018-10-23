package de.tobias.playpad.plugin;

import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.server.Server;
import de.tobias.updater.client.Updatable;
import de.tobias.updater.client.UpdateChannel;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

/**
 * Created by tobias on 16.04.17.
 */
public class StandardPluginUpdater implements Updatable {

	private int currentBuild;
	private String currentVersion;

	private Module module;
	private ModernPlugin modernPlugin;

	public StandardPluginUpdater(int currentBuild, String currentVersion, Module module) {
		this.currentBuild = currentBuild;
		this.currentVersion = currentVersion;
		this.module = module;
	}

	@Override
	public int getCurrentBuild() {
		return currentBuild;
	}

	@Override
	public String getCurrentVersion() {
		return currentVersion;
	}

	@Override
	public int getNewBuild() {
		return modernPlugin.getBuild();
	}

	@Override
	public String getNewVersion() {
		return modernPlugin.getVersion();
	}

	@Override
	public boolean isUpdateAvailable() {
		return getCurrentBuild() < getNewBuild();
	}

	@Override
	public void loadInformation(UpdateChannel updateChannel) throws IOException, URISyntaxException {
		modernPlugin = PlayPadPlugin.getServerHandler().getServer().getPlugin(module.identifier);
	}

	@Override
	public URL getDownloadPath() {
		try {
			Server server = PlayPadPlugin.getServerHandler().getServer();
			return new URL("https://" + server.getHost() + "/" + modernPlugin.getPath());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Path getLocalPath() {
		return ApplicationUtils.getApplication().getPath(PathType.LIBRARY, modernPlugin.getFileName());
	}

	@Override
	public String name() {
		return module.name;
	}
}
