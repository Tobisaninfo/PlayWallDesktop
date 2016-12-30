package de.tobias.playpad.namac;

import java.nio.file.Path;

import de.tobias.playpad.NativeAudio;
import de.tobias.playpad.audio.AudioHandler;
import de.tobias.playpad.audio.Peakable;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.utils.util.Worker;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.Duration;

public class NativeAudioMacHandler extends AudioHandler implements Peakable {

	private static int counter = 0;

	private final int id;
	ObjectProperty<Duration> positionProperty;
	private ObjectProperty<Duration> durationProperty;
	private boolean isLoaded;

	private DoubleProperty leftPeak;
	private DoubleProperty rightPeak;

	public NativeAudioMacHandler(PadContent content) {
		super(content);

		id = counter++;
		positionProperty = new SimpleObjectProperty<>();
		durationProperty = new SimpleObjectProperty<>();

		leftPeak = new SimpleDoubleProperty();
		rightPeak = new SimpleDoubleProperty();
	}

	protected int getId() {
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
		Platform.runLater(() ->
		{
			if (getContent().getPad().isPadVisible()) {
				getContent().getPad().getController().getView().showBusyView(true);
			}
		});
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