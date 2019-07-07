package de.tobias.playpad;

import de.tobias.playpad.design.Styleable;
import de.tobias.playpad.server.ServerHandler;
import de.tobias.playpad.server.sync.command.CommandExecutorHandler;

public final class PlayPadPlugin {

	private static PlayPad instance;
	private static Styleable modernDesignHandler;
	private static Registries registryCollection;
	private static ServerHandler serverHandler;
	private static CommandExecutorHandler commandExecutorHandler;

	private PlayPadPlugin() {
	}

	public static PlayPad getInstance() {
		return instance;
	}

	static void setInstance(PlayPad playPadMain) {
		instance = playPadMain;
	}

	/**
	 * Gibt die Implementierung für die Registries
	 *
	 * @return Registry Collection Impl
	 */
	public static Registries getRegistries() {
		return registryCollection;
	}

	static void setRegistryCollection(Registries registryCollection) {
		PlayPadPlugin.registryCollection = registryCollection;
	}

	public static ServerHandler getServerHandler() {
		return serverHandler;
	}

	static void setServerHandler(ServerHandler handler) {
		serverHandler = handler;
	}

	public static CommandExecutorHandler getCommandExecutorHandler() {
		return commandExecutorHandler;
	}

	static void setCommandExecutorHandler(CommandExecutorHandler handler) {
		commandExecutorHandler = handler;
	}

	public static Styleable styleable() {
		return modernDesignHandler;
	}

	static void setStyleable(Styleable modernDesignHandler) {
		PlayPadPlugin.modernDesignHandler = modernDesignHandler;
	}
}
