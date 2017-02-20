package de.tobias.playpad.server.sync.listener.downstream.page;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPad;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.page.Page;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.project.ref.ProjectReferences;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.listener.downstream.ServerListener;
import javafx.application.Platform;

import java.util.UUID;

/**
 * Handles incoming changes on project from server and set the right property.
 */
public class PageUpdateListener implements ServerListener {

	@Override
	public void listen(JsonElement element) {
		if (element instanceof JsonObject) {
			JsonObject json = (JsonObject) element;

			UUID uuid = UUID.fromString(json.get(PropertyDef.ID).getAsString());

			Project project = PlayPadPlugin.getImplementation().getCurrentProject();
			if (project != null) {
				Page page = project.getPage(uuid);
				if (page != null) {
					String field = json.get(PropertyDef.FIELD).getAsString();
					if (field.equals(PropertyDef.PAGE_NAME)) {
						String name = json.get(PropertyDef.VALUE).getAsString();
						Platform.runLater(() -> page.setName(name));
					} else if (field.equals(PropertyDef.PAGE_POSITION)) {
						int position = json.get(PropertyDef.VALUE).getAsInt();
						Platform.runLater(() -> project.setPage(position, page));
					}
				}
			}
		}
	}
}
