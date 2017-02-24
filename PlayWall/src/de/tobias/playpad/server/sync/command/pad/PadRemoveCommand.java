package de.tobias.playpad.server.sync.command.pad;

import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.command.Commands;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.command.Command;

/**
 * Created by tobias on 19.02.17.
 */
public class PadRemoveCommand implements Command {
	@Override
	public void execute(Object data) {
		if (data instanceof Pad) {
			Pad pad = (Pad) data;
			JsonObject json = new JsonObject();
			json.addProperty(PropertyDef.CMD, Commands.PAD_REMOVE);

			// Add Data
			json.addProperty(PropertyDef.ID, pad.getUuid().toString());

			Server server = PlayPadPlugin.getServerHandler().getServer();
			server.push(json);
		} else {
			throw new IllegalArgumentException("Argument mismatch");
		}
	}
}
