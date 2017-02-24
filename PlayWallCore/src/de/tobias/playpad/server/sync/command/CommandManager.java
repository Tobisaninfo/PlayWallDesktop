package de.tobias.playpad.server.sync.command;

import de.tobias.playpad.server.sync.ServerUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tobias on 24.02.17.
 */
public class CommandManager {

	private static Map<String, Command> commands = new HashMap<>();

	public static void register(String name, Command command) {
		commands.put(name, command);
	}

	public static void execute(String command) {
		if (ServerUtils.isNewValueComingFromServer()) {
			return;
		}
		commands.get(command).execute(null);
	}

	public static void execute(String command, Object data) {
		if (ServerUtils.isNewValueComingFromServer()) {
			return;
		}
		commands.get(command).execute(data);
	}
}
