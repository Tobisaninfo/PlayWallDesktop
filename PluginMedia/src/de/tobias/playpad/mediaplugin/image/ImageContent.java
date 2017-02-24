package de.tobias.playpad.mediaplugin.image;

import java.nio.file.Files;
import java.nio.file.Path;

import de.tobias.playpad.pad.mediapath.MediaPath;

import de.tobias.playpad.mediaplugin.main.impl.MediaPluginImpl;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.content.PadContent;
import javafx.application.Platform;

public class ImageContent extends PadContent {

	private final String type;
	private boolean loaded;

	public ImageContent(String type, Pad pad) {
		super(pad);
		this.type = type;
	}

	@Override
	public void updateVolume() {}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void play() {
		Path mediaPath = getPad().getPath();
		MediaPluginImpl.getInstance().getVideoViewController().setImage(mediaPath.toUri().toString(), getPad());
	}

	@Override
	public boolean stop() {
		MediaPluginImpl.getInstance().getVideoViewController().setImage(null, null);
		return true;
	}

	@Override
	public boolean isPadLoaded() {
		return loaded;
	}

	@Override
	public void loadMedia() {
		Path path = getPad().getPath();
		if (path != null && Files.exists(path)) {
			getPad().setStatus(PadStatus.READY);
			loaded = true;
		} else {
			// getPad().throwException(path, new FileNotFoundException()); TODO Error Handling User
		}
	}

	@Override
	public void loadMedia(MediaPath mediaPath) {
		loadMedia();
	}

	@Override
	public void unloadMedia() {
		// First Stop the pad (if playing)
		getPad().setStatus(PadStatus.STOP);
		loaded = false;
		Platform.runLater(() -> getPad().setStatus(PadStatus.EMPTY));
	}

	@Override
	public void unloadMedia(MediaPath mediaPath) {
		unloadMedia();
	}

	@Override
	public PadContent clone() throws CloneNotSupportedException {
		ImageContent clone = (ImageContent) super.clone();
		clone.loadMedia();
		return clone;
	}

}