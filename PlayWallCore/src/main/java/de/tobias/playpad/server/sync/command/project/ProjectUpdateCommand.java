package de.tobias.playpad.server.sync.command.project;

import com.google.gson.JsonObject;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.command.Change;
import de.tobias.playpad.server.sync.command.Command;
import de.tobias.playpad.server.sync.command.Commands;

/**
 * Created by tobias on 01.03.17.
 */
public class ProjectUpdateCommand implements Command {
	@Override
	public JsonObject execute(Object data) {
		if (data instanceof Change) {
			Change change = (Change) data;
			if (change.getRef() instanceof Project) {
				Project page = (Project) change.getRef();

				JsonObject json = new JsonObject();
				json.addProperty(PropertyDef.ID, page.getProjectReference().getUuid().toString());
				json.addProperty(PropertyDef.CMD, Commands.PROJECT_UPDATE);

				json.addProperty(PropertyDef.FIELD, change.getName());
				json.addProperty(PropertyDef.VALUE, change.getValue().toString());

				return json;
			}
		}
		throw new IllegalArgumentException("Argument mismatch");
	}
}
