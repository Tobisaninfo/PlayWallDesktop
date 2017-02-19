package de.tobias.playpad.server.sync.listener.downstream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.project.ref.ProjectReferences;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by tobias on 19.02.17.
 */
public class ProjectRemoveListener implements ServerListener {
	@Override
	public void listen(JsonElement element) {
		if (element instanceof JsonObject) {
			JsonObject json = (JsonObject) element;

			UUID uuid = UUID.fromString(json.get("id").getAsString());
			ProjectReference ref = ProjectReferences.getProject(uuid);
			try {
				ProjectReferences.removeProject(ref);
			} catch (IOException | DocumentException e) {
				e.printStackTrace();
			}
		}
	}
}
