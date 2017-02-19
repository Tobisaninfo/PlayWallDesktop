package de.tobias.playpad.server.sync.listener.downstream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.project.ref.ProjectReferences;
import de.tobias.playpad.server.sync.PropertyDef;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by tobias on 19.02.17.
 */
public class ProjectAddListener implements ServerListener {
	@Override
	public void listen(JsonElement element) {
		if (element instanceof JsonObject) {
			JsonObject json = (JsonObject) element;

			UUID uuid = UUID.fromString(json.get(PropertyDef.ID).getAsString());
			String name = json.get(PropertyDef.PROJECT_NAME).getAsString();

			ProjectReference ref = new ProjectReference(uuid, name, null, true); // No Profile Selected
			try {
				ProjectReferences.addProject(ref);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
