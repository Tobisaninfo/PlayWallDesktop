package de.tobias.playpad.server.sync.command;

import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.server.sync.conflict.Conflict;
import de.tobias.playpad.server.sync.conflict.ConflictSolver;
import javafx.beans.property.ListProperty;

/**
 * Created by tobias on 01.03.17.
 */
public interface CommandExecutor {

	void register(String name, Command command);

	void execute(String command);

	void execute(String name, ProjectReference projectReference, Object data);

	ConflictSolver getConflictSolver();

	ListProperty<Conflict> conflicts();
}
