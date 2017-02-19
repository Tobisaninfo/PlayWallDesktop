package de.tobias.playpad;

import de.tobias.playpad.server.ServerHandler;

public final class PlayPadPlugin {

	private static PlayPad implementation;
	private static RegistryCollection registryCollection;
	private static ServerHandler serverHandler;

	public static PlayPad getImplementation() {
		return implementation;
	}

	protected static void setImplementation(PlayPad playPadMain) {
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
}
