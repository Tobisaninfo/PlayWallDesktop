package de.tobias.playpad.server.sync.conflict;

import com.google.gson.JsonObject;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectNotFoundException;
import de.tobias.playpad.project.ref.ProjectReferenceManager;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.command.CommandExecutor;
import de.tobias.playpad.server.sync.command.CommandStore;
import de.tobias.playpad.settings.ProfileNotFoundException;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Created by tobias on 01.03.17.
 */
public class UpgradeStrategy implements ConflictStrategy {

	@Override
	public void solveConflict(IMainViewController mainView, Project project, Server server, CommandExecutor executor) throws ProjectNotFoundException, ProfileNotFoundException, DocumentException, IOException {
		if (executor instanceof CommandStore) {
			UUID uuid = project.getProjectReference().getUuid();

			// Send Changes to server
			List<JsonObject> commands = ((CommandStore) executor).getStoredCommands(uuid);
			commands.forEach(server::push);
			((CommandStore) executor).clearStoredCommands(uuid);

			// Reload Project
			Project newProject = ProjectReferenceManager.loadProject(project.getProjectReference(), null);
			mainView.openProject(newProject);
		}
	}
}
