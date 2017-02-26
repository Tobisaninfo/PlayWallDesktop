package de.tobias.playpad.server.sync.command.project;

import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.command.Commands;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.command.Command;

/**
 * Created by tobias on 19.02.17.
 */
public class ProjectAddCommand implements Command {

	@Override
	public void execute(Object data) {
		if (data instanceof ProjectReference) {
			ProjectReference project = (ProjectReference) data;
			JsonObject json = new JsonObject();
			json.addProperty(PropertyDef.CMD, Commands.PROJECT_ADD);

			// Add Data
			json.addProperty(PropertyDef.ID, project.getUuid().toString());
			json.addProperty(PropertyDef.PROJECT_NAME, project.getName());

			Server server = PlayPadPlugin.getServerHandler().getServer();
			server.push(json);
		} else {
			throw new IllegalArgumentException("Argument mismatch");
		}
	}
}
