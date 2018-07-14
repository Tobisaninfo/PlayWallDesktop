package de.tobias.playpad.server.sync.command.pad;

import com.google.gson.JsonObject;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.command.Change;
import de.tobias.playpad.server.sync.command.Command;
import de.tobias.playpad.server.sync.command.Commands;

/**
 * Created by tobias on 01.03.17.
 */
public class PadUpdateCommand implements Command {
	@Override
	public JsonObject execute(Object data) {
		if (data instanceof Change) {
			Change change = (Change) data;
			if (change.getRef() instanceof Pad) {
				Pad pad = (Pad) change.getRef();

				JsonObject json = new JsonObject();
				json.addProperty(PropertyDef.ID, pad.getUuid().toString());
				json.addProperty(PropertyDef.PAD_PAGE_REF, pad.getPage().getId().toString());
				json.addProperty(PropertyDef.CMD, Commands.PAD_UPDATE);

				json.addProperty(PropertyDef.FIELD, change.getName());
				json.addProperty(PropertyDef.VALUE, change.getValue() != null ? change.getValue().toString() : null);

				return json;
			}
		}
		throw new IllegalArgumentException("Argument mismatch");
	}
}
