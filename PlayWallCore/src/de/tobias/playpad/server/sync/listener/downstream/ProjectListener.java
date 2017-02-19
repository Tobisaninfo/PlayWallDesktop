package de.tobias.playpad.server.sync.listener.downstream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.server.sync.listener.PropertyDef;

import java.util.UUID;

/**
 * Handles incoming changes on project from server and set the right property.
 */
public class ProjectListener implements ServerListener {

	@Override
	public void listen(JsonElement element) {
		if (element instanceof JsonObject) {
			JsonObject json = (JsonObject) element;

			UUID uuid = UUID.fromString(json.get("project_id").getAsString());
			Project project = PlayPadPlugin.getImplementation().getCurrentProject();

			// Check if right project is open
			if (project.getProjectReference().getUuid() == uuid) {
				String field = json.get("field").getAsString();
				if (field.equals(PropertyDef.PROJECT_NAME)) {
					String name = json.get("value").getAsString();
					project.getProjectReference().setName(name);
				}
			}
		}
	}
}
