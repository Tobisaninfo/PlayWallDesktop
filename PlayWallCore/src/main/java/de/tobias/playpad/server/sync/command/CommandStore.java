package de.tobias.playpad.server.sync.command;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by tobias on 01.03.17.
 */
public interface CommandStore {
	void setStoredCommands(String id, List<JsonObject> commands);

	Map<UUID, List<JsonObject>> getStoredCommands();

	List<JsonObject> getStoredCommands(UUID uuid);

	void clearStoredCommands(UUID uuid);

	long getLastModification(UUID uuid);
}
