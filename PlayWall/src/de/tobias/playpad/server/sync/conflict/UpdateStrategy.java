package de.tobias.playpad.server.sync.conflict;

import com.google.gson.JsonObject;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.command.CommandExecutor;
import de.tobias.playpad.server.sync.command.CommandStore;
import de.tobias.playpad.viewcontroller.main.IMainViewController;

import java.util.List;
import java.util.UUID;

/**
 * Created by tobias on 01.03.17.
 */
public class UpdateStrategy implements ConflictStrategy {

	@Override
	public void solveConflict(IMainViewController mainView, Project project, Server server, CommandExecutor executor) {
		if (executor instanceof CommandStore) {
			UUID uuid = project.getProjectReference().getUuid();

			// Send Changes to server
			List<JsonObject> commands = ((CommandStore) executor).getStoredCommands(uuid);
			commands.forEach(server::push);
			((CommandStore) executor).clearStoredCommands(uuid);
		}
	}
}
