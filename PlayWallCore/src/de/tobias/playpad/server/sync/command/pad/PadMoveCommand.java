package de.tobias.playpad.server.sync.command.pad;

import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.Commands;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.ServerUtils;

/**
 * Created by tobias on 19.02.17.
 */
public class PadMoveCommand {

	public static void moved() {
		if (ServerUtils.isNewValueComingFromServer()) {
			return;
		}
		JsonObject json = new JsonObject();
		json.addProperty(PropertyDef.CMD, Commands.PAD_MOVE);

		Server server = PlayPadPlugin.getServerHandler().getServer();
		server.push(json);
	}

}
