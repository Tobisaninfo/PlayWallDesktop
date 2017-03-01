package de.tobias.playpad.server.sync.command.page;

import com.google.gson.JsonObject;
import de.tobias.playpad.project.page.Page;
import de.tobias.playpad.server.sync.command.Commands;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.command.Command;

/**
 * Created by tobias on 19.02.17.
 */
public class PageRemoveCommand implements Command {

	@Override
	public JsonObject execute(Object data) {
		if (data instanceof Page) {
			Page page = (Page) data;
			JsonObject json = new JsonObject();
			json.addProperty(PropertyDef.CMD, Commands.PAGE_REMOVE);

			// Add Data
			json.addProperty(PropertyDef.ID, page.getId().toString());

			return json;
		} else {
			throw new IllegalArgumentException("Argument mismatch");
		}
	}

}
