package de.tobias.playpad.server.sync.command;

import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.ServerUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tobias on 01.03.17.
 */
public class CommandExecutorImpl implements CommandExecutor {

	private Map<String, Command> commandMap = new HashMap<>();

	@Override
	public void register(String name, Command command) {
		commandMap.put(name, command);
	}

	@Override
	public void execute(String name) {
		if (ServerUtils.isNewValueComingFromServer()) {
			return;
		}
		Command command = commandMap.get(name);
		JsonObject sendData = command.execute(null);

		Server server = PlayPadPlugin.getServerHandler().getServer();
		server.push(sendData);
	}

	@Override
	public void execute(String name, ProjectReference projectReference, Object data) {
		if (ServerUtils.isNewValueComingFromServer()) {
			return;
		}
		Command command = commandMap.get(name);
		JsonObject sendData = command.execute(data);

		Server server = PlayPadPlugin.getServerHandler().getServer();
		server.push(sendData);
	}
}
