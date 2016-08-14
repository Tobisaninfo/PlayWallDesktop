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

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Mixer;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.pad.content.AudioContent;
import de.tobias.playpad.settings.Profile;
import de.tobias.utils.util.FileUtils;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.Duration;
import javazoom.jl.converter.Converter;
import javazoom.jl.decoder.JavaLayerException;

public class ClipAudioHandler extends AudioHandler {

	public static final String TYPE = "clip";
	public static final String NAME = "Clip (Experimental)";
	private static final String MP3 = "mp3";

	private Mixer mixer;
	private Clip clip;
	private FloatControl volumeControl;

	private ObjectProperty<Duration> durationProperty;
	private ObjectProperty<Duration> positionProperty;

	private transient boolean pause;
	private transient boolean stop;

	private static final int SLEEP_TIME_POSITION = 50;
	private static Thread positionThread;
	private static List<ClipAudioHandler> playedHandlers = new ArrayList<>();

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

					for (Iterator<ClipAudioHandler> iterator = playedHandlers.iterator(); iterator.hasNext();) {
						ClipAudioHandler handler = iterator.next();
						Pad pad = handler.getContent().getPad();

						if (handler.clip != null) {
							if (handler.clip.getMicrosecondLength() == handler.clip.getMicrosecondPosition() || !handler.pause || handler.stop) {
								if (!pad.getPadSettings().isLoop()) {
									pad.setEof(true);

									// Remove from Loop and Stop
									iterator.remove();
									Platform.runLater(() -> pad.setStatus(PadStatus.STOP));
								}
							}
						}

						// Update der Zeit
						Platform.runLater(() -> handler.positionProperty.set(Duration.millis(handler.clip.getMicrosecondPosition() / 1000.0)));
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
	}

	public ClipAudioHandler(PadContent content) {
		super(content);
		durationProperty = new SimpleObjectProperty<>();
		positionProperty = new SimpleObjectProperty<>();
	}

	@Override
	public void play() {
		if (!pause) {
			clip.setFramePosition(0);
			pause = false;
		}
		stop = false;

		if (getContent().getPad().getPadSettings().isLoop())
			clip.loop(Clip.LOOP_CONTINUOUSLY); // Loop
		else
			clip.start(); // Einfach Play

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
		clip.stop();
		pause = true;
		if (playedHandlers.contains(this))
			playedHandlers.remove(this);
	}

	@Override
	public void stop() {
		clip.stop();
		stop = true;
		pause = false;
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
	public void setVolume(double volume, double masterVolume, double customVolume) {
		setVolume(masterVolume * volume * customVolume);
	}

	/**
	 * Lineaer to dB
	 * 
	 * @param volume
	 *            [0, 1]
	 */
	private void setVolume(double volume) {
		if (volumeControl != null) {
			if (volume == 1.0) {
				volumeControl.setValue(0);
			} else if (volume == 0.0) {
				volumeControl.setValue(volumeControl.getMinimum());
			} else {
				float newValue = (float) (Math.log10(volume) * 20.0);
				if (newValue > volumeControl.getMinimum())
					volumeControl.setValue(newValue);
				else
					volumeControl.setValue(volumeControl.getMinimum());
			}
		}
	}

	@Override
	public boolean isMediaLoaded() {
		return clip != null;
	}

	@Override
	public void loadMedia(Path[] paths) {
		mixer = AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]);

		DataLine.Info info = new Info(Clip.class, null);
		try {
			clip = (Clip) mixer.getLine(info);

			Path path = ((AudioContent) getContent()).getPath();
			URL url = path.toUri().toURL();

			// Convert wenn mp3
			if (FileUtils.getFileExtention(url.getFile()).toLowerCase().endsWith(MP3)) {
				Path wavPath = Profile.currentProfile().getProfileSettings().getCachePath().resolve(path.getFileName().toString() + ".wav");
				url = convertMp3ToWav(path, wavPath, getContent().getPad());
			}

			AudioContent content = (AudioContent) getContent();
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(url);

			clip.open(audioInputStream);
			volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

			durationProperty.set(Duration.millis(clip.getMicrosecondLength() / 1000.0));
			content.getPad().setStatus(PadStatus.READY);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		clip.close();
		mixer.close();

		clip = null;
		mixer = null;
	}

	public static void shutdown() {
		positionThread.interrupt();
	}

}
