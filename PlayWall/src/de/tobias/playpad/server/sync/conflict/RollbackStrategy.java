package de.tobias.playpad.server.sync.conflict;

import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectNotFoundException;
import de.tobias.playpad.project.ref.ProjectReferenceManager;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.command.CommandExecutor;
import de.tobias.playpad.settings.ProfileNotFoundException;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import org.dom4j.DocumentException;

import java.io.IOException;

/**
 * Created by tobias on 01.03.17.
 */
public class RollbackStrategy implements ConflictStrategy {

	@Override
	public void solveConflict(IMainViewController mainView, Project project, Server server, CommandExecutor executor) throws ProjectNotFoundException, ProfileNotFoundException, DocumentException, IOException {
		// Reload Project
		Project newProject = ProjectReferenceManager.loadProject(project.getProjectReference(), null);
		mainView.openProject(newProject);
	}
}
