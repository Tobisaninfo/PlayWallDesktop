package de.tobias.playpad.server.sync.conflict;

import de.tobias.playpad.profile.ProfileNotFoundException;
import de.tobias.playpad.project.ProjectNotFoundException;
import de.tobias.playpad.project.ProjectReader;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.command.CommandExecutor;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import org.dom4j.DocumentException;

import java.io.IOException;

/**
 * Created by tobias on 01.03.17.
 */
public interface ConflictStrategy {
	void solveConflict(IMainViewController mainView, ProjectReference project, Server server, CommandExecutor executor) throws ProjectNotFoundException, ProfileNotFoundException, DocumentException, IOException, ProjectReader.ProjectReaderDelegate.ProfileAbortException;
}
