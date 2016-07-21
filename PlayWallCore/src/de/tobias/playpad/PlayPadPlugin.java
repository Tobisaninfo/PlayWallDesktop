package de.tobias.playpad;

public final class PlayPadPlugin {

	private static PlayPad implementation;
	private static RegistryCollection registryCollection;

	public static PlayPad getImplementation() {
		return implementation;
	}

	protected static void setImplementation(PlayPad playPadMain) {
		implementation = playPadMain;
	}

	/**
	 * Gibt die Implementierung für die Registries
	 * 
	 * @return
	 */
	public static RegistryCollection getRegistryCollection() {
		return registryCollection;
	}

	protected static void setRegistryCollection(RegistryCollection registryCollection) {
		PlayPadPlugin.registryCollection = registryCollection;
	}
}
