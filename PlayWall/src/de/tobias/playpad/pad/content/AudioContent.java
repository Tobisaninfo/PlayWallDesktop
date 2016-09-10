package de.tobias.playpad.pad.content;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.dom4j.Element;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.audio.AudioHandler;
import de.tobias.playpad.audio.AudioRegistry;
import de.tobias.playpad.audio.Equalizable;
import de.tobias.playpad.audio.fade.Fading;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.pad.conntent.path.SinglePathContent;
import de.tobias.playpad.pad.conntent.play.Durationable;
import de.tobias.playpad.pad.conntent.play.Fadeable;
import de.tobias.playpad.pad.conntent.play.IVolume;
import de.tobias.playpad.pad.conntent.play.Pauseable;
import de.tobias.playpad.project.ProjectExporter;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.volume.VolumeManager;
import de.tobias.utils.util.ZipFile;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.media.AudioEqualizer;
import javafx.util.Duration;

public class AudioContent extends PadContent implements Pauseable, Durationable, Fadeable, Equalizable, SinglePathContent, IVolume {

	private static final String TYPE = "audio";

	private Path path;
	private AudioHandler audioHandler;

	private ObjectProperty<Duration> durationProperty = new SimpleObjectProperty<>();
	private ObjectProperty<Duration> positionProperty = new SimpleObjectProperty<>();

	private ChangeListener<Number> volumeListener;

	private Fading fading;

	private transient Transition transition;

	public AudioContent(Pad pad) {
		super(pad);
		volumeListener = (a, b, c) ->
		{
			updateVolume();
		};
	}

	@Override
	public Path getPath() {
		return path;
	}

	@Override
	public void handlePath(Path path) throws NoSuchComponentException, IOException {
		// handle old media
		unloadMedia();

		this.path = getRealPath(path);

		// handle new media
		loadMedia();
	}

	@Override
	public void updateVolume() {
		double volume = Pad.getVolumeManager().computeVolume(getPad());
		audioHandler.setVolume(volume);
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
		Pad pad = getPad();

		if (pad.getPadSettings().getFade().getFadeIn().toMillis() > 0) {
			fading.fadeIn(pad.getPadSettings().getFade().getFadeIn());
		}
	}

	@Override
	public void fadeOut(Runnable onFinish) {
		if (transition != null) {
			transition.stop();
		}

		if (getPad().getPadSettings().getFade().getFadeOut().toMillis() > 0) {
			fading.fadeOut(getPad().getPadSettings().getFade().getFadeOut(), () ->
			{
				onFinish.run();
				updateVolume();
			});
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
	public void setFadeLevel(double level) {
		Pad pad = getPad();
		VolumeManager manager = Pad.getVolumeManager();

		audioHandler.setVolume(level * manager.computeVolume(pad));
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
		// init audio implementation
		AudioRegistry audioRegistry = PlayPadPlugin.getRegistryCollection().getAudioHandlers();
		audioHandler = audioRegistry.getCurrentAudioHandler().createAudioHandler(this);

		fading = new Fading(this);

		if (Files.exists(path)) {
			audioHandler.loadMedia(new Path[] { path });

			durationProperty.bind(audioHandler.durationProperty());
			positionProperty.bind(audioHandler.positionProperty());

			getPad().getPadSettings().volumeProperty().addListener(volumeListener);
		} else {
			// getPad().throwException(path, new FileNotFoundException()); TODO Error Handling User
		}
	}

	@Override
	public void unloadMedia() {
		durationProperty.unbind();
		positionProperty.unbind();

		getPad().getPadSettings().volumeProperty().removeListener(volumeListener);

		if (audioHandler != null)
			audioHandler.unloadMedia();

		Platform.runLater(() ->
		{
			if (getPad() != null) {
				getPad().setStatus(PadStatus.EMPTY);
			}
		});
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
	public void importMedia(Path mediaFolder, ZipFile zip, Element element) {
		String fileName = Paths.get(element.getStringValue()).getFileName().toString();
		Path mediaFile = Paths.get(ProjectExporter.mediaFolder, fileName);

		Path desFile = mediaFolder.resolve(fileName);

		try {
			if (Files.notExists(desFile.getParent())) {
				Files.createDirectories(desFile.getParent());
			}

			if (Files.notExists(desFile))
				zip.getFile(mediaFile, desFile);

			element.setText(desFile.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void exportMedia(ZipFile zip, Element element) {
		Path desPath = Paths.get(ProjectExporter.mediaFolder, path.getFileName().toString());
		try {
			if (Files.notExists(desPath.getParent())) {
				Files.createDirectories(desPath.getParent());
			}
			zip.addFile(path, desPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
