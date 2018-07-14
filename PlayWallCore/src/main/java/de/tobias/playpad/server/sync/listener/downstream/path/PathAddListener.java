package de.tobias.playpad.server.sync.listener.downstream.path;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.mediapath.MediaPath;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.listener.ServerListener;
import javafx.application.Platform;

import java.util.UUID;

/**
 * Created by tobias on 19.02.17.
 */
public class PathAddListener implements ServerListener {
	@Override
	public void listen(JsonElement element) {
		if (element instanceof JsonObject) {
			JsonObject json = (JsonObject) element;

			UUID uuid = UUID.fromString(json.get(PropertyDef.ID).getAsString());
			UUID pad_id = UUID.fromString(json.get(PropertyDef.PATH_PAD_REF).getAsString());
			String filename = json.get(PropertyDef.PATH_FILENAME).getAsString();

			Project project = PlayPadPlugin.getImplementation().getCurrentProject();
			if (project != null) {
				Pad pad = project.getPad(pad_id);
				if (pad != null) {
					MediaPath mediaPath = new MediaPath(uuid, filename, pad);
					Platform.runLater(() -> pad.addPath(mediaPath));
				}
			}
		}
	}
}
