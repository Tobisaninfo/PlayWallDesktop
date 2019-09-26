package de.tobias.playpad.server.sync.listener.downstream.project;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thecodelabs.logger.Logger;
import de.tobias.playpad.project.ref.ProjectReferenceManager;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.listener.ServerListener;

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

			try {
				ProjectReferenceManager.addProject(name, null, true);
			} catch (IOException e) {
				Logger.error(e);
			}
		}
	}
}
