package de.tobias.playpad.pad.view;

import de.tobias.playpad.pad.viewcontroller.IPadViewControllerV2;

/**
 * Zugriff auf eine PadView. Hier sind alle Methoden um mit der GUI für ein Pad zu agieren.
 * 
 * @author tobias
 * 
 * @since 5.1.0
 *
 */
public interface IPadViewV2 {

	/**
	 * Gibt die aktuelle Vorschau für den Content eines Pads zurück.
	 * 
	 * @return ContentView
	 */
	public IPadContentView getContentView();

	/**
	 * Setzt die Preview für den PadContent.
	 * 
	 * @param contentView
	 *            Vorschau
	 */
	public void setContentView(IPadContentView contentView);

	/**
	 * Gibt den zugehörigen ViewController zu einem Pad zurück.
	 * 
	 * @return ViewController des Pad
	 */
	public IPadViewControllerV2 getViewController();

	/**
	 * Schaltet den Design Modus für Drag And Drop ein.
	 * 
	 * @param enable
	 *            true eingeschaltet
	 */
	public void enableDragAndDropDesignMode(boolean enable);
}
