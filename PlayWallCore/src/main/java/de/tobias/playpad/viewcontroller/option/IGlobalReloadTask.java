package de.tobias.playpad.viewcontroller.option;

import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.playpad.viewcontroller.main.IMainViewController;

/**
 * * Schnittstelle, um das ein Task zum Laden der Einstellungen angezeigt werden kann.
 *
 * @author tobias
 * @since 5.1.0
 */
public interface IGlobalReloadTask {

	Runnable getTask(GlobalSettings settings, IMainViewController controller);
}
