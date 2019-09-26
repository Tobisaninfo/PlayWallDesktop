package de.tobias.playpad.plugin;

import de.thecodelabs.utils.application.container.ContainerPathType;
import de.thecodelabs.utils.application.container.PathType;

public enum NativeAudioPathType implements ContainerPathType {

	AUDIO(PathType.LIBRARY.getFolder() + "/Audio");

	private String folder;

	NativeAudioPathType(String folder) {
		this.folder = folder;
	}

	@Override
	public String getFolder() {
		return folder;
	}

	@Override
	public boolean shouldBackup() {
		return false;
	}
}
