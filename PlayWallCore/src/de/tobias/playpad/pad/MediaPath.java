package de.tobias.playpad.pad;

import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.project.ProjectSettings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Created by tobias on 21.02.17.
 */
public class MediaPath {

	private UUID id;
	private ObjectProperty<Path> path;
	private Pad pad;

	public MediaPath(Pad pad) {
		this(null, pad);
	}

	public MediaPath(Path path, Pad pad) {
		this(UUID.randomUUID(), path, pad);
	}

	public MediaPath(UUID id, Path path, Pad pad) {
		this.id = id;
		this.path = new SimpleObjectProperty<>(path);
		this.pad = pad;
	}

	public UUID getId() {
		return id;
	}

	public Path getPath() {
		return path.get();
	}

	public void setPath(Path path) {
		PadContent content = pad.getContent();
		if (content != null) {
			content.unloadMedia(this);
		}

		Path finalPath = getRealPath(path);
		this.path.set(finalPath);

		content = pad.getContent();
		if (content != null) {
			content.loadMedia(this);
		}
	}

	public ReadOnlyObjectProperty<Path> pathProperty() {
		return path;
	}

	public Pad getPad() {
		return pad;
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
}
