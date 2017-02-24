package de.tobias.playpad.server.sync.listener.upstream;

import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.MediaPath;
import de.tobias.playpad.server.Server;
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
			handleInvalidation(json -> {
				json.addProperty(PropertyDef.FIELD, PropertyDef.PATH_PATH);
				json.addProperty(PropertyDef.VALUE, newValue.toString());
			});
		};
	}

	private void handleInvalidation(Consumer<JsonObject> handler) {
		if (ServerUtils.isNewValueComingFromServer()) {
			return;
		}
		JsonObject json = new JsonObject();
		json.addProperty(PropertyDef.ID, path.getId().toString());
		json.addProperty(PropertyDef.PATH_PAD_REF, path.getPad().getUuid().toString());
		json.addProperty(PropertyDef.CMD, Commands.PATH_UPDATE);

		handler.accept(json);

		Server server = PlayPadPlugin.getServerHandler().getServer();
		server.push(json);
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
