package de.tobias.playpad.server.sync.command.path;

import com.google.gson.JsonObject;
import de.tobias.playpad.pad.mediapath.MediaPath;
import de.tobias.playpad.server.sync.command.Commands;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.command.Command;

import java.nio.file.Path;

/**
 * Created by tobias on 19.02.17.
 */
public class PathAddCommand implements Command {

	@Override
	public JsonObject execute(Object data) {
		if (data instanceof MediaPath) {
			MediaPath mediaPath = (MediaPath) data;

			final JsonObject json = new JsonObject();
			json.addProperty(PropertyDef.CMD, Commands.PATH_ADD);

			// Add Data
			json.addProperty(PropertyDef.ID, mediaPath.getId().toString());
			json.addProperty(PropertyDef.PATH_PAD_REF, mediaPath.getPad().getUuid().toString());

			Path path = mediaPath.getPath();
			if (path != null) {
				json.addProperty(PropertyDef.PATH_PATH, path.toString());
			}

			return json;
		} else {
			throw new IllegalArgumentException("Argument mismatch");
		}
	}
}
