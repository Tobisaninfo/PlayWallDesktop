package de.tobias.playpad.server.sync.listener.downstream.pad;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.page.Page;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.listener.ServerListener;
import javafx.application.Platform;

import java.util.UUID;

/**
 * Created by tobias on 19.02.17.
 */
public class PadAddListener implements ServerListener {
	@Override
	public void listen(JsonElement element) {
		if (element instanceof JsonObject) {
			JsonObject json = (JsonObject) element;

			UUID uuid = UUID.fromString(json.get(PropertyDef.ID).getAsString());
			UUID pageId = UUID.fromString(json.get(PropertyDef.PAD_PAGE_REF).getAsString());
			String name = json.get(PropertyDef.PAD_NAME).getAsString();
			int position = json.get(PropertyDef.PAD_POSITION).getAsInt();
			String contentType = null;
			if (!json.get(PropertyDef.PAD_CONTENT_TYPE).isJsonNull()) {
				contentType = json.get(PropertyDef.PAD_CONTENT_TYPE).getAsString();
			}

			Project project = PlayPadPlugin.getInstance().getCurrentProject();
			if (project != null) {
				Page page = project.getPage(pageId);
				if (page != null) {
					Pad pad = new Pad(project, uuid, position, page, name, contentType);
					Platform.runLater(() -> page.setPad(position, pad));
				}
			}
		}
	}
}
