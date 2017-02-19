package de.tobias.playpad.server.sync.command.project;

import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.command.Commands;

/**
 * Created by tobias on 19.02.17.
 */
public class ProjectAdd {

	public static void addProject(Project project) {
		JsonObject json = new JsonObject();
		json.addProperty("cmd", Commands.PROJECT_ADD);

		// Add Data
		json.addProperty("id", project.getProjectReference().getUuid().toString());
		json.addProperty("name", project.getProjectReference().getName());

		Server server = PlayPadPlugin.getServerHandler().getServer();
		server.push(json);
	}

}
