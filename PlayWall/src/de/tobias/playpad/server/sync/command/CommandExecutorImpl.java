package de.tobias.playpad.server.sync.command;

import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.project.ref.ProjectReferenceManager;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.ServerUtils;
import de.tobias.playpad.server.sync.conflict.Conflict;
import de.tobias.playpad.server.sync.conflict.ConflictSolver;
import de.tobias.playpad.server.sync.conflict.ConflictSolverImpl;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;

import java.util.*;

/**
 * Created by tobias on 01.03.17.
 */
public class CommandExecutorImpl implements CommandExecutor, CommandStore {

	private Map<String, Command> commandMap = new HashMap<>();
	private Map<UUID, List<JsonObject>> storedCommands = new HashMap<>();

	private ConflictSolver solver;
	private ListProperty<Conflict> conflicts;

	CommandExecutorImpl() {
		conflicts = new SimpleListProperty<>();
	}

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

		// Handle Last midication date
		long lastModified = System.currentTimeMillis();
		sendData.addProperty(PropertyDef.TIME, lastModified);
		sendData.addProperty(PropertyDef.PROJECT_REF, projectReference.getUuid().toString());
		projectReference.setLastModified(lastModified);

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

	@Override
	public long getLastModification(UUID uuid) {
		ProjectReference reference = ProjectReferenceManager.getProject(uuid);
		return reference != null ? reference.getLastModified() : -1;
	}

	@Override
	public ConflictSolver getConflictSolver() {
		if (solver == null) {
			solver = new ConflictSolverImpl();
		}
		return solver;
	}

	@Override
	public ListProperty<Conflict> conflicts() {
		return conflicts;
	}
}
