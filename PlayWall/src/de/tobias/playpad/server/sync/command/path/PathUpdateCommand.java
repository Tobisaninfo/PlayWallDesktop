package de.tobias.playpad.server.sync.command.path;

import com.google.gson.JsonObject;
import de.tobias.playpad.pad.mediapath.MediaPath;
import de.tobias.playpad.project.page.Page;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.command.Change;
import de.tobias.playpad.server.sync.command.Command;
import de.tobias.playpad.server.sync.command.Commands;

/**
 * Created by tobias on 01.03.17.
 */
public class PathUpdateCommand implements Command {
	@Override
	public JsonObject execute(Object data) {
		if (data instanceof Change) {
			Change change = (Change) data;
			if (change.getRef() instanceof MediaPath) {
				MediaPath path = (MediaPath) change.getRef();

				JsonObject json = new JsonObject();
				json.addProperty(PropertyDef.ID, path.getId().toString());
				json.addProperty(PropertyDef.PATH_PAD_REF, path.getPad().getUuid().toString());
				json.addProperty(PropertyDef.CMD, Commands.PATH_UPDATE);

				json.addProperty(PropertyDef.FIELD, change.getName());
				json.addProperty(PropertyDef.VALUE, change.getValue().toString());

				return json;
			}
		}
		throw new IllegalArgumentException("Argument mismatch");
	}
}
