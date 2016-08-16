package de.tobias.playpad.audio;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.UnsupportedAudioFileException;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.pad.content.AudioContent;
import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.playpad.settings.Profile;
import de.tobias.utils.util.FileUtils;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.Duration;
import javazoom.jl.converter.Converter;
import javazoom.jl.decoder.JavaLayerException;
import kuusisto.tinysound.Music;
import kuusisto.tinysound.TinySound;

public class TinyAudioHandler extends AudioHandler {

	public static final String SOUND_CARD = "SoundCard";

	public static final String TYPE = "TinyAudio";
	public static final String NAME = "Java Audiostream";
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

					for (Iterator<TinyAudioHandler> iterator = playedHandlers.iterator(); iterator.hasNext();) {
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
				} catch (InterruptedException e) {
				} catch (ConcurrentModificationException e) {
				} catch (Exception e) {
					e.printStackTrace();
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

	public TinyAudioHandler(PadContent content) {
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
			if (playedHandlers.contains(this))
				playedHandlers.remove(this);
			pause = true;
		}
	}

	@Override
	public void stop() {
		if (music != null) {
			music.stop();
			if (playedHandlers.contains(this))
				playedHandlers.remove(this);
			pause = false;
		}
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
	public void setVolume(double volume, double masterVolume, double customVolume) {
		if (music != null) {
			music.setVolume(volume * masterVolume * customVolume);
		}
	}

	@Override
	public boolean isMediaLoaded() {
		return loadedProperty.get();
	}

	@Override
	public void loadMedia(Path[] paths) {
		initTinySound();

		unloadMedia();
		Platform.runLater(() ->
		{
			if (getContent().getPad().isPadVisible()) {
				getContent().getPad().getController().getView().showBusyView(true);
			}
		});

		executorService.submit(() ->
		{
			Path path = ((AudioContent) getContent()).getPath();
			try {
				URL url = path.toUri().toURL();

				// Convert wenn mp3
				if (FileUtils.getFileExtention(url.getFile()).toLowerCase().endsWith(MP3)) {
					GlobalSettings globalSettings = PlayPadPlugin.getImplementation().getGlobalSettings();
					Path wavPath = globalSettings.getCachePath().resolve(path.getFileName().toString() + ".wav");
					url = convertMp3ToWav(path, wavPath, getContent().getPad());
				}

				// Load
				music = TinySound.loadMusic(url, true);
				calcDuration(url);

				Platform.runLater(() ->
				{
					loadedProperty.set(true);
					getContent().getPad().setStatus(PadStatus.READY);
				});
			} catch (Exception e) {
				loadedProperty.set(false);
				getContent().getPad().throwException(path, e);
				e.printStackTrace();
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
		double max = 1000.0 * (double) iStr.getFrameLength() / (double) iStr.getFormat().getFrameRate();
		Duration duration = Duration.millis(max);
		Platform.runLater(() -> this.duration.set(duration));
		iStr.close();
	}

	private static URL convertMp3ToWav(Path orgPath, Path wavPath, Pad pad) throws JavaLayerException, URISyntaxException, IOException {
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

	private void initTinySound() {
		String audioCardName = (String) Profile.currentProfile().getProfileSettings().getAudioUserInfo().get(SOUND_CARD);

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
				e.printStackTrace();
			}
			// Init mit Default Sound Card, wenn keine Ausgewählt wurde
			if (!TinySound.isInitialized()) {
				TinySound.init();
			}
		}
	}

	public static void shutdown() {
		executorService.shutdown();
		positionThread.interrupt();
	}
}
