package de.tobias.playpad.server.sync.command.pad.settings.design;

import com.google.gson.JsonObject;
import de.tobias.playpad.design.modern.model.ModernCartDesign;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.command.Command;
import de.tobias.playpad.server.sync.command.Commands;

import java.util.UUID;

/**
 * Created by tobias on 19.02.17.
 */
public class DesignAddCommand implements Command {

	@Override
	public JsonObject execute(Object data) {
		if (data instanceof ModernCartDesign) {
			ModernCartDesign design = (ModernCartDesign) data;

			JsonObject json = new JsonObject();
			json.addProperty(PropertyDef.CMD, Commands.DESIGN_ADD);

			// Add Data
			json.addProperty(PropertyDef.ID, design.getId().toString());
			UUID padId = design.getPad().getPadSettings().getId();
			json.addProperty(PropertyDef.DESIGN_PAD_SETTINGS_REF, padId.toString());
			json.addProperty(PropertyDef.DESIGN_BACKGROUND_COLOR, design.getBackgroundColor().name());
			json.addProperty(PropertyDef.DESIGN_PLAY_COLOR, design.getPlayColor().name());

			return json;
		} else {
			throw new IllegalArgumentException("Argument mismatch");
		}
	}
}
