package de.tobias.playpad.pad.content;

import java.io.FileNotFoundException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.dom4j.Element;

import de.tobias.playpad.audio.AudioHandler;
import de.tobias.playpad.audio.AudioRegistry;
import de.tobias.playpad.audio.Equalizable;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.conntent.Durationable;
import de.tobias.playpad.pad.conntent.Fadeable;
import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.pad.conntent.Pauseable;
import de.tobias.playpad.project.ProjectExporter;
import de.tobias.playpad.settings.Profile;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.media.AudioEqualizer;
import javafx.util.Duration;

public class AudioContent extends PadContent implements Pauseable, Durationable, Fadeable, Equalizable {

	private static final String TYPE = "audio";

	private Path path;
	private AudioHandler audioHandler;

	private ObjectProperty<Duration> durationProperty = new SimpleObjectProperty<>();
	private ObjectProperty<Duration> positionProperty = new SimpleObjectProperty<>();

	private ChangeListener<Number> volumeListener;

	private transient Transition transition;

	public AudioContent(Pad pad) {
		super(pad);
		volumeListener = (a, b, c) ->
		{
			audioHandler.setVolume(c.doubleValue(), Profile.currentProfile().getProfileSettings().getVolume());
		};
	}

	public AudioContent(Pad pad, Path path) {
		this(pad);
		this.path = path;
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	@Override
	public void handlePath(Path path) {
		unloadMedia();
		setPath(path);
		loadMedia();
	}

	@Override
	public void setMasterVolume(double masterVolume) {
		if (audioHandler != null) {
			audioHandler.setVolume(getPad().getVolume(), masterVolume);
		}
	}

	@Override
	public String getType() {
		return TYPE;
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
	public void fadeIn() {
		if (transition != null) {
			transition.stop();
		}

		if (getPad().getFade().getFadeIn().toMillis() > 0) {
			double masterVolume = Profile.currentProfile().getProfileSettings().getVolume();
			audioHandler.setVolume(0, masterVolume);
			transition = new Transition() {

				{
					setCycleDuration(getPad().getFade().getFadeIn());
				}

				@Override
				protected void interpolate(double frac) {
					audioHandler.setVolume(frac * getPad().getVolume(), masterVolume);
				}
			};
			transition.setOnFinished(e ->
			{
				transition = null;
			});
			transition.play();
		}
	}

	@Override
	public void fadeOut(Runnable onFinish) {
		if (transition != null) {
			transition.stop();
		}

		if (getPad().getFade().getFadeOut().toMillis() > 0) {
			transition = new Transition() {

				{
					setCycleDuration(getPad().getFade().getFadeOut());
				}

				@Override
				protected void interpolate(double frac) {
					double masterVolume = Profile.currentProfile().getProfileSettings().getVolume();
					audioHandler.setVolume(getPad().getVolume() - frac * getPad().getVolume(), masterVolume);
				}
			};
			transition.setOnFinished(event ->
			{
				onFinish.run();

				double masterVolume = Profile.currentProfile().getProfileSettings().getVolume();
				audioHandler.setVolume(getPad().getVolume(), masterVolume);
				transition = null;
			});
			transition.play();
		} else {
			onFinish.run();
		}
	}

	@Override
	public boolean isFading() {
		if (transition != null) {
			return true;
		}
		return false;
	}

	@Override
	public AudioEqualizer getAudioEqualizer() {
		if (audioHandler instanceof Equalizable) {
			return ((Equalizable) audioHandler).getAudioEqualizer();
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
		audioHandler = AudioRegistry.geAudioType().createAudioHandler(this);
		if (Files.exists(path)) {
			audioHandler.loadMedia(new Path[] { path });

			durationProperty.bind(audioHandler.durationProperty());
			positionProperty.bind(audioHandler.positionProperty());

			getPad().volumeProperty().addListener(volumeListener);
		} else {
			getPad().throwException(path, new FileNotFoundException());
		}
	}

	@Override
	public void unloadMedia() {
		durationProperty.unbind();
		positionProperty.unbind();

		getPad().volumeProperty().removeListener(volumeListener);

		if (audioHandler != null)
			audioHandler.unloadMedia();
		try {
			getPad().setStatus(PadStatus.EMPTY);
		} catch (Exception e) {
			Platform.runLater(() -> getPad().setStatus(PadStatus.EMPTY));
		}
	}

	@Override
	public void load(Element element) {
		path = Paths.get(element.getStringValue());
	}

	@Override
	public void save(Element element) {
		element.addText(path.toString());
	}

	@Override
	public void importMedia(Path mediaFolder, FileSystem zipfs, Element element) {
		String fileName = Paths.get(element.getStringValue()).getFileName().toString();
		Path mediaFile = zipfs.getPath(ProjectExporter.mediaFolder, fileName);
		if (Files.exists(mediaFile)) {
			Path desFile = mediaFolder.resolve(fileName);

			try {
				if (Files.notExists(desFile.getParent())) {
					Files.createDirectories(desFile.getParent());
				}
				Files.copy(mediaFile, desFile, StandardCopyOption.REPLACE_EXISTING);

				element.setText(desFile.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void exportMedia(FileSystem mediaFolder, Element element) {
		Path desPath = mediaFolder.getPath(ProjectExporter.mediaFolder, path.getFileName().toString());
		try {
			if (Files.notExists(desPath.getParent())) {
				Files.createDirectories(desPath.getParent());
			}
			Files.copy(path, desPath, StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
