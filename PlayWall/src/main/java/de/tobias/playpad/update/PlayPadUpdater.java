package de.tobias.playpad.update;

import de.thecodelabs.storage.settings.StorageTypes;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.remote.RemoteResource;
import de.thecodelabs.utils.application.remote.RemoteResourceType;
import de.thecodelabs.utils.util.SystemUtils;
import de.tobias.updater.client.Updatable;
import de.tobias.updater.client.UpdateChannel;
import de.tobias.updater.client.UpdateItem;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

public class PlayPadUpdater implements Updatable {

	private UpdateItem updateItem;
	private URL remoteUrl;

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
		return updateItem.getBuild();
	}

	@Override
	public String getNewVersion() {
		return updateItem.getVersion();
	}

	@Override
	public void loadInformation(UpdateChannel channel) throws MalformedURLException {
		App app = ApplicationUtils.getMainApplication();
		RemoteResource update = app.getRemoteResource(RemoteResourceType.UPDATE, channel.toString(), "version.yml");
		updateItem = update.deserialize(StorageTypes.YAML, UpdateItem.class);

		String remotePath;
		if (SystemUtils.isExe()) {
			remotePath = updateItem.getPathExe();
		} else if (SystemUtils.isJar()) {
			remotePath = updateItem.getPathJar();
		} else {
			remotePath = updateItem.getPathApp();
		}
		remoteUrl = new URL(remotePath);
	}

	@Override
	public boolean isUpdateAvailable() {
		return getCurrentBuild() < getNewBuild();
	}

	@Override
	public URL getDownloadPath() {
		return remoteUrl;
	}

	@Override
	public Path getLocalPath() {
		return SystemUtils.getRunPath();
	}

	@Override
	public String name() {
		return ApplicationUtils.getApplication().getInfo().getName();
	}

}
