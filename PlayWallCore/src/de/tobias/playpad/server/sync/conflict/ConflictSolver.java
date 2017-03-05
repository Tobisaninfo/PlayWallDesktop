package de.tobias.playpad.server.sync.conflict;

import de.tobias.playpad.project.Project;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.command.CommandStore;

import java.util.List;

/**
 * Created by tobias on 01.03.17.
 */
public interface ConflictSolver {

	/**
	 * Perform the strategy to solve the problem.
	 *
	 * @param type strategy type
	 */
	void solveConflict(ConflictStrategyType type);

	/**
	 * Get a list of all possible strategies to solve the sync problem.
	 *
	 * @param project current project
	 * @param server  server implementation
	 * @return conflict strategies
	 */
	List<ConflictStrategyType> possibleTypes(Project project, Server server, CommandStore store);
}
