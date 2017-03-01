package de.tobias.playpad.server.sync.command;

import de.tobias.playpad.project.ref.ProjectReference;

/**
 * Created by tobias on 01.03.17.
 */
public interface CommandExecutor {

	void register(String name, Command command);

	void execute(String command);

	void execute(String name, ProjectReference projectReference, Object data);
}
