package de.tobias.playpad.view.main;

import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.main.MenuToolbarViewController;

/**
 * Schnittstelle für das Handle einer neunen Toolbar. Diese Methode wird vom MainView automatisch bei einem neuen Layout aufgeführt.
 *
 * @author tobias
 * @see IMainViewController#performLayoutDependedAction(MainLayoutHandler)
 * @see MainLayoutFactory
 * @since 5.1.0
 */
@FunctionalInterface
public interface MainLayoutHandler {

	/**
	 * Listener Methode für den Wechsel der Toolbar.
	 *
	 * @param oldToolbar Alte Toolbar
	 * @param newToolbar Neue Toolbar
	 */
	void handle(MenuToolbarViewController oldToolbar, MenuToolbarViewController newToolbar);

}
