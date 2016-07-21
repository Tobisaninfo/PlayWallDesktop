package de.tobias.playpad.audio;

import java.nio.file.Path;

import de.tobias.playpad.pad.conntent.PadContent;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.util.Duration;

// Mögliche Interfaces: Equalizable

public abstract class AudioHandler {

	private PadContent content;

	public AudioHandler(PadContent content) {
		super();
		this.content = content;
	}

	public PadContent getContent() {
		return content;
	}

	public abstract void play();

	public abstract void pause();

	public abstract void stop();

	public abstract Duration getPosition();

	public abstract ReadOnlyObjectProperty<Duration> positionProperty();

	public abstract Duration getDuration();

	public abstract ReadOnlyObjectProperty<Duration> durationProperty();

	public abstract void setVolume(double volume, double masterVolume, double customVolume);

	public abstract boolean isMediaLoaded();

	// TODO Auch einzelne Dateien laden
	public abstract void loadMedia(Path[] paths);

	public abstract void unloadMedia();

}
