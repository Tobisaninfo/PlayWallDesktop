package de.tobias.playpad.viewcontroller.option;

import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.concurrent.Task;

/**
 * * Schnittstelle, um das ein Task zum Laden der Einstellungen angezeigt werden kann.
 * 
 * @author tobias
 *
 * @since 5.1.0
 * 
 */
public interface IGlobalReloadTask {

	public Task<Void> getTask(GlobalSettings settings, IMainViewController controller);
}
