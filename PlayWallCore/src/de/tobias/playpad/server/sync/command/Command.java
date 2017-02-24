package de.tobias.playpad.server.sync.command;

/**
 * Created by tobias on 24.02.17.
 */
public interface Command {

	/**
	 * Execute a command.
	 *
	 * @param data optional data parameter
	 */
	void execute(Object data);
}
