package de.tobias.playpad.server.sync.listener.upstream;

import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.mediapath.MediaPath;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.command.Change;
import de.tobias.playpad.server.sync.command.CommandManager;
import de.tobias.playpad.server.sync.command.Commands;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.ServerUtils;
import javafx.beans.value.ChangeListener;

import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Listen to the properties of Project to send changes to the server.
 */
public class PathUpdateListener {

	private MediaPath path;

	private ChangeListener<Path> pathChangeListener;

	public PathUpdateListener(MediaPath path) {
		this.path = path;

		pathChangeListener = (observable, oldValue, newValue) -> {
			Change change = new Change(PropertyDef.PATH_PATH, newValue, path);
			CommandManager.execute(Commands.PATH_UPDATE, path.getPad().getProject().getProjectReference(), change);
		};
	}

	private boolean added;

	public void addListener() {
		if (!added) {
			added = true;
			path.pathProperty().addListener(pathChangeListener);
		}
	}

	public void removeListener() {
		added = false;
		path.pathProperty().removeListener(pathChangeListener);
	}
}
