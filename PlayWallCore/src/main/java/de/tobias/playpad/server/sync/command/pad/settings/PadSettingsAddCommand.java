package de.tobias.playpad.server.sync.command.pad.settings;

import com.google.gson.JsonObject;
import de.tobias.playpad.pad.PadSettings;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.command.Command;
import de.tobias.playpad.server.sync.command.Commands;

import java.util.UUID;

public class PadSettingsAddCommand implements Command {

	@Override
	public JsonObject execute(Object data) {
		if (data instanceof PadSettings) {
			PadSettings settings = (PadSettings) data;

			JsonObject json = new JsonObject();
			json.addProperty(PropertyDef.CMD, Commands.PAD_SETTINGS_ADD);

			// Add Data
			json.addProperty(PropertyDef.ID, settings.getId().toString());
			UUID padId = settings.getPad().getUuid();
			json.addProperty(PropertyDef.PAD_SETTINGS_PAD_ID, padId.toString());
			json.addProperty(PropertyDef.PAD_SETTINGS_VOLUME, settings.getVolume());
			json.addProperty(PropertyDef.PAD_SETTINGS_LOOP, settings.isLoop());
			if (settings.isCustomTimeMode()) {
				json.addProperty(PropertyDef.PAD_SETTINGS_TIME_MODE, settings.getTimeMode().name());
			}
			if (settings.isCustomWarning()) {
				json.addProperty(PropertyDef.PAD_SETTINGS_WARNING, settings.getWarning().toMillis());
			}

			return json;
		} else {
			throw new IllegalArgumentException("Argument mismatch");
		}
	}
}
