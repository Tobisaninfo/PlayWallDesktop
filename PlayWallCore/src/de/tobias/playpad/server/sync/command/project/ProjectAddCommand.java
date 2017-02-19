package de.tobias.playpad.server.sync.command.project;

import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.Commands;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.ServerUtils;

/**
 * Created by tobias on 19.02.17.
 */
public class ProjectAddCommand {

	public static void addProject(Project project) {
		if (ServerUtils.isNewValueComingFromServer()) {
			return;
		}
		JsonObject json = new JsonObject();
		json.addProperty(PropertyDef.CMD, Commands.PROJECT_ADD);

		// Add Data
		json.addProperty(PropertyDef.ID, project.getProjectReference().getUuid().toString());
		json.addProperty(PropertyDef.PROJECT_NAME, project.getProjectReference().getName());

		Server server = PlayPadPlugin.getServerHandler().getServer();
		server.push(json);
	}

}
