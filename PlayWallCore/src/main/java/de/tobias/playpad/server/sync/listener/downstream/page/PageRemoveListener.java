package de.tobias.playpad.server.sync.listener.downstream.page;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.page.Page;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.listener.ServerListener;
import javafx.application.Platform;

import java.util.UUID;

/**
 * Created by tobias on 19.02.17.
 */
public class PageRemoveListener implements ServerListener {
	@Override
	public void listen(JsonElement element) {
		if (element instanceof JsonObject) {
			JsonObject json = (JsonObject) element;

			UUID uuid = UUID.fromString(json.get(PropertyDef.ID).getAsString());
			Project project = PlayPadPlugin.getInstance().getCurrentProject();
			if (project != null) {
				Page page = project.getPage(uuid);
				if (page != null) {
					Platform.runLater(() -> project.removePage(page));
				}
			}
		}
	}
}
