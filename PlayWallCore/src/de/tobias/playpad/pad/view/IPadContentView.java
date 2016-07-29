package de.tobias.playpad.pad.view;

import javafx.scene.Node;

// TODO Rename to ContentView
/**
 * Schnittstelle für die Vorschau des PadContent eines Pads.
 * 
 * @author tobias
 *
 * @since 5.1.0
 */
public interface IPadContentView {

	/**
	 * Gibt das GUI Element zurück.
	 * 
	 * @return GUI Element
	 */
	public Node getNode();

	@Deprecated
	public void unconnect();

	/**
	 * Deinitialisiert die View. Hier können mögliche Bindings und Listener entfernt werden.
	 */
	public default void deinit() {
		// TODO Remove the default after remove unconnect from interface
	}
}
