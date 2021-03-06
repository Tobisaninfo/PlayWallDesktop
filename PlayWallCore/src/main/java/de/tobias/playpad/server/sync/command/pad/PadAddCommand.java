package de.tobias.playpad.server.sync.command.pad;

import com.google.gson.JsonObject;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.command.Command;
import de.tobias.playpad.server.sync.command.Commands;

/**
 * Created by tobias on 19.02.17.
 */
public class PadAddCommand implements Command {

	@Override
	public JsonObject execute(Object data) {
		if (data instanceof Pad) {
			Pad pad = (Pad) data;

			JsonObject json = new JsonObject();
			json.addProperty(PropertyDef.CMD, Commands.PAD_ADD);

			// Add Data
			json.addProperty(PropertyDef.ID, pad.getUuid().toString());
			json.addProperty(PropertyDef.PAD_PAGE_REF, pad.getPage().getId().toString());
			json.addProperty(PropertyDef.PAD_POSITION, pad.getPosition());
			json.addProperty(PropertyDef.PAD_NAME, pad.getName());
			json.addProperty(PropertyDef.PAD_CONTENT_TYPE, pad.getContentType());

			return json;
		} else {
			throw new IllegalArgumentException("Argument mismatch");
		}
	}
}
