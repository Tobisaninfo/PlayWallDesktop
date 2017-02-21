package de.tobias.playpad.server.sync.command.pad;

import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.Commands;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.ServerUtils;

import java.util.UUID;

/**
 * Created by tobias on 19.02.17.
 */
public class PadAddCommand {

	public static void addPad(Pad pad) {
		if (ServerUtils.isNewValueComingFromServer()) {
			return;
		}

		JsonObject json = new JsonObject();
		json.addProperty(PropertyDef.CMD, Commands.PAD_ADD);

		// Add Data
		json.addProperty(PropertyDef.ID, pad.getUuid().toString());
		json.addProperty(PropertyDef.PAD_PAGE, pad.getPage().getId().toString());
		json.addProperty(PropertyDef.PAD_POSITION, pad.getPosition());
		json.addProperty(PropertyDef.PAD_NAME, pad.getName());

		Server server = PlayPadPlugin.getServerHandler().getServer();
		server.push(json);
	}

}
