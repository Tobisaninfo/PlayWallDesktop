package de.tobias.playpad.server.sync.conflict;

import de.tobias.playpad.project.Project;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.command.CommandStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tobias on 01.03.17.
 */
public class ConflictSolverImpl implements ConflictSolver {

	@Override
	public void solveConflict(ConflictStrategyType type) {

	}

	@Override
	public List<ConflictStrategyType> possibleTypes(Project project, Server server, CommandStore commandStore) {
		List<ConflictStrategyType> types = new ArrayList<>();



		return types;
	}
}
