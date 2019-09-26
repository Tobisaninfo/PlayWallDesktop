package de.tobias.playpad.server.sync.command;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.project.ref.ProjectReference;

/**
 * Created by tobias on 24.02.17.
 */
public class CommandManager {

	private static CommandExecutor executor;

	static {
		executor = PlayPadPlugin.getCommandExecutorHandler().getCommandExecutor();
	}

	private CommandManager() {
	}

	public static void register(String name, Command command) {
		executor.register(name, command);
	}

	public static void execute(String command) {
		executor.execute(command);
	}

	public static void execute(String name, ProjectReference projectReference, Object data) {
		executor.execute(name, projectReference, data);
	}
}
