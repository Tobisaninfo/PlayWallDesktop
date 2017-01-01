package de.tobias.playpad.mediaplugin.video;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.dom4j.Element;

import de.tobias.playpad.mediaplugin.main.impl.MediaPluginImpl;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadSettings;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.pad.content.play.Durationable;
import de.tobias.playpad.pad.content.play.Pauseable;
import de.tobias.playpad.project.ProjectExporter;
import de.tobias.playpad.volume.VolumeManager;
import de.tobias.utils.util.ZipFile;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class VideoContent extends PadContent implements Pauseable, Durationable {

	public static final String VIDEO_LAST_FRAME = "Video.LastFrame";

	private final String type;
	private Media media;
	private MediaPlayer player;

	private Path path;

	private transient ObjectProperty<Duration> durationProperty = new SimpleObjectProperty<>();
	private transient ObjectProperty<Duration> positionProperty = new SimpleObjectProperty<>();

	private transient ChangeListener<Number> padVolumeListener;

	private transient boolean holdLastFrame = false;

	public VideoContent(String type, Pad pad) {
		super(pad);
		this.type = type;
		padVolumeListener = (a, b, c) ->
		{
			updateVolume();
		};
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	@Override
	public void handlePath(Path path) throws IOException {
		unloadMedia();
		setPath(getRealPath(path));
		loadMedia();
	}

	@Override
	public void updateVolume() {
		if (player != null) {
			VolumeManager manager = Pad.getVolumeManager();
			double volume = manager.computeVolume(getPad());
			player.setVolume(volume);
		}
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void play() {
		getPad().setEof(false);
		MediaPluginImpl.getInstance().getVideoViewController().setMediaPlayer(player, getPad());
		if (holdLastFrame) {
			holdLastFrame = false;
			player.seek(Duration.ZERO);
		}

		player.play();
		holdLastFrame = false;
	}

	@Override
	public void pause() {
		player.pause();
	}

	@Override
	public boolean stop() {
		PadSettings padSettings = getPad().getPadSettings();

		if (padSettings.getCustomSettings().containsKey(VIDEO_LAST_FRAME) && !holdLastFrame && getPad().isEof()) {
			if ((boolean) padSettings.getCustomSettings().get(VIDEO_LAST_FRAME)) {
				getPad().setStatus(PadStatus.PAUSE);
				holdLastFrame = true;
				return false;
			}
		}
		player.stop();
		MediaPluginImpl.getInstance().getVideoViewController().setMediaPlayer(null, null);
		return true;
	}

	@Override
	public boolean isPadLoaded() {
		return player != null;
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

	public MediaPlayer getPlayer() {
		return player;
	}

	@Override
	public void loadMedia() {
		if (Files.exists(path)) {
			Platform.runLater(() ->
			{
				if (getPad().isPadVisible()) {
					getPad().getController().getView().showBusyView(true);
				}
			});
			media = new Media(path.toUri().toString());

			// Old Player
			if (player != null) {
				stop();
			}

			player = new MediaPlayer(media);

			// Player Listener
			player.setOnReady(() ->
			{
				getPad().setStatus(PadStatus.READY);

				Platform.runLater(() ->
				{
					if (getPad().isPadVisible()) {
						getPad().getController().getView().showBusyView(false);
					}
				});
			});

			player.setOnError(() ->
			{
				Platform.runLater(() ->
				{
					if (getPad().isPadVisible()) {
						getPad().getController().getView().showBusyView(false);
					}
				});
				// getPad().throwException(path, player.getError()); TODO Error Handling User
			});
			player.setOnEndOfMedia(() ->
			{
				if (!getPad().getPadSettings().isLoop()) {
					getPad().setEof(true);
					getPad().setStatus(PadStatus.STOP);
				} else {
					// Loop
					player.seek(Duration.ZERO);
				}
			});

			durationProperty.bind(player.totalDurationProperty());
			positionProperty.bind(player.currentTimeProperty());

			getPad().getPadSettings().volumeProperty().addListener(padVolumeListener);
		}
	}

	@Override
	public void unloadMedia() {
		// First Stop the pad (if playing)
		getPad().setStatus(PadStatus.STOP);

		durationProperty.unbind();
		positionProperty.unbind();

		getPad().getPadSettings().volumeProperty().removeListener(padVolumeListener);

		player = null;
		media = null;
		durationProperty.set(null);

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

	@Override
	public PadContent clone() throws CloneNotSupportedException {
		VideoContent clone = (VideoContent) super.clone();
		clone.path = Paths.get(path.toUri());
		clone.loadMedia();
		return clone;
	}
}