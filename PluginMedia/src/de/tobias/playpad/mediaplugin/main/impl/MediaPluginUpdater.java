package de.tobias.playpad.mediaplugin.main.impl;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import de.tobias.updater.client.Updatable;
import de.tobias.updater.client.UpdateChannel;
import de.tobias.utils.application.App;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;

public class MediaPluginUpdater implements Updatable {

	private int newBuild;
	private String newVersion;
	private URL remotePath;

	private String localFileName;
	private String name;

	@Override
	public int getCurrentBuild() {
		return 7;
	}

	@Override
	public String getCurrentVersion() {
		return "4.2.1";
	}

	@Override
	public int getNewBuild() {
		return newBuild;
	}

	@Override
	public String getNewVersion() {
		return newVersion;
	}

	@Override
	public void loadInformation(UpdateChannel channel) throws IOException {
		App app = ApplicationUtils.getMainApplication();
		URL url = new URL(app.getInfo().getUpdateURL() + "/" + channel + "/plugins.yml");
		FileConfiguration config = YamlConfiguration.loadConfiguration(url.openStream());

		newBuild = config.getInt("plugins.media.build");
		newVersion = config.getString("plugins.media.version");
		remotePath = new URL(config.getString("plugins.media.url"));
		localFileName = config.getString("plugins.media.filename");
		name = config.getString("plugins.media.name");

	}

	@Override
	public boolean isUpdateAvailable() {
		return getCurrentBuild() < getNewBuild();
	}

	@Override
	public URL getDownloadPath() {
		return remotePath;
	}

	@Override
	public Path getLocalPath() {
		return ApplicationUtils.getApplication().getPath(PathType.LIBRARY, localFileName);
	}

	@Override
	public String name() {
		return name;
	}

}
