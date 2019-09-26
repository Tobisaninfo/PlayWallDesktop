package de.tobias.playpad.server.sync.conflict;

import de.tobias.playpad.profile.ProfileNotFoundException;
import de.tobias.playpad.project.ProjectNotFoundException;
import de.tobias.playpad.project.ProjectReader;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.server.sync.command.CommandExecutor;
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
	 * @throws IOException                                               server communication error
	 * @throws ProjectNotFoundException                                  Project to solve error not found
	 * @throws ProfileNotFoundException                                  Profile of project not found
	 * @throws DocumentException                                         XML Error
	 * @throws ProjectReader.ProjectReaderDelegate.ProfileAbortException Profile Choose aborted
	 */
	void solveConflict(CommandExecutor executor, ProjectReference project, ConflictStrategyType type) throws ProjectNotFoundException, ProfileNotFoundException, DocumentException, IOException, ProjectReader.ProjectReaderDelegate.ProfileAbortException;

	/**
	 * Check if a project has a sync conflict.
	 *
	 * @param executor  command executor
	 * @param reference project reference
	 * @return conflict type
	 * @throws IOException server communication error
	 */
	ConflictType checkConflict(CommandExecutor executor, ProjectReference reference) throws IOException;

	/**
	 * Get all the versions of a project.
	 *
	 * @param reference project reference
	 * @return list of versions
	 * @throws IOException server communication error
	 */
	List<Version> getVersions(ProjectReference reference) throws IOException;
}
