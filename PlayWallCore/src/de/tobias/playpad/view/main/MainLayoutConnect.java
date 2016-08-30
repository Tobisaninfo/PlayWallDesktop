package de.tobias.playpad.view.main;

import de.tobias.playpad.pad.view.IPadView;
import de.tobias.playpad.pad.viewcontroller.IPadViewController;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.main.MenuToolbarViewController;

/**
 * Dieses Interface beschreibt die Bestandteile des Layouts (GUI Elemente) des Main Views.
 * 
 * @author tobias
 *
 * @since 5.1.0
 */
public interface MainLayoutConnect {

	/**
	 * Gibt den Unique Identifier zurück.
	 * 
	 * @return ID
	 */
	public String getType();

	/**
	 * Gibt einen lesbaren (am besten Localized) Namen für den Nutzer zurück.
	 * 
	 * @return Name
	 */
	public String name();

	/**
	 * Erstellt einen ViewController für die Menu/Toolbar Fläche.
	 * 
	 * @param mainViewRef
	 *            Refernz auf den Main View
	 * 
	 * @return Neuer ViewController mit View
	 */
	public MenuToolbarViewController createMenuToolbar(IMainViewController mainViewRef);

	/**
	 * Erstellt ein neues Pad mit einem ViewController
	 * 
	 * @return Pad
	 * 
	 * @see IPadView notwendige Methoden für ein Pad
	 * @see IPadViewController ViewController zum Pad
	 */
	public IPadView createPadView();

	/**
	 * Recycelt eine PadView, damit nicht immer neue erstellt werden müssen.
	 * 
	 * @param padView
	 *            alte PadView
	 */
	public void recyclePadView(IPadView padView);

	/**
	 * Gibt das Layout sepzifische Stylesheet zurück.
	 * 
	 * @return path in jar
	 */
	public String getStylesheet();

}
