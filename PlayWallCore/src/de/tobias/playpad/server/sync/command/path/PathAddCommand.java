package de.tobias.playpad.server.sync.command.path;

import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.MediaPath;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.Commands;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.ServerUtils;

import java.nio.file.Path;

/**
 * Created by tobias on 19.02.17.
 */
public class PathAddCommand {

	public static void addPath(MediaPath mediaPath) {
		if (ServerUtils.isNewValueComingFromServer()) {
			return;
		}

		final JsonObject json = new JsonObject();
		json.addProperty(PropertyDef.CMD, Commands.PATH_ADD);

		// Add Data
		json.addProperty(PropertyDef.ID, mediaPath.getId().toString());
		json.addProperty(PropertyDef.PATH_PAD, mediaPath.getPad().getUuid().toString());

		Path path = mediaPath.getPath();
		if (path != null) {
			json.addProperty(PropertyDef.PATH_PATH, path.toString());
		}

		Server server = PlayPadPlugin.getServerHandler().getServer();
		server.push(json);
	}

}
