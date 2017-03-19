package de.tobias.playpad.nawin.audio;

import de.tobias.playpad.audio.AudioHandler;
import de.tobias.playpad.audio.Soundcardable;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.profile.Profile;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.Duration;
import nativeaudio.NativeAudio;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

public class NativeAudioWinHandler extends AudioHandler implements Soundcardable {

	public static final String SOUND_CARD = "SoundCard";

	private NativeAudio audioHandler;
	private ObjectProperty<Duration> durationProperty;
	private ObjectProperty<Duration> positionProperty;

	private static Thread positionThread;
	private static List<NativeAudioWinHandler> playedHandlers = new ArrayList<>();
	private static final int SLEEP_TIME_POSITION = 50;

	static {
		positionThread = new Thread(() ->
		{
			while (true) {
				try {
					if (playedHandlers.isEmpty()) {
						synchronized (positionThread) {
							positionThread.wait();
						}
					}

					for (Iterator<NativeAudioWinHandler> iterator = playedHandlers.iterator(); iterator.hasNext();) {
						NativeAudioWinHandler handler = iterator.next();
						Pad pad = handler.getContent().getPad();

						if (handler.audioHandler != null) {
							if (!handler.audioHandler.isPlaying()) {
								if (!pad.getPadSettings().isLoop()) {
									System.out.println("Stop");
									pad.setEof(true);

									// Remove from Loop and Stop
									iterator.remove();
									Platform.runLater(() -> pad.setStatus(PadStatus.STOP));
								}
							}
						}

						Duration position = Duration.millis(handler.audioHandler.getPosition());

						// Update der Zeit
						Platform.runLater(() -> handler.positionProperty.set(position));
					}

					Thread.sleep(SLEEP_TIME_POSITION);
				} catch (InterruptedException e) {} catch (ConcurrentModificationException e) {} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		positionThread.start();
	}

	public NativeAudioWinHandler(PadContent content) {
		super(content);
		durationProperty = new SimpleObjectProperty<>();
		positionProperty = new SimpleObjectProperty<>();
	}

	@Override
	public void play() {
		audioHandler.setLoop(getContent().getPad().getPadSettings().isLoop());

		audioHandler.play();

		boolean start = false;
		if (playedHandlers.isEmpty()) {
			start = true;
		}

		if (!playedHandlers.contains(this))
			playedHandlers.add(this);
		if (start) {
			synchronized (positionThread) {
				positionThread.notify();
			}
		}
	}

	@Override
	public void pause() {
		audioHandler.pause();
		if (playedHandlers.contains(this))
			playedHandlers.remove(this);
	}

	@Override
	public void stop() {
		audioHandler.stop();
		if (playedHandlers.contains(this))
			playedHandlers.remove(this);
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
		if (audioHandler != null) {
			audioHandler.setVolume((float) volume);
		}
	}

	@Override
	public boolean isMediaLoaded() {
		return audioHandler != null;
	}

	@Override
	public void loadMedia(Path[] paths) {
		if (audioHandler == null)
			audioHandler = new NativeAudio();
		audioHandler.load(paths[0].toString());

		String name = (String) Profile.currentProfile().getProfileSettings().getAudioUserInfo().get(NativeAudioWinHandler.SOUND_CARD);
		audioHandler.setDevice(name);

		Platform.runLater(() ->
		{
			durationProperty.set(Duration.millis(audioHandler.getDuration()));
			getContent().getPad().setStatus(PadStatus.READY);
			if (getContent().getPad().isPadVisible()) {
				getContent().getPad().getController().getView().showBusyView(false);
			}
		});
	}

	@Override
	public void unloadMedia() {
		if (audioHandler != null) {
			audioHandler.unload();
			audioHandler = null;
		}
	}

	@Override
	public void setOutputDevice(String name) {
		audioHandler.setDevice(name);
	}

}
