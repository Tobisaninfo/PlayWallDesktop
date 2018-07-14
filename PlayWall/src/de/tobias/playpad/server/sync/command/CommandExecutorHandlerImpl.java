package de.tobias.playpad.server.sync.command;

/**
 * Created by tobias on 01.03.17.
 */
public class CommandExecutorHandlerImpl implements CommandExecutorHandler {
	private CommandExecutor executor;

	public CommandExecutorHandlerImpl() {
		executor = new CommandExecutorImpl();
	}

	@Override
	public CommandExecutor getCommandExecutor() {
		return executor;
	}
}
