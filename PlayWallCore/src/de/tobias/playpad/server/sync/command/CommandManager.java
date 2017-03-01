package de.tobias.playpad.server.sync.command;

import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPad;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.ServerUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tobias on 24.02.17.
 */
public class CommandManager {

	private static Map<String, Command> commandMap = new HashMap<>();

	public static void register(String name, Command command) {
		commandMap.put(name, command);
	}

	public static void execute(String command) {
		if (ServerUtils.isNewValueComingFromServer()) {
			return;
		}
		commandMap.get(command).execute(null);
	}

	public static void execute(String name, ProjectReference projectReference, Object data) {
		if (ServerUtils.isNewValueComingFromServer()) {
			return;
		}
		Command command = commandMap.get(name);
		JsonObject sendData = command.execute(data);

		sendData.addProperty("pro-ref", projectReference.getUuid().toString());

		Server server = PlayPadPlugin.getServerHandler().getServer();
		server.push(sendData);
	}
}
