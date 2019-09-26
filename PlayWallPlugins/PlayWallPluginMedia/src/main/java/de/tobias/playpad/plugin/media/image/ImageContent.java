package de.tobias.playpad.plugin.media.image;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.pad.mediapath.MediaPath;
import de.tobias.playpad.plugin.media.main.impl.MediaPluginImpl;
import javafx.application.Platform;

import java.nio.file.Files;
import java.nio.file.Path;

public class ImageContent extends PadContent {

	private final String type;
	private boolean loaded;

	ImageContent(String type, Pad pad) {
		super(pad);
		this.type = type;
	}

	@Override
	public void updateVolume() {
	}

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

			Platform.runLater(() ->
			{
				if (getPad().isPadVisible()) {
					getPad().getController().getView().showBusyView(false);
				}
			});
		} else {
			Platform.runLater(() -> getPad().setStatus(PadStatus.NOT_FOUND));
		}
	}

	@Override
	public void loadMedia(MediaPath mediaPath) {
		loadMedia();
	}

	@Override
	public void unloadMedia() {
		// First Stop the pad (if playing)
		if (getPad().getStatus() == PadStatus.PLAY || getPad().getStatus() == PadStatus.PAUSE) {
			getPad().setStatus(PadStatus.STOP);
		}

		loaded = false;
		Platform.runLater(() -> getPad().setStatus(PadStatus.EMPTY));
	}

	@Override
	public void unloadMedia(MediaPath mediaPath) {
		unloadMedia();
	}

	@Override
	public PadContent copy(Pad pad) {
		ImageContent clone = new ImageContent(getType(), pad);
		clone.loadMedia();
		return clone;
	}

}