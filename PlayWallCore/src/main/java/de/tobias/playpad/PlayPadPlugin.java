package de.tobias.playpad;

import de.tobias.playpad.design.Styleable;
import de.tobias.playpad.server.ServerHandler;
import de.tobias.playpad.server.sync.command.CommandExecutorHandler;

public final class PlayPadPlugin {

	private static PlayPad implementation;
	private static Styleable modernDesignHandler;
	private static RegistryCollection registryCollection;
	private static ServerHandler serverHandler;
	private static CommandExecutorHandler commandExecutorHandler;

	public static PlayPad getImplementation() {
		return implementation;
	}

	static void setImplementation(PlayPad playPadMain) {
		implementation = playPadMain;
	}

	/**
	 * Gibt die Implementierung für die Registries
	 *
	 * @return Registry Collection Impl
	 */
	public static RegistryCollection getRegistryCollection() {
		return registryCollection;
	}

	static void setRegistryCollection(RegistryCollection registryCollection) {
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
