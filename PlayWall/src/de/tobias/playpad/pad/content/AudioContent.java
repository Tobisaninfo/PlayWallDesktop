package de.tobias.playpad.pad.content;

import java.nio.file.Files;
import java.nio.file.Path;

import de.tobias.playpad.audio.AudioEqualizeable;
import de.tobias.playpad.pad.content.play.Seekable;
import de.tobias.playpad.pad.mediapath.MediaPath;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.audio.AudioHandler;
import de.tobias.playpad.audio.AudioRegistry;
import de.tobias.playpad.pad.content.play.Equalizeable;
import de.tobias.playpad.pad.fade.Fade;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.content.play.Durationable;
import de.tobias.playpad.pad.fade.Fadeable;
import de.tobias.playpad.pad.fade.FadeDelegate;
import de.tobias.playpad.pad.content.play.Pauseable;
import de.tobias.playpad.volume.VolumeManager;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.media.AudioEqualizer;
import javafx.util.Duration;

public class AudioContent extends PadContent implements Pauseable, Durationable, Fadeable, Equalizeable, FadeDelegate, Seekable {

	private final String type;

	private AudioHandler audioHandler;

	private ObjectProperty<Duration> durationProperty = new SimpleObjectProperty<>();
	private ObjectProperty<Duration> positionProperty = new SimpleObjectProperty<>();

	private ChangeListener<Number> volumeListener;

	private Fade fade;

	AudioContent(String type, Pad pad) {
		super(pad);
		this.type = type;
		fade = new Fade(this);

		// Pad Volume Listener
		volumeListener = (a, b, c) -> updateVolume();
	}

	@Override
	public void updateVolume() {
		double volume = VolumeManager.getInstance().computeVolume(getPad());
		audioHandler.setVolume(volume);
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void play() {
		getPad().setEof(false);
		audioHandler.play();
	}

	@Override
	public void pause() {
		audioHandler.pause();
	}

	@Override
	public boolean stop() {
		audioHandler.stop();
		return true;
	}

	@Override
	public void seekToStart() {
		if (audioHandler instanceof Seekable) {
			((Seekable) audioHandler).seekToStart();
		}
	}

	@Override
	public void fadeIn() {
		Pad pad = getPad();

		Duration fadeIn = pad.getPadSettings().getFade().getFadeIn();
		if (fadeIn.toMillis() > 0) {
			fade.fadeIn(fadeIn);
		}
	}

	@Override
	public void fadeOut(Runnable onFinish) {
		Duration fadeOut = getPad().getPadSettings().getFade().getFadeOut();
		if (fadeOut.toMillis() > 0) {
			fade.fadeOut(fadeOut, () -> {
				if (onFinish != null) {
					onFinish.run();
				}
				updateVolume();
			});
		} else {
			onFinish.run();
		}
	}

	public boolean isFadeActive() {
		return fade.isFading();
	}

	@Override
	public void onFadeLevelChange(double level) {
		Pad pad = getPad();
		audioHandler.setVolume(level * VolumeManager.getInstance().computeVolume(pad));
	}

	@Override
	public AudioEqualizer getAudioEqualizer() {
		if (audioHandler instanceof AudioEqualizeable) {
			return ((AudioEqualizeable) audioHandler).getAudioEqualizer();
		}
		return null;
	}

	@Override
	public boolean isPadLoaded() {
		return audioHandler.isMediaLoaded();
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

	@Override
	public void loadMedia() {
		// init audio implementation
		AudioRegistry audioRegistry = PlayPadPlugin.getRegistryCollection().getAudioHandlers();
		audioHandler = audioRegistry.getCurrentAudioHandler().createAudioHandler(this);

		Path path = getPad().getPath();

		if (path != null && Files.exists(path)) {
			audioHandler.loadMedia(path);

			durationProperty.bind(audioHandler.durationProperty());
			positionProperty.bind(audioHandler.positionProperty());

			getPad().getPadSettings().volumeProperty().addListener(volumeListener);
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

		getPad().getPadSettings().volumeProperty().removeListener(volumeListener);

		if (audioHandler != null)
			audioHandler.unloadMedia();

		Platform.runLater(() -> {
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
		AudioContent clone = (AudioContent) super.clone();

		AudioRegistry audioRegistry = PlayPadPlugin.getRegistryCollection().getAudioHandlers();
		clone.audioHandler = audioRegistry.getCurrentAudioHandler().createAudioHandler(this);

		clone.durationProperty = new SimpleObjectProperty<>();
		clone.positionProperty = new SimpleObjectProperty<>();

		clone.loadMedia();
		return clone;
	}
}
