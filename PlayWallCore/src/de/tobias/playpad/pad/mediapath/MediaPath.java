package de.tobias.playpad.pad.mediapath;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.project.ProjectSettings;
import de.tobias.playpad.server.sync.command.CommandManager;
import de.tobias.playpad.server.sync.command.Commands;
import de.tobias.playpad.server.sync.listener.upstream.PathUpdateListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Created by tobias on 21.02.17.
 */
public class MediaPath implements Cloneable {

	private UUID id;
	private ObjectProperty<Path> path;
	private Pad pad;

	private PathUpdateListener pathListener;

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

		pathListener = new PathUpdateListener(this);
		if (pad.getProject().getProjectReference().isSync()) {
			addSyncListener();
		}
	}

	public UUID getId() {
		return id;
	}

	public Path getPath() {
		return path.get();
	}

	public void setPath(Path path, boolean load) {
		PadContent content = pad.getContent();
		if (content != null) {
			content.unloadMedia(this);
		}

		Path finalPath = getRealPath(path);
		this.path.set(finalPath);

		if (load) {
			content = pad.getContent();
			if (content != null) {
				content.loadMedia(this);
			}
		}
	}

	public ReadOnlyObjectProperty<Path> pathProperty() {
		return path;
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

	private void addSyncListener() {
		pathListener.addListener();
	}

	public void removeSyncListener() {
		pathListener.removeListener();
	}

	public MediaPath clone(Pad pad) throws CloneNotSupportedException {
		MediaPath clone = (MediaPath) super.clone();
		clone.id = UUID.randomUUID();
		clone.path = new SimpleObjectProperty<>(Paths.get(getPath().toUri()));
		clone.pad = pad;

		if (pad.getProject().getProjectReference().isSync()) {
			CommandManager.execute(Commands.PATH_ADD, pad.getProject().getProjectReference(), clone);
			clone.pathListener = new PathUpdateListener(clone);
			clone.addSyncListener();
		}

		return clone;
	}
}
