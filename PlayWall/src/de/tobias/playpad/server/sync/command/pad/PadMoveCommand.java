package de.tobias.playpad.server.sync.command.pad;

import com.google.gson.JsonObject;
import de.tobias.playpad.server.sync.command.Commands;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.command.Command;

/**
 * Created by tobias on 19.02.17.
 */
public class PadMoveCommand implements Command {
	@Override
	public JsonObject execute(Object data) {
		JsonObject json = new JsonObject();
		json.addProperty(PropertyDef.CMD, Commands.PAD_MOVE);

		return json;
	}
}