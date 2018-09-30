package de.tobias.playpad.audio.mac;

import de.tobias.playpad.NativeAudio;
import de.tobias.playpad.audio.AudioHandler;
import de.tobias.playpad.audio.Peakable;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.pad.content.play.Seekable;
import de.tobias.utils.threading.Worker;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.util.Duration;

import java.nio.file.Path;

public class NativeAudioMacHandler extends AudioHandler implements Peakable, Seekable {

	private static int counter = 0;

	private final int id;
	ObjectProperty<Duration> positionProperty;
	private ObjectProperty<Duration> durationProperty;
	private boolean isLoaded;

	private DoubleProperty leftPeak;
	private DoubleProperty rightPeak;

	NativeAudioMacHandler(PadContent content) {
		super(content);

		id = counter++;
		positionProperty = new SimpleObjectProperty<>();
		durationProperty = new SimpleObjectProperty<>();

		leftPeak = new SimpleDoubleProperty();
		rightPeak = new SimpleDoubleProperty();
	}

	int getId() {
		return id;
	}

	@Override
	public void play() {
		NativeAudio.setLoop(id, getContent().getPad().getPadSettings().isLoop());
		NativeAudio.play(id);
	}

	@Override
	public void pause() {
		NativeAudio.pause(id);
	}

	@Override
	public void stop() {
		NativeAudio.stop(id);
	}

	@Override
	public void seekToStart() {
		NativeAudio.seek(id, 0);
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
	public Duration getDuration() {
		return durationProperty.get();
	}

	@Override
	public ReadOnlyObjectProperty<Duration> durationProperty() {
		return durationProperty;
	}

	@Override
	public void setVolume(double volume) {
		NativeAudio.setVolume(id, volume);

	}

	@Override
	public boolean isMediaLoaded() {
		return isLoaded;
	}

	@Override
	public void loadMedia(Path[] paths) {
		Worker.runLater(() ->
		{
			isLoaded = NativeAudio.load(id, paths[0].toString());
			if (isLoaded) {
				Platform.runLater(() ->
				{
					durationProperty.set(Duration.seconds(NativeAudio.getDuration(id)));
					getContent().getPad().setStatus(PadStatus.READY);
					if (getContent().getPad().isPadVisible()) {
						getContent().getPad().getController().getView().showBusyView(false);
					}
				});
			}
		});
	}

	@Override
	public void unloadMedia() {
		NativeAudio.dispose(id);
	}

	@Override
	public DoubleProperty audioLevelProperty(Channel channel) {
		if (channel == Channel.LEFT) {
			return leftPeak;
		} else if (channel == Channel.RIGHT) {
			return rightPeak;
		}
		return null;
	}

	@Override
	public double getAudioLevel(Channel channel) {
		return audioLevelProperty(channel).get();
	}
}
