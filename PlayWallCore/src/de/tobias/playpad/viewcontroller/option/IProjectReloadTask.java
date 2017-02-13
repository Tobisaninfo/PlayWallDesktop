package de.tobias.playpad.viewcontroller.option;

import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectSettings;
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
public interface IProjectReloadTask {

	Task<Void> getTask(ProjectSettings settings, Project project, IMainViewController controller);
}
