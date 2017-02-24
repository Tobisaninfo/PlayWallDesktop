package de.tobias.playpad.server.sync.command.path;

import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.mediapath.MediaPath;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.command.Commands;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.command.Command;

/**
 * Created by tobias on 19.02.17.
 */
public class PathRemoveCommand implements Command {
	@Override
	public void execute(Object data) {
		if (data instanceof MediaPath) {
			MediaPath path = (MediaPath) data;
			JsonObject json = new JsonObject();
			json.addProperty(PropertyDef.CMD, Commands.PATH_REMOVE);

			// Add Data
			json.addProperty(PropertyDef.ID, path.getId().toString());

			Server server = PlayPadPlugin.getServerHandler().getServer();
			server.push(json);
		} else {
			throw new IllegalArgumentException("Argument mismatch");
		}
	}
}
