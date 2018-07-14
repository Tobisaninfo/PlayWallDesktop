package de.tobias.playpad.server.sync.listener.downstream.project;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.project.ref.ProjectReferenceManager;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.listener.ServerListener;
import javafx.application.Platform;

import java.util.UUID;

/**
 * Handles incoming changes on project from server and set the right property.
 */
public class ProjectUpdateListener implements ServerListener {

	@Override
	public void listen(JsonElement element) {
		if (element instanceof JsonObject) {
			JsonObject json = (JsonObject) element;

			UUID uuid = UUID.fromString(json.get(PropertyDef.ID).getAsString());

			// Check if right project is open
			ProjectReference ref = ProjectReferenceManager.getProject(uuid);
			if (ref != null) {
				String field = json.get(PropertyDef.FIELD).getAsString();
				if (field.equals(PropertyDef.PROJECT_NAME)) {
					String name = json.get(PropertyDef.VALUE).getAsString();
					Platform.runLater(() -> ref.setName(name));
				}
			}
		}
	}
}
