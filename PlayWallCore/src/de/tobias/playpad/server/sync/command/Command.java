package de.tobias.playpad.server.sync.command;

import com.google.gson.JsonObject;

/**
 * Created by tobias on 24.02.17.
 */
public interface Command {

	/**
	 * Execute a command.
	 *
	 * @param data optional data parameter
	 */
	JsonObject execute(Object data);
}
