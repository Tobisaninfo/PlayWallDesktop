package de.tobias.playpad.server.sync.command.page;

import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.project.page.Page;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.Commands;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.ServerUtils;

import java.util.UUID;

/**
 * Created by tobias on 19.02.17.
 */
public class PageAddCommand {

	public static void addPage(Page page) {
		if (ServerUtils.isNewValueComingFromServer()) {
			return;
		}
		JsonObject json = new JsonObject();
		json.addProperty(PropertyDef.CMD, Commands.PAGE_ADD);

		// Add Data
		json.addProperty(PropertyDef.ID, page.getId().toString());
		UUID projectUuid = page.getProject().getProjectReference().getUuid();
		json.addProperty(PropertyDef.PAGE_PROJECT, projectUuid.toString());
		json.addProperty(PropertyDef.PAGE_POSITION, page.getPosition());
		json.addProperty(PropertyDef.PAGE_NAME, page.getName());

		Server server = PlayPadPlugin.getServerHandler().getServer();
		server.push(json);
	}

}
