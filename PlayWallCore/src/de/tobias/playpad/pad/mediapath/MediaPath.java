package de.tobias.playpad.pad.mediapath;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.project.ProjectSettings;
import de.tobias.playpad.server.sync.command.CommandManager;
import de.tobias.playpad.server.sync.command.Commands;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Created by tobias on 21.02.17.
 */
public class MediaPath implements Cloneable {

	private UUID id;
	private StringProperty fileName;
	private Pad pad;

	public MediaPath(UUID id, Path path, Pad pad) {
		this.id = id;
		this.fileName = new SimpleStringProperty(path.getFileName().toString());
		this.pad = pad;
	}

	public MediaPath(UUID id, String filename, Pad pad) {
		this.id = id;
		this.fileName = new SimpleStringProperty(filename);
		this.pad = pad;
	}

	public static MediaPath create(Pad pad, Path localPath) {
		MediaPath mediaPath = new MediaPath(UUID.randomUUID(), localPath, pad);
		MediaPool.getInstance().create(mediaPath, localPath);
		return mediaPath;
	}

	public static MediaPath create(Pad pad, String filename) {
		MediaPath mediaPath = new MediaPath(UUID.randomUUID(), filename, pad);
		MediaPool.getInstance().create(mediaPath);
		return mediaPath;
	}

	public UUID getId() {
		return id;
	}

	public Path getPath() {
		return MediaPool.getInstance().getPath(this);
	}

	public void setPath(Path path, boolean load) {
		PadContent content = pad.getContent();
		if (content != null) {
			content.unloadMedia(this);
		}

		Path finalPath = getRealPath(path);
		MediaPool.getInstance().setPath(this, finalPath);

		if (load) {
			content = pad.getContent();
			if (content != null) {
				content.loadMedia(this);
			}
		}
	}

	public String getFileName() {
		return fileName.get();
	}

	public Pad getPad() {
		return pad;
	}

	void setPad(Pad pad) {
		this.pad = pad;
	}

	/**
	 * Convert a path into the settings based path. If the media folder is used the path is converted.
	 *
	 * @param original path
	 * @return new path
	 * @since 6.2.0
	 */
	private Path getRealPath(Path original) {
		ProjectSettings settings = pad.getProject().getSettings();
		if (settings.isUseMediaPath()) {
			try {
				Path mediaFolder = settings.getMediaPath();
				Path newPath = mediaFolder.resolve(original.getFileName());

				if (Files.notExists(mediaFolder)) {
					Files.createDirectories(mediaFolder);
				}

				Files.copy(original, newPath, StandardCopyOption.REPLACE_EXISTING);
				return newPath;
			} catch (IOException e) {
				return original;
			}
		}
		return original;
	}

	public MediaPath clone(Pad pad) throws CloneNotSupportedException {
		MediaPath clone = (MediaPath) super.clone();
		clone.id = UUID.randomUUID();
		clone.fileName = new SimpleStringProperty(fileName.get());
		clone.pad = pad;

		if (pad.getProject().getProjectReference().isSync()) {
			CommandManager.execute(Commands.PATH_ADD, pad.getProject().getProjectReference(), clone);
		}

		return clone;
	}

	@Override
	public String toString() {
		return "MediaPath{" +
				"id=" + id +
				", fileName=" + fileName.get() +
				'}';
	}
}
