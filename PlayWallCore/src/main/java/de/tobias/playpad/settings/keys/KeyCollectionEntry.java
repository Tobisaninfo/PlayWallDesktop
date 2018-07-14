package de.tobias.playpad.settings.keys;

/**
 * Datenstruktur für die KeyCollection, zum speichern der Einträge.
 *
 * @author tobias
 * @since 6.0.0
 */
class KeyCollectionEntry {

	private final String name;
	private Key key;

	public KeyCollectionEntry(String name, Key key) {
		this.name = name;
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

}
