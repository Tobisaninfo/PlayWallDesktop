package de.tobias.playpad.namac;

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

public class NativeAudioMacUpdater implements Updatable {

	private int newBuild;
	private String newVersion;
	private URL remotePath;

	private String localFileName;
	private String name;

	@Override
	public int getCurrentBuild() {
		return 1;
	}

	@Override
	public String getCurrentVersion() {
		return "1.0";
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

		newBuild = config.getInt("plugins.namac.build");
		newVersion = config.getString("plugins.namac.version");
		remotePath = new URL(config.getString("plugins.namac.url"));
		localFileName = config.getString("plugins.namac.filename");
		name = config.getString("plugins.namac.name");

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