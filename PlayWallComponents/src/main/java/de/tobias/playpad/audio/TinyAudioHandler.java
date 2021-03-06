package de.tobias.playpad.audio;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.io.PathUtils;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.pad.content.play.Seekable;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.settings.GlobalSettings;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.util.Duration;
import javazoom.jl.converter.Converter;
import javazoom.jl.decoder.JavaLayerException;
import kuusisto.tinysound.Music;
import kuusisto.tinysound.TinySound;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TinyAudioHandler extends AudioHandler implements Soundcardable, Seekable {

	public static final String SOUND_CARD = "SoundCard";

	static final String TYPE = "TinyAudio";
	private static final String MP3 = "mp3";

	private static final int SLEEP_TIME_POSITION = 50;

	private static ExecutorService executorService;

	private static Thread positionThread;
	private static List<TinyAudioHandler> playedHandlers = new ArrayList<>();

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

					for (Iterator<TinyAudioHandler> iterator = playedHandlers.iterator(); iterator.hasNext(); ) {
						TinyAudioHandler handler = iterator.next();
						Pad pad = handler.getContent().getPad();

						if (handler.music != null) {
							if (!handler.music.playing()) {
								if (!pad.getPadSettings().isLoop()) {
									pad.setEof(true);

									// Remove from Loop and Stop
									iterator.remove();
									Platform.runLater(() -> pad.setStatus(PadStatus.STOP));
								}
							}
						}

						// Differenz seit Play bis jetzt
						long diff = System.currentTimeMillis() - handler.start;

						Duration position = Duration.millis(diff);

						// Für Loop wieder am Anfang anfangen -> Wenn Aktuelle Position
						if (handler.duration.isNotNull().get()) {
							if (position.greaterThan(handler.duration.get())) {
								position.subtract(handler.duration.get());
								handler.start += handler.duration.get().toMillis();
							}
						}

						// Update der Zeit
						Platform.runLater(() -> handler.position.set(position));
					}

					Thread.sleep(SLEEP_TIME_POSITION);
				} catch (InterruptedException | ConcurrentModificationException ignored) {
				} catch (Exception e) {
					Logger.error(e);
				}
			}
		});

		positionThread.start();

		executorService = Executors.newFixedThreadPool(1);
	}

	private Music music;
	private boolean pause; // Play fängt immer vorne an. Wenn diese Variable True ist dann wird Resume aufgerufen

	private ObjectProperty<Duration> duration;
	private ObjectProperty<Duration> position;
	private long start;

	private BooleanProperty loadedProperty;

	TinyAudioHandler(PadContent content) {
		super(content);

		duration = new SimpleObjectProperty<>();
		position = new SimpleObjectProperty<>();
		loadedProperty = new SimpleBooleanProperty();
	}

	@Override
	public void play() {
		if (music != null) {
			if (!pause) {
				if (!getContent().getPad().getPadSettings().isLoop()) {
					music.play(false); // Kein Loop
				} else {
					music.play(true); // Mit Loop
				}
				Platform.runLater(() -> position.set(Duration.ZERO));
				start = System.currentTimeMillis();
			} else {
				music.resume();
				start = (long) (System.currentTimeMillis() - position.get().toMillis());
			}
			pause = false;

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
	}

	@Override
	public void pause() {
		if (music != null) {
			music.pause();
			playedHandlers.remove(this);
			pause = true;
		}
	}

	@Override
	public void stop() {
		if (music != null) {
			music.stop();
			playedHandlers.remove(this);
			pause = false;
		}
	}

	@Override
	public void seekToStart() {
		stop();
		play();
	}

	@Override
	public Duration getPosition() {
		return position.get();
	}

	@Override
	public ReadOnlyObjectProperty<Duration> positionProperty() {
		return position;
	}

	@Override
	public Duration getDuration() {
		return duration.get();
	}

	@Override
	public ReadOnlyObjectProperty<Duration> durationProperty() {
		return duration;
	}

	@Override
	public void setVolume(double volume) {
		if (music != null) {
			music.setVolume(volume);
		}
	}

	@Override
	public boolean isMediaLoaded() {
		return loadedProperty.get();
	}

	@Override
	public void loadMedia(Path[] paths) {
		String audioCardName = (String) Profile.currentProfile().getProfileSettings().getAudioUserInfo().get(SOUND_CARD);
		initTinySound(audioCardName);

		unloadMedia();
		executorService.submit(() ->
		{
			Path path = getContent().getPad().getPath();
			try {
				URL url = path.toUri().toURL();

				// Convert wenn mp3
				if (PathUtils.getFileExtension(url.getFile()).toLowerCase().endsWith(MP3)) {
					GlobalSettings globalSettings = PlayPadPlugin.getInstance().getGlobalSettings();
					Path wavPath = globalSettings.getCachePath().resolve(path.getFileName().toString() + ".wav");
					url = convertMp3ToWav(path, wavPath, getContent().getPad());
				}

				// Load
				music = TinySound.loadMusic(url, true);
				getContent().updateVolume();
				calcDuration(url);

				Platform.runLater(() ->
				{
					loadedProperty.set(true);
					getContent().getPad().setStatus(PadStatus.READY);
				});
			} catch (Exception e) {
				loadedProperty.set(false);
				// getContent().getPad().throwException(path, e); TODO Error Handling User
				Logger.error(e);
			} finally {
				Platform.runLater(() ->
				{
					if (getContent().getPad().isPadVisible()) {
						getContent().getPad().getController().getView().showBusyView(false);
					}
				});
			}
		});
	}

	private void calcDuration(URL url) throws UnsupportedAudioFileException, IOException {
		AudioInputStream iStr = AudioSystem.getAudioInputStream(url);
		double max = 1000.0 * iStr.getFrameLength() / iStr.getFormat().getFrameRate();
		Duration duration = Duration.millis(max);
		Platform.runLater(() -> this.duration.set(duration));
		iStr.close();
	}

	private static URL convertMp3ToWav(Path orgPath, Path wavPath, Pad pad) throws JavaLayerException, IOException {
		if (Files.notExists(wavPath)) {
			Files.createDirectories(wavPath.getParent());
			Files.createFile(wavPath);

			Converter converter = new Converter();
			converter.convert(orgPath.toString(), wavPath.toString());
		}
		return wavPath.toUri().toURL();
	}

	@Override
	public void unloadMedia() {
		if (music != null) {
			music.unload();
			music = null;
		}
		Platform.runLater(() -> duration.set(null));
		loadedProperty.set(false);
	}

	public void setMusic(Music music, URL url) throws UnsupportedAudioFileException, IOException {
		this.music = music;
		calcDuration(url);
		loadedProperty.set(true);
	}

	private static String audioCardName;

	private void initTinySound(String audioCardName) {
		if (TinyAudioHandler.audioCardName != null) {
			if (!TinyAudioHandler.audioCardName.equals(audioCardName)) {
				TinySound.shutdown();
			}
		}

		if (!TinySound.isInitialized()) {
			// INIT
			try {
				// Init mit spezieler Sound Card
				for (Info info : AudioSystem.getMixerInfo()) {
					if (info.getName().equals(audioCardName)) {
						TinyAudioHandler.audioCardName = audioCardName;

						TinySound.init(info);
						break;
					}
				}

			} catch (SecurityException | IllegalArgumentException | LineUnavailableException e) {
				Logger.error(e);
			}
			// Init mit Default Sound Card, wenn keine Ausgewählt wurde
			if (!TinySound.isInitialized()) {
				TinySound.init();
			}
		}
	}

	static void shutdown() {
		executorService.shutdown();
		positionThread.interrupt();
	}

	@Override
	public void setOutputDevice(String name) {
		initTinySound(name);
	}
}
