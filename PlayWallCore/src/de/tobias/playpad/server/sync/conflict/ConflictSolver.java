package de.tobias.playpad.server.sync.conflict;

import de.tobias.playpad.profile.ProfileNotFoundException;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectNotFoundException;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.command.CommandExecutor;
import de.tobias.playpad.server.sync.command.CommandStore;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.util.List;

/**
 * Created by tobias on 01.03.17.
 */
public interface ConflictSolver {

	/**
	 * Perform the strategy to solve the problem.
	 *
	 * @param executor command executor
	 * @param project  project
	 * @param type     strategy type
	 */
	void solveConflict(CommandExecutor executor, ProjectReference project, ConflictStrategyType type) throws ProjectNotFoundException, ProfileNotFoundException, DocumentException, IOException;

	/**
	 * Check if a project has a sync conflict.
	 *
	 * @param executor  command executor
	 * @param reference project reference
	 * @return conflict type
	 */
	ConflictType checkConflict(CommandExecutor executor, ProjectReference reference) throws IOException;

	/**
	 * Get all the versions of a project.
	 *
	 * @param reference project reference
	 * @return list of versions
	 */
	List<Version> getVersions(ProjectReference reference) throws IOException;
}
