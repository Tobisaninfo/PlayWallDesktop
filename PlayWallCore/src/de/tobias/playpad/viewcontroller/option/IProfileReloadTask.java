package de.tobias.playpad.viewcontroller.option;

import de.tobias.playpad.profile.ProfileSettings;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.viewcontroller.main.IMainViewController;

/**
 * * Schnittstelle, um das ein Task zum Laden der Einstellungen angezeigt werden kann.
 * 
 * @author tobias
 *
 * @since 5.1.0
 * 
 */
public interface IProfileReloadTask {

	Runnable getTask(ProfileSettings settings, Project project, IMainViewController controller);
}
