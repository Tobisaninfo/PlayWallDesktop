package de.tobias.playpad.server.sync.command.pad.settings.design;

import com.google.gson.JsonObject;
import de.tobias.playpad.design.modern.ModernCartDesign;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.command.Change;
import de.tobias.playpad.server.sync.command.Command;
import de.tobias.playpad.server.sync.command.Commands;

/**
 * Created by tobias on 01.03.17.
 */
public class DesignUpdateCommand implements Command {
	@Override
	public JsonObject execute(Object data) {
		if (data instanceof Change) {
			Change change = (Change) data;
			if (change.getRef() instanceof ModernCartDesign) {
				ModernCartDesign design = (ModernCartDesign) change.getRef();

				JsonObject json = new JsonObject();
				json.addProperty(PropertyDef.ID, design.getId().toString());
				json.addProperty(PropertyDef.DESIGN_PAD_SETTINGS_REF, design.getPad().getPadSettings().getId().toString());
				json.addProperty(PropertyDef.CMD, Commands.DESIGN_UPDATE);

				json.addProperty(PropertyDef.FIELD, change.getName());
				json.addProperty(PropertyDef.VALUE, change.getValue().toString());

				return json;
			}
		}
		throw new IllegalArgumentException("Argument mismatch");
	}
}
