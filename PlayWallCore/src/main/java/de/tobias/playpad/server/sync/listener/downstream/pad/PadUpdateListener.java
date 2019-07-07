package de.tobias.playpad.server.sync.listener.downstream.pad;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.listener.ServerListener;
import javafx.application.Platform;

import java.util.UUID;

/**
 * Handles incoming changes on project from server and set the right property.
 */
public class PadUpdateListener implements ServerListener {

	@Override
	public void listen(JsonElement element) {
		if (element instanceof JsonObject) {
			JsonObject json = (JsonObject) element;

			UUID uuid = UUID.fromString(json.get(PropertyDef.ID).getAsString());

			Project project = PlayPadPlugin.getInstance().getCurrentProject();
			if (project != null) {
				Pad pad = project.getPad(uuid);
				if (pad != null) {
					String field = json.get(PropertyDef.FIELD).getAsString();
					switch (field) {
						case PropertyDef.PAD_NAME:
							String name = json.get(PropertyDef.VALUE).getAsString();
							Platform.runLater(() -> pad.setName(name));
							break;
						case PropertyDef.PAD_POSITION:
							int position = json.get(PropertyDef.VALUE).getAsInt();
							Platform.runLater(() -> pad.setPosition(position));
							break;
						case PropertyDef.PAD_CONTENT_TYPE:
							String contentType = json.get(PropertyDef.VALUE).getAsString();
							Platform.runLater(() -> pad.setContentType(contentType));
							break;
					}
				}
			}
		}
	}
}
