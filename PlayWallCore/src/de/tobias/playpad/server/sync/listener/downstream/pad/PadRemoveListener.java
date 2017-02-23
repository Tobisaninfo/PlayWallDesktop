package de.tobias.playpad.server.sync.listener.downstream.pad;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.page.Page;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.listener.downstream.ServerListener;

import java.util.UUID;

/**
 * Created by tobias on 19.02.17.
 */
public class PadRemoveListener implements ServerListener {
	@Override
	public void listen(JsonElement element) {
		if (element instanceof JsonObject) {
			JsonObject json = (JsonObject) element;

			UUID uuid = UUID.fromString(json.get(PropertyDef.ID).getAsString());
			Project project = PlayPadPlugin.getImplementation().getCurrentProject();
			if (project != null) {
				Pad pad = project.getPad(uuid);
				if (pad != null) {
					project.removePad(pad.getPadIndex());
				}
			}
		}
	}
}
