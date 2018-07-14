package de.tobias.playpad.server.sync.conflict;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.profile.ProfileNotFoundException;
import de.tobias.playpad.project.ProjectNotFoundException;
import de.tobias.playpad.project.ProjectReader;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.command.CommandExecutor;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tobias on 01.03.17.
 */
public class ConflictSolverImpl implements ConflictSolver {

	@Override
	public void solveConflict(CommandExecutor commandExecutor, ProjectReference project, ConflictStrategyType type) throws ProjectNotFoundException, ProfileNotFoundException, DocumentException, IOException, ProjectReader.ProjectReaderDelegate.ProfileAbortException {
		ConflictStrategy strategy = null;

		if (type == ConflictStrategyType.ROLLBACK) {
			strategy = new RollbackStrategy();
		} else if (type == ConflictStrategyType.UPGRADE) {
			strategy = new UpgradeStrategy();
		}

		if (strategy != null) {
			IMainViewController mainViewController = PlayPadMain.getProgramInstance().getMainViewController();
			Server server = PlayPadPlugin.getServerHandler().getServer();
			strategy.solveConflict(mainViewController, project, server, commandExecutor);
		}
	}

	@Override
	public ConflictType checkConflict(CommandExecutor executor, ProjectReference reference) throws IOException {
		Server server = PlayPadPlugin.getServerHandler().getServer();
		Version serverVersion = server.getLastServerModification(reference);
		Version localVersion = reference.getVersion();

		if (serverVersion.getTimestamp() > localVersion.getTimestamp()) {
			return ConflictType.SERVER_CHANGES;
		} else if (serverVersion.getTimestamp() < localVersion.getTimestamp()) {
			if (!serverVersion.getSessionName().equals(localVersion.getSessionName())) {
				return ConflictType.BOTH_CHANGES;
			} else {
				return ConflictType.LOCAL_CHANGES;
			}
		}
		return ConflictType.NON;
	}

	@Override
	public List<Version> getVersions(ProjectReference reference) throws IOException {
		Server server = PlayPadPlugin.getServerHandler().getServer();

		List<Version> versions = new ArrayList<>();
		versions.add(server.getLastServerModification(reference));
		versions.add(reference.getVersion());
		return versions;
	}
}
