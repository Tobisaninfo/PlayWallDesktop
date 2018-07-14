package de.tobias.playpad.server.sync.command.pad.settings;

import com.google.gson.JsonObject;
import de.tobias.playpad.pad.PadSettings;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.command.Change;
import de.tobias.playpad.server.sync.command.Command;
import de.tobias.playpad.server.sync.command.Commands;

public class PadSettingsUpdateCommand implements Command {

	@Override
	public JsonObject execute(Object data) {
		if (data instanceof Change) {
			Change change = (Change) data;
			if (change.getRef() instanceof PadSettings) {
				PadSettings settings = (PadSettings) change.getRef();

				JsonObject json = new JsonObject();
				json.addProperty(PropertyDef.ID, settings.getId().toString());
				json.addProperty(PropertyDef.PAD_SETTINGS_PAD_ID, settings.getPad().getUuid().toString());
				json.addProperty(PropertyDef.CMD, Commands.PAD_SETTINGS_UPDATE);

				json.addProperty(PropertyDef.FIELD, change.getName());
				json.addProperty(PropertyDef.VALUE, change.getValue() != null ? change.getValue().toString() : null);

				return json;
			}
		}
		throw new IllegalArgumentException("Argument mismatch");
	}
}
