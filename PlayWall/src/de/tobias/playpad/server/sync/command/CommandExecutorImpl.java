package de.tobias.playpad.server.sync.command;

import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.ServerUtils;

import java.util.*;

/**
 * Created by tobias on 01.03.17.
 */
public class CommandExecutorImpl implements CommandExecutor, CommandStore {

	private Map<String, Command> commandMap = new HashMap<>();
	private Map<UUID, List<JsonObject>> storedCommands = new HashMap<>();

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
		boolean send = server.push(sendData);

		// Store local if server is disconnected
		if (!send) {
			UUID uuid = projectReference.getUuid();
			if (!storedCommands.containsKey(uuid)) {
				storedCommands.put(uuid, new ArrayList<>());
			}
			storedCommands.get(uuid).add(sendData);
		}
	}

	@Override
	public void setStoredCommands(String id, List<JsonObject> commands) {
		UUID uuid = UUID.fromString(id);
		storedCommands.put(uuid, commands);
	}

	@Override
	public Map<UUID, List<JsonObject>> getStoredCommands() {
		return storedCommands;
	}

	@Override
	public List<JsonObject> getStoredCommands(UUID uuid) {
		return storedCommands.get(uuid);
	}

	@Override
	public void clearStoredCommands(UUID uuid) {
		storedCommands.remove(uuid);
	}
}
