package de.tobias.playpad.server.sync.listener.downstream.path;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.MediaPath;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.page.Page;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.listener.downstream.ServerListener;
import javafx.application.Platform;

import java.nio.file.Paths;
import java.util.UUID;

/**
 * Handles incoming changes on project from server and set the right property.
 */
public class PathUpdateListener implements ServerListener {

	@Override
	public void listen(JsonElement element) {
		if (element instanceof JsonObject) {
			JsonObject json = (JsonObject) element;

			UUID uuid = UUID.fromString(json.get(PropertyDef.ID).getAsString());

			Project project = PlayPadPlugin.getImplementation().getCurrentProject();
			if (project != null) {
				MediaPath mediaPath = project.getMediaPath(uuid);
				if (mediaPath != null) {
					String field = json.get(PropertyDef.FIELD).getAsString();
					if (field.equals(PropertyDef.PATH_PATH)) {
						String path = json.get(PropertyDef.VALUE).getAsString();
						Platform.runLater(() -> mediaPath.setPath(Paths.get(path)));
					}
				}
			}
		}
	}
}
