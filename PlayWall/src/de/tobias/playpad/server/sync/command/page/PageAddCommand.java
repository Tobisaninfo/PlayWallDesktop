package de.tobias.playpad.server.sync.command.page;

import com.google.gson.JsonObject;
import de.tobias.playpad.project.page.Page;
import de.tobias.playpad.server.sync.command.Commands;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.command.Command;

import java.util.UUID;

/**
 * Created by tobias on 19.02.17.
 */
public class PageAddCommand implements Command {

	@Override
	public JsonObject execute(Object data) {
		if (data instanceof Page) {
			Page page = (Page) data;
			JsonObject json = new JsonObject();
			json.addProperty(PropertyDef.CMD, Commands.PAGE_ADD);

			// Add Data
			json.addProperty(PropertyDef.ID, page.getId().toString());
			UUID projectUuid = page.getProject().getProjectReference().getUuid();
			json.addProperty(PropertyDef.PAGE_PROJECT_REF, projectUuid.toString());
			json.addProperty(PropertyDef.PAGE_POSITION, page.getPosition());
			json.addProperty(PropertyDef.PAGE_NAME, page.getName());

			return json;
		} else {
			throw new IllegalArgumentException("Argument mismatch");
		}
	}
}
