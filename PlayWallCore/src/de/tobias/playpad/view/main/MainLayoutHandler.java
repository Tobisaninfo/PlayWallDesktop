package de.tobias.playpad.view.main;

import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.main.MenuToolbarViewController;

/**
 * Schnittstelle für das Handle einer neunen Toolbar. Diese Methode wird vom MainView automatisch bei einem neuen Layout aufgeführt.
 * 
 * @author tobias
 * 
 * @since 5.1.0
 *
 * @see IMainViewController#performLayoutDependendAction(MainLayoutHandler)
 * @see MainLayoutFactory
 */
@FunctionalInterface
public interface MainLayoutHandler {

	/**
	 * Listener Methode für den Wechsel der Toolbar.
	 * 
	 * @param oldToolbar
	 *            Alte Toolbar
	 * @param newToolbar
	 *            Neue Toolbar
	 */
	void handle(MenuToolbarViewController oldToolbar, MenuToolbarViewController newToolbar);

}
