package de.tobias.playpad.equalizerplugin.main.impl;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import de.tobias.playpad.update.Updatable;
import de.tobias.playpad.update.UpdateChannel;
import de.tobias.utils.application.App;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;

public class EqualizerPluginUpdater implements Updatable {

	private int newBuild;
	private String newVersion;
	private URL remotePath;

	private String localFileName;
	private String name;

	@Override
	public int getCurrentBuild() {
		return 4;
	}

	@Override
	public String getCurrentVersion() {
		return "4.0";
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

		newBuild = config.getInt("plugins.equalizer.build");
		newVersion = config.getString("plugins.equalizer.version");
		remotePath = new URL(config.getString("plugins.equalizer.url"));
		localFileName = config.getString("plugins.equalizer.filename");
		name = config.getString("plugins.equalizer.name");
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
