package de.tobias.playpad.audio;

import java.nio.file.Path;

import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.pad.content.AudioContent;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.media.AudioEqualizer;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class JavaFXAudioHandler extends AudioHandler implements Equalizable {

	public static final String TYPE = "JavaFx";
	public static final String NAME = "Java FX Media";

	private Media media;
	private MediaPlayer player;

	private ObjectProperty<Duration> durationProperty;

	private BooleanProperty loadedProperty;

	public JavaFXAudioHandler(PadContent content) {
		super(content);

		media = null;
		player = null;
		durationProperty = new SimpleObjectProperty<>();

		loadedProperty = new SimpleBooleanProperty();
	}

	@Override
	public void play() {
		player.play();
	}

	@Override
	public void pause() {
		player.pause();
	}

	@Override
	public void stop() {
		player.stop();
	}

	@Override
	public AudioEqualizer getAudioEqualizer() {
		return player.getAudioEqualizer();
	}

	@Override
	public Duration getPosition() {
		return player.getCurrentTime();
	}

	@Override
	public ReadOnlyObjectProperty<Duration> positionProperty() {
		return player.currentTimeProperty();
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
	public void setVolume(double volume, double masterVolume, double customVolume) {
		if (player != null)
			player.setVolume(volume * masterVolume * customVolume);
	}

	@Override
	public boolean isMediaLoaded() {
		return player != null;
	}

	@Override
	public void loadMedia(Path[] paths) {
		Platform.runLater(() -> {
			if (getContent().getPad().isPadVisible()) {
				getContent().getPad().getController().getView().showBusyView(true);
			}
		});

		// Old Player
		if (player != null) {
			getContent().stop();
		}

		Path path = ((AudioContent) getContent()).getPath();
		media = new Media(path.toFile().toURI().toString());
		player = new MediaPlayer(media);

		// Player Listener
		player.setOnReady(() -> {
			durationProperty.set(player.getTotalDuration());
			getContent().getPad().setStatus(PadStatus.READY);
			loadedProperty.set(true);

			Platform.runLater(() -> {
				if (getContent().getPad().isPadVisible()) {
					getContent().getPad().getController().getView().showBusyView(false);
				}
			});
		});

		player.setOnError(() -> {
			Platform.runLater(() -> {
				if (getContent().getPad().isPadVisible()) {
					getContent().getPad().getController().getView().showBusyView(false);
				}
			});
			loadedProperty.set(false);
			getContent().getPad().throwException(path, player.getError());
		});
		player.setOnEndOfMedia(() -> {
			if (!getContent().getPad().getPadSettings().isLoop()) {
				getContent().getPad().setEof(true);
				getContent().getPad().setStatus(PadStatus.STOP);
			} else {
				// Loop
				player.seek(Duration.ZERO);
			}
		});
	}

	@Override
	public void unloadMedia() {
		if (player != null)
			player.dispose();
		player = null;
		media = null;
		durationProperty.set(null);
		loadedProperty.set(false);
	}
}
