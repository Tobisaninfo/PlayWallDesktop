package de.tobias.playpad;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import de.tobias.utils.application.App;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.util.SystemUtils;

public class PlayPadUpdater implements Updatable {

	private int newBuild;
	private String newVersion;
	private URL remotePath;

	@Override
	public int getCurrentBuild() {
		return (int) ApplicationUtils.getMainApplication().getInfo().getBuild();
	}

	@Override
	public String getCurrentVersion() {
		return ApplicationUtils.getMainApplication().getInfo().getVersion();
	}

	@Override
	public int getNewBuild() {
		if (newBuild == 0) {
			checkUpdate();
		}
		return newBuild;
	}

	@Override
	public String getNewVersion() {
		if (newVersion == null) {
			checkUpdate();
		}
		return newVersion;
	}

	@Override
	public boolean checkUpdate() {
		App app = ApplicationUtils.getMainApplication();
		try {
			URL url = new URL(app.getInfo().getUpdateURL() + "/version.yml");
			FileConfiguration config = YamlConfiguration.loadConfiguration(url.openStream());
			newBuild = config.getInt("build");
			newVersion = config.getString("version");

			if (SystemUtils.isExe()) {
				remotePath = new URL(config.getString("pathExe"));
			} else {
				remotePath = new URL(config.getString("pathJar"));				
			}
			
			return getCurrentBuild() < getNewBuild();
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public URL getDownloadPath() {
		if (remotePath == null) {
			checkUpdate();
		}
		return remotePath;
	}

	@Override
	public Path getLocalPath() {
		try {
			return SystemUtils.getRunPath();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String name() {
		return ApplicationUtils.getApplication().getInfo().getName();
	}

}
