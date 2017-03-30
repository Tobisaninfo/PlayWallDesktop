package de.tobias.playpad.server.sync.conflict;

import de.tobias.playpad.profile.ProfileNotFoundException;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectNotFoundException;
import de.tobias.playpad.project.loader.ProjectLoader;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.command.CommandExecutor;
import de.tobias.playpad.server.sync.command.CommandStore;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by tobias on 01.03.17.
 */
public class RollbackStrategy implements ConflictStrategy {

	@Override
	public void solveConflict(IMainViewController mainView, ProjectReference project, Server server, CommandExecutor executor) throws ProjectNotFoundException, ProfileNotFoundException, DocumentException, IOException {
		if (executor instanceof CommandStore) {
			// Clear local changes
			UUID uuid = project.getUuid();
			((CommandStore) executor).clearStoredCommands(uuid);

			// Reload Project
			ProjectLoader loader = new ProjectLoader(project);
			Project newProject = loader.load();

			// TODO Check if project should be displayed
			mainView.openProject(newProject);
		}
	}
}
