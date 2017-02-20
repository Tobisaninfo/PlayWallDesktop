package de.tobias.playpad.server.sync.command.page;

import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.project.page.Page;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.Commands;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.ServerUtils;

/**
 * Created by tobias on 19.02.17.
 */
public class PageRemoveCommand {

	public static void removePage(Page page) {
		if (ServerUtils.isNewValueComingFromServer()) {
			return;
		}
		JsonObject json = new JsonObject();
		json.addProperty(PropertyDef.CMD, Commands.PAGE_REMOVE);

		// Add Data
		json.addProperty(PropertyDef.ID, page.getId().toString());

		Server server = PlayPadPlugin.getServerHandler().getServer();
		server.push(json);
	}

}
