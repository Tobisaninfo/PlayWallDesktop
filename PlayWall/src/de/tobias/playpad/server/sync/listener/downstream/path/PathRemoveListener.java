package de.tobias.playpad.server.sync.listener.downstream.path;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.MediaPath;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.listener.ServerListener;

import java.util.UUID;

/**
 * Created by tobias on 19.02.17.
 */
public class PathRemoveListener implements ServerListener {
	@Override
	public void listen(JsonElement element) {
		if (element instanceof JsonObject) {
			JsonObject json = (JsonObject) element;

			UUID uuid = UUID.fromString(json.get(PropertyDef.ID).getAsString());
			Project project = PlayPadPlugin.getImplementation().getCurrentProject();
			if (project != null) {
				MediaPath path = project.getMediaPath(uuid);
				if (path != null) {
					path.getPad().removePath(path);
				}
			}
		}
	}
}
