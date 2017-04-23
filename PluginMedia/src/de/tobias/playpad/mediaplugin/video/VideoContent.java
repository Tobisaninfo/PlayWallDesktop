package de.tobias.playpad.mediaplugin.video;

import de.tobias.playpad.mediaplugin.main.impl.MediaPluginImpl;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadSettings;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.pad.content.play.Durationable;
import de.tobias.playpad.pad.content.play.Pauseable;
import de.tobias.playpad.pad.mediapath.MediaPath;
import de.tobias.playpad.volume.VolumeManager;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.nio.file.Files;
import java.nio.file.Path;

public class VideoContent extends PadContent implements Pauseable, Durationable {

	static final String VIDEO_LAST_FRAME = "Video.LastFrame";

	private final String type;
	private Media media;
	private MediaPlayer player;

	private transient ObjectProperty<Duration> durationProperty = new SimpleObjectProperty<>();
	private transient ObjectProperty<Duration> positionProperty = new SimpleObjectProperty<>();

	private transient ChangeListener<Number> padVolumeListener;

	private transient boolean holdLastFrame = false;

	VideoContent(String type, Pad pad) {
		super(pad);
		this.type = type;
		padVolumeListener = (a, b, c) ->
		{
			updateVolume();
		};
	}

	@Override
	public void updateVolume() {
		if (player != null) {
			double volume = VolumeManager.getInstance().computeVolume(getPad());
			player.setVolume(volume);
		}
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void play() {
		getPad().setEof(false);
		MediaPluginImpl.getInstance().getVideoViewController().setMediaPlayer(player, getPad());
		if (holdLastFrame) {
			holdLastFrame = false;
			player.seek(Duration.ZERO);
		}

		player.play();
		holdLastFrame = false;
	}

	@Override
	public void pause() {
		player.pause();
	}

	@Override
	public boolean stop() {
		PadSettings padSettings = getPad().getPadSettings();

		if (padSettings.getCustomSettings().containsKey(VIDEO_LAST_FRAME) && !holdLastFrame && getPad().isEof()) {
			if ((boolean) padSettings.getCustomSettings().get(VIDEO_LAST_FRAME)) {
				getPad().setStatus(PadStatus.PAUSE);
				holdLastFrame = true;
				return false;
			}
		}
		player.stop();
		MediaPluginImpl.getInstance().getVideoViewController().setMediaPlayer(null, null);
		return true;
	}

	@Override
	public boolean isPadLoaded() {
		return player != null;
	}

	@Override
	public Duration getDuration() {
		return durationProperty.get();
	}

	@Override
	public ReadOnlyObjectProperty<Duration> durationProperty() {
		return durationProperty;
	}

	@Override
	public Duration getPosition() {
		return positionProperty.get();
	}

	@Override
	public ReadOnlyObjectProperty<Duration> positionProperty() {
		return positionProperty;
	}

	public MediaPlayer getPlayer() {
		return player;
	}

	@Override
	public void loadMedia() {
		Path path = getPad().getPath();
		if (path != null && Files.exists(path)) {
			media = new Media(path.toUri().toString());

			// Old Player
			if (player != null) {
				stop();
			}

			player = new MediaPlayer(media);

			// Player Listener
			player.setOnReady(() ->
			{
				getPad().setStatus(PadStatus.READY);

				Platform.runLater(() ->
				{
					if (getPad().isPadVisible()) {
						getPad().getController().getView().showBusyView(false);
					}
				});
			});

			player.setOnError(() ->
			{
				Platform.runLater(() ->
				{
					if (getPad().isPadVisible()) {
						getPad().getController().getView().showBusyView(false);
					}
				});
			});
			player.setOnEndOfMedia(() ->
			{
				if (!getPad().getPadSettings().isLoop()) {
					getPad().setEof(true);
					getPad().setStatus(PadStatus.STOP);
				} else {
					// Loop
					player.seek(Duration.ZERO);
				}
			});

			durationProperty.bind(player.totalDurationProperty());
			positionProperty.bind(player.currentTimeProperty());

			getPad().getPadSettings().volumeProperty().addListener(padVolumeListener);
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

		durationProperty.unbind();
		positionProperty.unbind();

		getPad().getPadSettings().volumeProperty().removeListener(padVolumeListener);

		player = null;
		media = null;
		durationProperty.set(null);

		Platform.runLater(() ->
		{
			if (getPad() != null) {
				getPad().setStatus(PadStatus.EMPTY);
			}
		});
	}

	@Override
	public void unloadMedia(MediaPath mediaPath) {
		unloadMedia();
	}

	@Override
	public PadContent clone() throws CloneNotSupportedException {
		VideoContent clone = (VideoContent) super.clone();
		clone.loadMedia();
		return clone;
	}
}