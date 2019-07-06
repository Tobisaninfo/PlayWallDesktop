package de.tobias.playpad.audio.mac;

import de.thecodelabs.utils.threading.Worker;
import de.tobias.playpad.audio.AudioHandler;
import de.tobias.playpad.audio.Peakable;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.pad.content.play.Seekable;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.util.Duration;

import java.nio.file.Path;

public class NativeAudioMacHandler extends AudioHandler implements Peakable, Seekable {

	ObjectProperty<Duration> positionProperty;
	private ObjectProperty<Duration> durationProperty;
	private boolean isLoaded;

	private DoubleProperty leftPeak;
	private DoubleProperty rightPeak;

	private AVAudioPlayerBridge bridge;

	NativeAudioMacHandler(PadContent content) {
		super(content);

		bridge = new AVAudioPlayerBridge();

		positionProperty = new SimpleObjectProperty<>();
		durationProperty = new SimpleObjectProperty<>();

		leftPeak = new SimpleDoubleProperty();
		rightPeak = new SimpleDoubleProperty();
	}

	AVAudioPlayerBridge getBridge() {
		return bridge;
	}

	@Override
	public void play() {
		bridge.setLoop(getContent().getPad().getPadSettings().isLoop());
		bridge.play();
	}

	@Override
	public void pause() {
		bridge.pause();
	}

	@Override
	public void stop() {
		bridge.stop();
	}

	@Override
	public void seekToStart() {
		bridge.seek(0);
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
		bridge.setVolume(volume);
	}

	@Override
	public boolean isMediaLoaded() {
		return isLoaded;
	}

	@Override
	public void loadMedia(Path[] paths) {
		Worker.runLater(() ->
		{
			isLoaded = bridge.load(paths[0].toString());
			if (isLoaded) {
				Platform.runLater(() ->
				{
					durationProperty.set(Duration.seconds(bridge.getDuration()));
					getContent().getPad().setStatus(PadStatus.READY);
					if (getContent().getPad().isPadVisible()) {
						getContent().getPad().getController().getView().showBusyView(false);
					}
				});
				getContent().updateVolume();
			}
		});
	}

	@Override
	public void unloadMedia() {
		bridge.dispose();
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
