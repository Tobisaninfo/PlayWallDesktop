package de.tobias.playpad.server.sync.listener.downstream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.project.ref.ProjectReferences;
import de.tobias.playpad.server.sync.listener.PropertyDef;
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

			UUID uuid = UUID.fromString(json.get("id").getAsString());

			// Check if right project is open
			ProjectReference ref = ProjectReferences.getProject(uuid);
			if (ref != null) {
				String field = json.get("field").getAsString();
				if (field.equals(PropertyDef.PROJECT_NAME)) {
					String name = json.get("value").getAsString();
					Platform.runLater(() -> ref.setName(name));
				}
			}
		}
	}
}
