package de.tobias.playpad.server.sync.listener.downstream.page;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.page.Page;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.project.ref.ProjectReferences;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.listener.downstream.ServerListener;
import javafx.application.Platform;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by tobias on 19.02.17.
 */
public class PageAddListener implements ServerListener {
	@Override
	public void listen(JsonElement element) {
		if (element instanceof JsonObject) {
			JsonObject json = (JsonObject) element;

			UUID uuid = UUID.fromString(json.get(PropertyDef.ID).getAsString());
			UUID project_id = UUID.fromString(json.get(PropertyDef.PAGE_PROJECT).getAsString());
			String name = json.get(PropertyDef.PAGE_NAME).getAsString();
			int position = json.get(PropertyDef.PAGE_POSITION).getAsInt();

			Project project = PlayPadPlugin.getImplementation().getCurrentProject();
			if (project != null) {
				if (project.getProjectReference().getUuid().equals(project_id)) {
					Page page = new Page(uuid, position, name, project);
					Platform.runLater(() -> project.addPage(page));
				}
			}
		}
	}
}
