package de.tobias.playpad.server.sync.command.project;

import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.command.Commands;
import de.tobias.playpad.server.sync.listener.ServerUtils;

/**
 * Created by tobias on 19.02.17.
 */
public class ProjectRemoveCommand {

	public static void removeProject(ProjectReference reference) {
		if (ServerUtils.isNewValueComingFromServer()) {
			return;
		}
		JsonObject json = new JsonObject();
		json.addProperty("cmd", Commands.PROJECT_REMOVE);

		// Add Data
		json.addProperty("id", reference.getUuid().toString());

		Server server = PlayPadPlugin.getServerHandler().getServer();
		server.push(json);
	}

}
